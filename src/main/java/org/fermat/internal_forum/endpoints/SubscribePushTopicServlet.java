package org.fermat.internal_forum.endpoints;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.fermat.ArraysUtils;
import org.fermat.Context;
import org.fermat.CryptoBytes;
import org.fermat.KeyEd25519Java;
import org.fermat.forum.ResponseMessageConstants;
import org.fermat.internal_forum.endpoints.base.AuthEndpoint;
import org.fermat.push_notifications.PushDao;
import org.fermat.push_notifications.PushDao2;
import org.fermat.push_notifications.TopicSubscription;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static org.fermat.forum.ResponseMessageConstants.ERROR_DETAIL;
import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.*;

public class SubscribePushTopicServlet extends AuthEndpoint {

	private static final Logger logger = Logger.getLogger(SubscribePushTopicServlet.class);

	private PushDao2 pushDao;

	public SubscribePushTopicServlet() {
		this.pushDao = Context.getPushDao();
	}

	@Override
	public JsonObject doPost(HttpServletRequest req, HttpServletResponse resp, String profilePublicKey,JsonObject msg) {

		logger.info("SubscribePushTopicServlet");

		JsonObject responseObj = new JsonObject();
		String devicePushId = null;
		long topicId = -1;
		String signature = null;
		boolean subscribe = false;
		boolean invArg = false;

		if (!msg.has(KEY_PUSH_ID)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "KEY_PUSH_ID must not be null");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}else {
			devicePushId = msg.get(KEY_PUSH_ID).getAsString();
		}
		if (!msg.has(KEY_TOPIC_ID)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "topicId must not be less than 1");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}else {
			String topicIdStr = msg.get(KEY_TOPIC_ID).getAsString();
			if (topicIdStr!=null){
				topicId = Long.parseLong(topicIdStr);
			}
			if (topicId<1){
				responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "topicId must not be less than 1");
				resp.setStatus(HttpStatus.BAD_REQUEST_400);
				invArg = true;
			}
		}
		if (!msg.has(KEY_SIGNATURE)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "signature must not be null");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}else {
			signature = msg.get(KEY_SIGNATURE).getAsString();
			if (signature==null){
				responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "signature must not be null");
				resp.setStatus(HttpStatus.BAD_REQUEST_400);
				invArg = true;
			}else if (signature.length()==0){
				responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "signature must not be null");
				resp.setStatus(HttpStatus.BAD_REQUEST_400);
				invArg = true;
			}
		}

		if (!msg.has(KEY_UNSUBSCRIBE)){
			subscribe = msg.get(KEY_UNSUBSCRIBE).getAsBoolean();
		}else {
			subscribe = true;
		}

		if (!invArg) {
			byte[] signBytes = CryptoBytes.fromHexToBytes(signature);
//			if (checkSignature(topicId,devicePushId, profilePublicKey, signBytes)) {
				try {
					if (subscribe) {
					logger.info("suscribe push id: " + devicePushId);
						TopicSubscription topicSubscription = pushDao.addTopicSubscription(
								topicId,
								devicePushId
						);
						resp.setStatus(HttpStatus.OK_200);
					}else {
						logger.info("remove suscribe push id: " + devicePushId);
						TopicSubscription topicSubscription = pushDao.removeTopicSubscription(
								topicId,
								devicePushId
						);
						resp.setStatus(HttpStatus.OK_200);
					}
				} catch (Exception e) {
//					logger.error("CantPushDeviceException", e);
					responseObj.addProperty(ERROR_DETAIL, "server error: " + e.getMessage());
					resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
				}
//			} else {
//				logger.error("bad signature");
//				responseObj.addProperty(ERROR_DETAIL, "bad signature");
//				resp.setStatus(HttpStatus.BAD_REQUEST_400);
//			}
		}
		return responseObj;
	}

	private boolean checkSignature(long topicId,String devicePushId,String publicKey,byte[] signature){
		try {
			ByteBuffer byteBuffer = ByteBuffer.allocate(8).putLong(topicId);
			byte[] rawBytes = devicePushId.getBytes("UTF-8");
			return KeyEd25519Java.verifyMsg(signature, ArraysUtils.concatenateByteArrays(byteBuffer.array(),rawBytes),CryptoBytes.fromHexToBytes(publicKey));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
	}
}
