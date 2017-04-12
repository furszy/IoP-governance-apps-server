package org.fermat.internal_forum.endpoints.base;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.fermat.Context;
import org.fermat.forum.ResponseMessageConstants;
import org.fermat.internal_forum.db.ProfilesDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.KEY_PUBLIC_KEY;

/**
 * Created by mati on 04/04/17.
 */
public abstract class AuthEndpoint extends HttpServlet {

    private static final Logger logger = Logger.getLogger(AuthEndpoint.class);

    private ProfilesDao profilesDao;

    public AuthEndpoint() {
        this.profilesDao = Context.getProfilesDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // log request ip
        logIp(req);
        // parse and do request
        String json = doGetImp(req,resp);
        PrintWriter pWriter = resp.getWriter();
        pWriter.println(json);
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // log request ip
        logIp(req);
        // parse request
        JsonObject jsonElement = parseRequest(req);
        JsonObject responseObj = new JsonObject();
        if (jsonElement.has(KEY_PUBLIC_KEY)) {
            String profilePublicKey = jsonElement.get(KEY_PUBLIC_KEY).getAsString();
            if (profilesDao.containsProfile(profilePublicKey)){
                responseObj = doPost(req,resp,profilePublicKey,jsonElement);
            }else {
                responseObj.addProperty(ResponseMessageConstants.AUTH_FAIL, "profile public key not registered in server");
                resp.setStatus(HttpStatus.FORBIDDEN_403);
            }
        }else {
            responseObj.addProperty(ResponseMessageConstants.AUTH_FAIL, "profile public key not found in the post data");
            resp.setStatus(HttpStatus.FORBIDDEN_403);
        }
        PrintWriter pWriter = resp.getWriter();
        pWriter.println(responseObj.toString());
    }


    /**
     *
     * @param req
     * @param resp
     * @return responseObject -> reponse object
     */
    public String doGetImp(HttpServletRequest req, HttpServletResponse resp){
        throw new UnsupportedOperationException("Method not implemented..") ;
    }

    /**
     *
     * @param req
     * @param resp
     * @param profilePublicKey
     * @param msg
     * @return responseObject -> reponse object
     */
    public JsonObject doPost(HttpServletRequest req, HttpServletResponse resp,String profilePublicKey,JsonObject msg){
        throw new UnsupportedOperationException("Method not implemented..") ;
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

    private void logIp(HttpServletRequest req){
        String ipAddress = req.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = req.getRemoteAddr();
        }

        logger.info("AuthEndpoint, client ip: " + ipAddress);
    }

}
