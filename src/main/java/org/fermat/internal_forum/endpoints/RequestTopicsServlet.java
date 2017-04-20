package org.fermat.internal_forum.endpoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.fermat.Context;
import org.fermat.forum.ResponseMessageConstants;
import org.fermat.internal_forum.db.PostDao;
import org.fermat.internal_forum.db.ProfileNotFoundException;
import org.fermat.internal_forum.db.ProfilesDao;
import org.fermat.internal_forum.endpoints.base.AuthEndpoint;
import org.fermat.internal_forum.model.Profile;
import org.fermat.internal_forum.model.Topic;
import org.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.fermat.forum.ResponseMessageConstants.TOPIC;
import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.*;

public class RequestTopicsServlet extends AuthEndpoint {

	private static final Logger logger = Logger.getLogger(RequestTopicsServlet.class);

	private PostDao postDao;
	private ProfilesDao profilesDao;

	public RequestTopicsServlet() {
		postDao = Context.getPostDao();
		profilesDao = Context.getProfilesDao();
	}

	@Override
	public String doGetImp(HttpServletRequest req, HttpServletResponse resp) {

		int maxRecordAmount = 10;
		long startTime = 0;
		boolean minimizaData = false;
		boolean orderByDate = false;

		String timeStr = req.getParameter(KEY_TIME);
		if (timeStr!=null)
			startTime = Long.parseLong(timeStr);
		String maxStr = req.getParameter(KEY_MAX_RECORD_AMOUNT);
		if (maxStr!=null) {
			maxRecordAmount = Integer.parseInt(maxStr);
		}
		String minDataStr = req.getParameter(KEY_MINIMIZE_DATA);
		if (minDataStr!=null){
			minimizaData = Boolean.parseBoolean(minDataStr);
		}

		String json = null;
		List<Topic> topicList = postDao.getTopicsAfterTime(startTime,maxRecordAmount);
		if (minimizaData){
			JsonArray jsonArray = new JsonArray();
			for (Topic topic : topicList) {
				try {
					jsonArray.add(toMinDataJson(topic,profilesDao.getProfile(topic.getOwnerPk())));
				} catch (ProfileNotFoundException e) {
					logger.error("profile not found looking for comments, pk: "+topic.getOwnerPk());
				}
			}
			json = jsonArray.toString();
		}else {
			JSONArray jsArray = new JSONArray(topicList);
			json = jsArray.toString();
		}
		return json;
	}

	@Override
	public JsonObject doPost(HttpServletRequest req, HttpServletResponse resp, String profilePublicKey,JsonObject msg) {

		JsonObject responseObj = new JsonObject();

		long topicId = -1;
		boolean invArg = false;


		if (!msg.has(KEY_TOPIC_ID) || ((topicId = msg.get(KEY_TOPIC_ID).getAsLong())>0)){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "topicId must not be less than 1");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}

		if (!invArg) {
			responseObj.addProperty(TOPIC, postDao.getTopics(topicId).toJson());
			resp.setStatus(HttpStatus.OK_200);
		}
		return responseObj;
	}

	public String toMinDataJson(Topic topic, Profile profile){
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("topic_id",topic.getId());
		jsonObject.addProperty("title",topic.getTitle());
		jsonObject.addProperty("posts",(topic.getPosts()!=null)?topic.getPosts().size():0);
		jsonObject.addProperty("ccValue",topic.getCcValueInToshis());
		jsonObject.addProperty("pubTime",topic.getPubTime());
		jsonObject.addProperty("profName",profile.getName());
		jsonObject.addProperty("profPk",profile.getPk());

		return jsonObject.toString();
	}
}
