package org.fermat.internal_forum.endpoints;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.fermat.Context;
import org.fermat.forum.ResponseMessageConstants;
import org.fermat.internal_forum.db.CantSavePostException;
import org.fermat.internal_forum.db.CantUpdateProfileException;
import org.fermat.internal_forum.db.ProfileNotFoundException;
import org.fermat.internal_forum.db.ProfilesDao;
import org.fermat.internal_forum.model.Profile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import static org.fermat.forum.ResponseMessageConstants.ERROR_DETAIL;
import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.KEY_PROFILE_NAME;
import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.KEY_PUBLIC_KEY;

/**
 * todo: image..
 */
public class RequestUpdateProfileServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(RequestUpdateProfileServlet.class);

	private ProfilesDao profilesDao;

	public RequestUpdateProfileServlet() {
		profilesDao = Context.getProfilesDao();
	}

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setStatus(HttpStatus.OK_200);
		resp.getWriter().println("EmbeddedJetty");
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		JsonObject responseObj = new JsonObject();
		String name = null;
		String profilePublicKey = null;
		boolean invArg = false;

		// log request ip
		logIp(req);
		// parse request
		JsonObject jsonElement = parseRequest(req);
		if (jsonElement.has(KEY_PUBLIC_KEY)) {
			profilePublicKey = jsonElement.get(KEY_PUBLIC_KEY).getAsString();
		}else {
			responseObj.addProperty(ResponseMessageConstants.AUTH_FAIL, "profile public key not found in the post data");
			resp.setStatus(HttpStatus.FORBIDDEN_403);
			invArg = true;
		}
		if (!jsonElement.has(KEY_PROFILE_NAME) || ((name = jsonElement.get(KEY_PROFILE_NAME).getAsString())!=null && name.length()==0)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "raw must not be null");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}
		if (!invArg) {
			try {
				logger.info("trying to update profile with pk: "+profilePublicKey);
				if(profilesDao.updateProfile(profilePublicKey,name)){
					resp.setStatus(HttpStatus.OK_200);
				}else {
					logger.error("Error updating profile, pk: "+profilePublicKey);
					responseObj.addProperty(ERROR_DETAIL, "server error.. ");
					resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
				}

			} catch (ProfileNotFoundException e) {
				logger.error("ProfileNotFoundException", e);
				responseObj.addProperty(ERROR_DETAIL, "server error: " + e.getMessage());
				resp.setStatus(HttpStatus.BAD_REQUEST_400);
			} catch (CantUpdateProfileException e) {
				logger.error("Error updating profile, pk: "+profilePublicKey,e);
				responseObj.addProperty(ERROR_DETAIL, "server error.. ");
				resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
			}
		}

		PrintWriter pWriter = resp.getWriter();
		pWriter.println(responseObj.toString());
	}

	private void logIp(HttpServletRequest req){
		String ipAddress = req.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = req.getRemoteAddr();
		}

		logger.info("AuthEndpoint, client ip: " + ipAddress);
	}

	private JsonObject parseRequest(HttpServletRequest req) {
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = req.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) { /*report an error*/ }

		JsonParser jsonParser = new JsonParser();
		return (JsonObject) jsonParser.parse(jb.toString());
	}

}
