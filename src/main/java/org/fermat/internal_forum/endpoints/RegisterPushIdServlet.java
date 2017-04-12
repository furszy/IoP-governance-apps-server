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
import org.fermat.internal_forum.db.CantUpdateProfileException;
import org.fermat.internal_forum.db.PostDao;
import org.fermat.internal_forum.db.ProfilesDao;
import org.fermat.internal_forum.endpoints.base.AuthEndpoint;
import org.fermat.internal_forum.model.Post;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static org.fermat.forum.ResponseMessageConstants.ERROR_DETAIL;
import static org.fermat.forum.ResponseMessageConstants.POST_ID;
import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.*;

public class RegisterPushIdServlet extends AuthEndpoint {

	private static final Logger logger = Logger.getLogger(RegisterPushIdServlet.class);

	private ProfilesDao profilesDao;

	public RegisterPushIdServlet() {
		this.profilesDao = Context.getProfilesDao();
	}

	@Override
	public JsonObject doPost(HttpServletRequest req, HttpServletResponse resp, String profilePublicKey,JsonObject msg) {

		logger.info("RegisterPushIdServlet");

		JsonObject responseObj = new JsonObject();
		String devicePushId = null;
		String signature = null;
		boolean invArg = false;

		if (!msg.has(KEY_PUSH_ID)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "KEY_PUSH_ID must not be null");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}else {
			devicePushId = msg.get(KEY_PUSH_ID).getAsString();
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
		if (!invArg) {
			byte[] signBytes = CryptoBytes.fromHexToBytes(signature);
//			if (checkSignature(devicePushId, profilePublicKey, signBytes)) {
				try {
					logger.info("register push id: " + devicePushId);
					profilesDao.addPushDeviceId(
							profilePublicKey,
							devicePushId
					);
//					responseObj.addProperty(POST_ID, postId);
					resp.setStatus(HttpStatus.OK_200);
				} catch (CantUpdateProfileException e) {
					logger.error("CantUpdateProfileException", e);
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

	private boolean checkSignature(String devicePushId,String publicKey,byte[] signature){
		try {
			byte[] rawBytes = devicePushId.getBytes("UTF-8");
			return KeyEd25519Java.verifyMsg(signature, rawBytes,CryptoBytes.fromHexToBytes(publicKey));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
	}
}
