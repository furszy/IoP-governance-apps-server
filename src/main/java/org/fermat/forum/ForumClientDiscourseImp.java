package org.fermat.forum;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.fermat.forum.discourse.DiscourseApiClient;
import org.fermat.forum.discourse.DiscouseApiConstants;
import org.fermat.forum.discourse.utils.ResponseModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Map;


/**
 * Created by mati on 28/11/16.
 */

public class ForumClientDiscourseImp implements ForumClient {

    private static final Logger logger = Logger.getLogger(ForumClientDiscourseImp.class);

    //private final ForumConfigurations conf;

    private DiscourseApiClient client;

    private ForumProfile forumProfile;

//    private String apiKey;

//    private boolean isActive;

    public ForumClientDiscourseImp() {
        forumProfile = null;// aca iria el profile del admin.  forumConfigurations.getForumUser();
        init();
    }

    private void init(){
        client = new DiscourseApiClient(
                DiscouseApiConstants.FORUM_URL,//args[0] // api_url  : e.g. http://your_domain.com
                DiscouseApiConstants.API_KEY, //, args[1] // api_key : you get from discourse admin
                DiscouseApiConstants.API_USER_BEHALF_OF //, args[2] // api_username : you make calls on behalf of
        );

    }


    @Override
    public ForumProfile getForumProfile() {
        return forumProfile;
    }

    /**
     *  Check if the user exist
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public boolean connect(String username, String password) throws UserNotFoundException, InactiveUserException {
//        if (isActive) throw new IllegalStateException("Forum is already connected");
        init();
//        LOG.debug("connect");
        //if (apiKey == null) {
            try {
                // get user
                ForumProfile getUserResponse = getUser(username);
                if (getUserResponse!=null) {
                    getUserResponse.setPassword(password);
                    if (getUserResponse.isActive()) {
                        // get api key
                        String apiKey = requestApiKey(forumProfile);
                        if (apiKey == null) {
                            return false;
                        }
                        return true;
                    }else {
                        throw new InactiveUserException("User not verified account");
                    }
                }
            } catch (InactiveUserException e) {
                throw e;
            }catch (Exception e){
                e.printStackTrace();
            }
        //}
        return false;
    }

    @Override
    public boolean trustUser(ForumProfile forumProfile) {
        System.out.println("Trust user");
        Map<String,String> parameters;
        parameters = new HashMap<String, String>();
        parameters.put("userid", ""+forumProfile.getForumId());
        parameters.put("username", forumProfile.getUsername());
        parameters.put("level", "2"); // level can be: 0 (new user), 1 (basic user), 2 (regular user), 3 (leader), 4 (elder)
        ResponseModel responseModel = client.trustUser(parameters);
        System.out.println("trust user response: "+ responseModel);
        if (responseModel.meta.code>201){
            return false;
        }
        return true;

    }

    @Override
    public boolean loginUser(String userName, String password) {
        Map<String,String> parameters;
        parameters = new HashMap<String, String>();
        parameters.put("password", ""+password);
        parameters.put("username", userName);
        ResponseModel responseModel = client.loginUser(parameters);
        if (responseModel.meta.code>201){
            return true;
        }else
            return false;
    }

    public ForumProfile getUser(String username) throws UserNotFoundException {
//        LOG.debug("getUser");
        ForumProfile forumProfile = null;
        Map<String, String> parameters = new HashMap<String, String>();
        parameters = new HashMap<String, String>();
        parameters.put("username", username);
        ResponseModel responseModel = client.getUser(parameters);
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = null;
        try {
            jsonElement = jsonParser.parse("" + responseModel.data);
        }catch (Exception e){
            throw new UserNotFoundException();
        }
        JsonArray jsonArray = null;
        JsonObject userObject = null;
        if (jsonElement!=null) {
            userObject = jsonElement.isJsonObject()?jsonElement.getAsJsonObject():null;
            jsonArray = jsonElement.isJsonArray()?jsonElement.getAsJsonArray():null;
        }

        if (userObject!=null && userObject.has("user")) {
            userObject = userObject.getAsJsonObject("user");
        }else
            return null;

        forumProfile = new ForumProfile(
                userObject.get("id").getAsLong(),
                userObject.get("name").getAsString(),
                userObject.get("username").getAsString(),
                true//(userObject.has("active")) ? userObject.get("active").getAsBoolean():false
        );
        return forumProfile;

    }

    @Override
    public String requestKey(String username, String password) throws UserNotActiveException, UserNotFoundException {
        String key = null;
        ForumProfile forumProfile = getUser(username);
        if (forumProfile!=null) {
            if (!forumProfile.isActive()) throw new UserNotActiveException("User: "+forumProfile.getUsername()+" not active");
                key = requestApiKey(forumProfile);
        }
        return key;
    }

    /**
     *
     *
     * @param forumProfile
     * @return
     */
    public String requestApiKey(ForumProfile forumProfile){
        // -> generate api_key
        String apiKey = null;
        Map<String, String> parameters = new HashMap<String, String>();
        parameters = new HashMap<String, String>();
        parameters.put("userid", ""+forumProfile.getForumId());
        parameters.put("username", forumProfile.getUsername());
        ResponseModel responseModel = client.generateApiKey(parameters);

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject1 = null;
        jsonObject1 = jsonParser.parse( ""+responseModel.data ).getAsJsonObject();

        if (jsonObject1!=null && jsonObject1.has("api_key")) {
            jsonObject1 = jsonObject1.getAsJsonObject("api_key");

            if (jsonObject1.has("key")) {
                apiKey = jsonObject1.get("key").getAsString();
            }
        }
        return apiKey;
    }

