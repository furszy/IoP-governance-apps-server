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
import org.fermat.internal_forum.endpoints.base.AuthEndpoint;
import org.fermat.internal_forum.model.Topic;
import org.fermat.push_notifications.Firebase;
import org.fermat.push_notifications.SuscriptionType;
import org.fermat.push_notifications.message.NewTopicPushMsg;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.fermat.forum.ResponseMessageConstants.ERROR_DETAIL;
import static org.fermat.forum.ResponseMessageConstants.TOPIC;
import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.*;

/**
 * todo: make this..
 */
public class RequestChangeTopicServlet extends AuthEndpoint {

	private static final Logger logger = Logger.getLogger(RequestChangeTopicServlet.class);

	private PostDao postDao;
	private NotificationDispatcher dispatcher;

	public RequestChangeTopicServlet() {
		postDao = Context.getPostDao();
		dispatcher = Context.getNotificationDispatcher();
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

		String title = null;
		String subTitle = null;
		String category = null;
		String raw = null;
		String signature = null;
		long ccValue = 0;
		boolean invArg = false;

		if (!msg.has(KEY_TITLE) || ((title = msg.get(KEY_TITLE).getAsString())!=null && title.length()==0)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "title must not be null");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}
		if (!msg.has(KEY_SUBTITLE) || ((subTitle = msg.get(KEY_SUBTITLE).getAsString())!=null && subTitle.length()==0)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "subTitle must not be null");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}
		if (!msg.has(KEY_CATEGORY) || ((category = msg.get(KEY_CATEGORY).getAsString())!=null && category.length()==0)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "category must not be null");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
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
		if (!msg.has(KEY_CC_VALUE) || ((ccValue = msg.get(KEY_CC_VALUE).getAsLong())<=1)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "cc value must not be null");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}

		List<String> categories = new ArrayList<>();
		categories.add(category);

		byte[] signBytes = CryptoBytes.fromHexToBytes(signature);
		if (checkSignature(title,subTitle,raw,profilePublicKey,signBytes)) {
			if (!invArg) {
				try {
					//todo: check if title exists..
					Topic topic = Topic.newTopic(
							profilePublicKey,
							title,
							subTitle,
							categories,
							raw,
							signBytes,
							ccValue
					);
					long topicId = postDao.saveTopic(
							topic
					);
					logger.info("topic saved with id: "+topicId);
					responseObj.addProperty(TOPIC, topic.toJson());
					resp.setStatus(HttpStatus.OK_200);

					// notify users
					pushTopicCreation(topicId,topic.getTitle());

				} catch (CantSavePostException e) {
					logger.error("CantSavePostException", e);
					responseObj.addProperty(ERROR_DETAIL, "server error: " + e.getMessage());
					resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
				}
			}
		}else {
			logger.error("bad signature");
			responseObj.addProperty(ERROR_DETAIL, "bad signature");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
		}
		return responseObj;
	}

	private boolean checkSignature(String title,String subtitle,String raw,String publicKey,byte[] signature){
		try {
			byte[] titleBytes = title.getBytes("UTF-8");
			byte[] subtitleBytes = subtitle.getBytes("UTF-8");
			byte[] rawBytes = raw.getBytes("UTF-8");
			return KeyEd25519Java.verifyMsg(signature, ArraysUtils.concatenateByteArrays(titleBytes,subtitleBytes,rawBytes),CryptoBytes.fromHexToBytes(publicKey));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void pushTopicCreation(long topicId,String title){
		dispatcher.dispatchTopicNotification(topicId, Firebase.Type.CONTRIB, Firebase.Type.VOT);
	}
}
