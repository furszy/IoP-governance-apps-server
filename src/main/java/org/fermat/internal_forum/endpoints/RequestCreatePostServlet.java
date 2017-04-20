package org.fermat.internal_forum.endpoints;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.fermat.ArraysUtils;
import org.fermat.Context;
import org.fermat.CryptoBytes;
import org.fermat.KeyEd25519Java;
import org.fermat.forum.ResponseMessageConstants;
import org.fermat.internal_forum.db.CantSavePostException;
import org.fermat.internal_forum.db.PostDao;
import org.fermat.internal_forum.db.ProfilesDao;
import org.fermat.internal_forum.db.TopicNotFounException;
import org.fermat.internal_forum.endpoints.base.AuthEndpoint;
import org.fermat.internal_forum.model.Post;
import org.fermat.internal_forum.model.Profile;
import org.fermat.internal_forum.model.Topic;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static org.fermat.forum.ResponseMessageConstants.ERROR_DETAIL;
import static org.fermat.forum.ResponseMessageConstants.POST_ID;
import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.*;

public class RequestCreatePostServlet extends AuthEndpoint {

	private static final Logger logger = Logger.getLogger(RequestCreatePostServlet.class);

	protected ProfilesDao profilesDao;
	private PostDao postDao;
	private NotificationDispatcher notificationDispatcher;

	public RequestCreatePostServlet() {
		postDao = Context.getPostDao();
		notificationDispatcher = Context.getNotificationDispatcher();
		profilesDao = Context.getProfilesDao();
	}

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setStatus(HttpStatus.OK_200);
		resp.getWriter().println("EmbeddedJetty");
	}

	@Override
	public JsonObject doPost(HttpServletRequest req, HttpServletResponse resp, String profilePublicKey,JsonObject msg) {

		JsonObject responseObj = new JsonObject();

		String raw = null;
		long topicId = -1;
		String signature = null;
		boolean invArg = false;

		if (!msg.has(KEY_TOPIC_ID)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "topicId must not be less than 1");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}else {
			if((topicId = msg.get(KEY_TOPIC_ID).getAsLong())<=0){
				responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "topicId must not be less than 1");
				resp.setStatus(HttpStatus.BAD_REQUEST_400);
				invArg = true;
			}
		}
		if (!msg.has(KEY_RAW) || ((raw = msg.get(KEY_RAW).getAsString())!=null && raw.length()==0)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "raw must not be null");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}
		if (!msg.has(KEY_SIGNATURE) || ((signature = msg.get(KEY_SIGNATURE).getAsString())!=null && signature.length()==0)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "signature must not be null");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}

		byte[] signBytes = CryptoBytes.fromHexToBytes(signature);
		if (checkSignature(topicId,raw,profilePublicKey,signBytes)) {
			if (!invArg) {
				try {
					logger.info("save post for topic id: "+topicId);
					long postId = postDao.savePost(
							Post.newPost(
									topicId,
									profilePublicKey,
									raw,
									signBytes
							)
					);
					responseObj.addProperty(POST_ID, postId);
					resp.setStatus(HttpStatus.OK_200);

					// notify users
					Profile profile = profilesDao.getProfile(profilePublicKey);
					notificationDispatcher.dispatchTopicNotification(topicId,profile.getAppType());
				} catch (CantSavePostException e) {
					logger.error("CantSavePostException", e);
					responseObj.addProperty(ERROR_DETAIL, "server error: " + e.getMessage());
					resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
				} catch (TopicNotFounException e) {
					logger.error("TopicNotFounException", e);
					responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "topic not found, id: "+e.getTopicId());
					resp.setStatus(HttpStatus.BAD_REQUEST_400);
				}
			}
		}else {
			logger.error("bad signature");
			responseObj.addProperty(ERROR_DETAIL, "bad signature");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
		}
		return responseObj;
	}

	private boolean checkSignature(long topicId,String raw,String publicKey,byte[] signature){
		try {
			byte[] topicBytes = ByteBuffer.allocate(8).putLong(topicId).array();
			byte[] rawBytes = raw.getBytes("UTF-8");
			return KeyEd25519Java.verifyMsg(signature, ArraysUtils.concatenateByteArrays(topicBytes,rawBytes),CryptoBytes.fromHexToBytes(publicKey));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
	}
}
