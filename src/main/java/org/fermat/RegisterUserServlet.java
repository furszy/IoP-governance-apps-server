package org.fermat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.fermat.db.IdentityData;
import org.fermat.forum.*;
import org.fermat.forum.discourse.utils.ResponseModel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class RegisterUserServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(RegisterUserServlet.class);

	private ForumClient forumClient;

	public RegisterUserServlet() {
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

        logger.info("Register user");

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
        String email = jsonElement.get("email").getAsString();

       logger.info("json element: "+jsonElement);

		System.out.println("username: "+userName);
		System.out.println("email: "+email);
		System.out.println("password: "+password);

        JsonObject responseObj = new JsonObject();
        ForumProfile forumProfile = null;
        try {
           logger.info("registering user ");
            forumProfile = forumClient.registerUser(userName,password,email,true);

           logger.info("register response: "+forumProfile);

            if (forumProfile!=null){

                // -> trust
                if (forumClient.trustUser(forumProfile)){
                    resp.setStatus(HttpStatus.OK_200);
                    //save user
                    Context.getDao().saveIdentity(new IdentityData(userName,password,email,null));
                }else {
                    resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    responseObj.addProperty("errors","fail trust user");
                }

            }else {
                resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            }

        } catch (CantRegisterUserException e) {
            resp.setStatus(HttpStatus.FORBIDDEN_403);
            responseObj.addProperty(ResponseMessageConstants.REGISTER_ERROR_STR,e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
            resp.setStatus(500);
            responseObj.addProperty(ResponseMessageConstants.REGISTER_ERROR_STR,e.getMessage());
            logger.error(e);
        }

        PrintWriter pWriter = resp.getWriter();
        pWriter.println(responseObj.toString());

	}
}
