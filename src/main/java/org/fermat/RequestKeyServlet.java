package org.fermat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.fermat.db.IdentityData;
import org.fermat.db.exceptions.CantSaveIdentityException;
import org.fermat.forum.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Map;

public class RequestKeyServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(RequestKeyServlet.class);

	private ForumClient forumClient;

	public RequestKeyServlet() {
		this.forumClient = new ForumClientDiscourseImp(
				Context.getForumUrl(),
				Context.getApiKey(),
				Context.getAdminUsername()
		);
	}

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setStatus(HttpStatus.OK_200);
		resp.getWriter().println("EmbeddedJetty");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//super.doPost(req, resp);

        String ipAddress = req.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = req.getRemoteAddr();
        }

        logger.info("RequestKeyServlet, client ip: "+ipAddress);

		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = req.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) { /*report an error*/ }

		JsonParser jsonParser = new JsonParser();
		JsonObject jsonElement = (JsonObject) jsonParser.parse(jb.toString());

		String userName = jsonElement.get("username").getAsString();
		String password = jsonElement.get("password").getAsString();

		JsonObject responseObj = new JsonObject();

		logger.info("username: "+userName);
//		logger.info("email: "+email);
		logger.info("password: "+password);


		IdentityData identityData = Context.getDao().getIdentity(userName);
		if (identityData==null){
			if (forumClient.loginUser(userName,password))
				identityData = new IdentityData(userName,password,null,null);
		}

		if (identityData!=null) {

			if (!identityData.getPassword().equals(password)) {
				responseObj.addProperty(ResponseMessageConstants.USER_ERROR_STR, "Invalid password");
				resp.setStatus(HttpStatus.FORBIDDEN_403);
			} else {

				if (userName != null && password != null) {

					if (identityData.getApiKey() == null) {
						String apiKey = null;
						try {
							apiKey = forumClient.requestKey(userName, password);
							if (apiKey != null) {
								identityData.setApiKey(apiKey);
								Context.getDao().saveIdentity(identityData);

								resp.setStatus(HttpStatus.OK_200);
								responseObj.addProperty(ResponseMessageConstants.API_KEY, apiKey);
							} else {
								resp.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500);
							}

						} catch (UserNotActiveException e) {
							responseObj.addProperty(ResponseMessageConstants.USER_ERROR_STR, e.getMessage() + ", please verify your mail account");
							resp.setStatus(HttpStatus.FORBIDDEN_403);
						} catch (UserNotFoundException e) {
							logger.error("Data: "+userName+" "+password,e);
							responseObj.addProperty(ResponseMessageConstants.USER_ERROR_STR, "User not found");
							resp.setStatus(HttpStatus.FORBIDDEN_403);
						} catch (Exception e) {
							e.printStackTrace();
							logger.error(e);
						}
					}else {
						// here i check if the key that i had is fine
						logger.info("check login with user apikey");
						if(!forumClient.loginUser(userName,password,identityData.getApiKey())){
							logger.info("check login with user fail");
							String apiKey = null;
							try {
								apiKey = forumClient.requestKey(userName, password);
								if (apiKey != null) {
									identityData.setApiKey(apiKey);
									Context.getDao().saveIdentity(identityData);

									resp.setStatus(HttpStatus.OK_200);
									responseObj.addProperty(ResponseMessageConstants.API_KEY, apiKey);
								} else {
									resp.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500);
								}

							} catch (UserNotActiveException e) {
								responseObj.addProperty(ResponseMessageConstants.USER_ERROR_STR, e.getMessage() + ", please verify your mail account");
								resp.setStatus(HttpStatus.FORBIDDEN_403);
							} catch (UserNotFoundException e) {
								logger.error("Data: "+userName+" "+password,e);
								responseObj.addProperty(ResponseMessageConstants.USER_ERROR_STR, "User not found");
								resp.setStatus(HttpStatus.FORBIDDEN_403);
							} catch (Exception e) {
								e.printStackTrace();
								logger.error(e);
							}

						}else {
							logger.info("check login with user good");
							resp.setStatus(HttpStatus.OK_200);
							responseObj.addProperty(ResponseMessageConstants.API_KEY, identityData.getApiKey());
						}
					}
				} else {
					responseObj.addProperty(ResponseMessageConstants.USER_ERROR_STR, "Username & password null");
				}
			}
		}else {
			responseObj.addProperty(ResponseMessageConstants.USER_ERROR_STR, "Invalid user");
			resp.setStatus(HttpStatus.FORBIDDEN_403);
		}
		PrintWriter pWriter = resp.getWriter();
		pWriter.println(responseObj.toString());
	}
}
