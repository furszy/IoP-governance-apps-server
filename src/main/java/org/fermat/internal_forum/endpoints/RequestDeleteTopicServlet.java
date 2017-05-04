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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.fermat.forum.ResponseMessageConstants.ERROR_DETAIL;
import static org.fermat.forum.ResponseMessageConstants.TOPIC;
import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.*;

public class RequestDeleteTopicServlet extends AuthEndpoint {

	private static final Logger logger = Logger.getLogger(RequestDeleteTopicServlet.class);

	private ScheduledExecutorService scheduledExecutorService;

	private PostDao postDao;

	public RequestDeleteTopicServlet() {
		postDao = Context.getPostDao();
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
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

		long topicId = -1;
		String signature = null;
		long ccValue = 0;
		boolean invArg = false;

		if (!msg.has(KEY_TOPIC_ID) || ((topicId = msg.get(KEY_TOPIC_ID).getAsLong())<1)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "topic id must not be less than 1");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}
		if (!msg.has(KEY_SIGNATURE) || ((signature = msg.get(KEY_SIGNATURE).getAsString())!=null && signature.length()==0)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "signature must not be null");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}

		if (!invArg) {
			byte[] signBytes = CryptoBytes.fromHexToBytes(signature);
			if (checkSignature(topicId,profilePublicKey,signBytes)) {
				try {
					Topic topic = postDao.getTopics(topicId);
					if (topic.getOwnerPk()==profilePublicKey) {
						if (postDao.deleteTopic(topicId)) {
							logger.info("topic removed with id: " + topicId);
							resp.setStatus(HttpStatus.OK_200);
						} else {
							responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "topic not found, with id: " + topicId);
							resp.setStatus(HttpStatus.BAD_REQUEST_400);
						}
					}else {
						responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "profile key is not the owner of the topic");
						resp.setStatus(HttpStatus.BAD_REQUEST_400);
					}
				} catch (Exception e) {
					logger.error("CantDeleteTopic", e);
					responseObj.addProperty(ERROR_DETAIL, "server error: " + e.getMessage());
					resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
				}
			}else {
				logger.info("bad signature for: "+profilePublicKey);
				responseObj.addProperty(ERROR_DETAIL, "bad signature");
				resp.setStatus(HttpStatus.BAD_REQUEST_400);
			}
		}else{
			logger.info("Delete topic invalid arguments, json respond: "+responseObj.toString());
		}
		return responseObj;
	}

	private boolean checkSignature(long topicId,String publicKey,byte[] signature){
		ByteBuffer byteBuffer = ByteBuffer.allocate(8).putLong(topicId);
		return KeyEd25519Java.verifyMsg(signature, byteBuffer.array(),CryptoBytes.fromHexToBytes(publicKey));
	}

	private void pushTopicCreation(long topicId,String title){
		scheduledExecutorService.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					Firebase.pushFCMNotificationToGroup(SuscriptionType.TOPICS.getId(),new NewTopicPushMsg(topicId,title), Firebase.Type.CONTRIB, Firebase.Type.VOT);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		},5, TimeUnit.SECONDS);
	}
}
