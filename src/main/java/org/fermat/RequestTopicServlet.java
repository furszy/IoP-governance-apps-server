package org.fermat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.fermat.db.IdentityData;
import org.fermat.forum.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class RequestTopicServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(RequestTopicServlet.class);

	private ForumClient forumClient;

	public RequestTopicServlet() {
		this.forumClient = new ForumClientDiscourseImp();
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
		int topicId = jsonElement.get("id").getAsInt();

		JsonObject responseObj = new JsonObject();





		logger.info("json element: "+jsonElement);

		logger.info("username: "+userName);
//		logger.info("email: "+email);
		logger.info("password: "+password);
		logger.info("topic id: "+topicId);


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


					String topic = null;
					try {
						topic = forumClient.getTopic(topicId);
						if (topic != null) {
							logger.info("get topic ok!, response: "+topic);
							resp.setStatus(HttpStatus.OK_200);
							responseObj.addProperty(ResponseMessageConstants.TOPIC_POST, topic);
						} else {
							resp.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500);
						}

					} catch (Exception e) {
						e.printStackTrace();
						logger.error(e);
						resp.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500);
					}
				} else {
					resp.setStatus(HttpStatus.FORBIDDEN_403);
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