    @Override
    public ForumProfile registerUser(String username, String password, String email,boolean active) throws CantRegisterUserException, Exception {
//        if (forumProfile!=null) throw new IllegalStateException("Forum profile already exist");
        System.out.println("registerUser");
        try {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("name", username);
            parameters.put("email", email);
            parameters.put("username", username);
            parameters.put("password", password);
            parameters.put("active", String.valueOf(active)); // por ahora lo dejo activo hasta que vea como hacer la auth..

            ResponseModel responseModel = client.createUser(parameters);
//        LOG.info("createUser responseModel -> " + responseModel.toString());
            if (checkIfResponseFail(responseModel)) {
                throwException(responseModel);
            }
//            JsonParser jsonParser = new JsonParser();
//            JsonObject jsonObject1 = (JsonObject) jsonParser.parse(responseModel.data.toString());

            JSONObject jsonObject = null;

            try {

                jsonObject = new JSONObject(responseModel.data.toString());

            }catch (Exception e){
                logger.error("registerUser forum error: code: "+responseModel.meta.code+", data: "+responseModel.data.toString());
                throw new Exception("Forum error, code: "+responseModel.meta.code);
            }


            if (jsonObject.has("errors")) {
                StringBuilder errorsStr = new StringBuilder();
                JSONObject errors = (JSONObject) jsonObject.get("errors");
                if (errors.has("email")) {
                    errorsStr.append("Email "+errors.get("email"));
                    errorsStr.append("\n");
                }
                if (errors.has("username")) {
                    errorsStr.append("Username "+errors.get("username"));
                    errorsStr.append("\n");
                }
                if (errors.has("password")) {
                    errorsStr.append("Password "+errors.get("password"));
                }
                throw new CantRegisterUserException(errorsStr.toString());
            }
//            try {
//                jsonObject = (JSONObject) jsonObject.get("user");
//            }catch (Exception e){
//                e.printStackTrace();
//            }
            if (jsonObject!=null)
                System.out.println("data received: "+jsonObject.toString());
            ForumProfile forumProfile = new ForumProfile(
                    jsonObject.getInt("user_id"),
                    username,
                    true);
            return forumProfile;
        } catch (CantRegisterUserException e){
            throw e;
        }
    }

    @Override
    public String getTopic(int topicId) throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("id", String.valueOf(topicId));
        ResponseModel responseModel = client.getTopic(parameters);

        if (responseModel.meta.code>201){

            logger.error("## getTopic error, data: "+responseModel.data.toString());

            throw new Exception("Forum error, code: "+responseModel.meta.code);
        }

        try {
            JSONObject jsonObject = new JSONObject(responseModel.data.toString());
            jsonObject = jsonObject.getJSONObject("post_stream");
            JSONArray jsonArray = jsonObject.getJSONArray("posts");
//            JSONArray jsonArray = new JSONArray(jsonObject.get("posts"));

            // post
            jsonObject = (JSONObject) jsonArray.get(0);
            // body
            String formatedBody = jsonObject.getString("cooked");
            return formatedBody;
        } catch (JSONException e) {
            logger.error("## getTopic error, data: "+responseModel.data.toString());
            e.printStackTrace();
        }

        return null;
    }




    private boolean checkIfResponseFail(ResponseModel responseModel){
        return responseModel.meta.code>201 || responseModel.data==null;
    }

    private void throwException(ResponseModel responseModel){
//        LOG.error("throwException");
        switch (responseModel.meta.code){
            default:

                break;
        }

    }
}
