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
import org.fermat.internal_forum.model.Comment;
import org.fermat.internal_forum.model.Post;
import org.fermat.internal_forum.model.Profile;
import org.fermat.internal_forum.model.Topic;
import org.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.fermat.forum.ResponseMessageConstants.TOPIC;
import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.*;

public class RequestCommentsServlet extends AuthEndpoint {

	private static final Logger logger = Logger.getLogger(RequestCommentsServlet.class);

	private PostDao postDao;
	private ProfilesDao profilesDao;
	private NotificationDispatcher notificationDispatcher;

	public RequestCommentsServlet() {
		postDao = Context.getPostDao();
		profilesDao = Context.getProfilesDao();
		notificationDispatcher = Context.getNotificationDispatcher();
	}

	@Override
	public String doGetImp(HttpServletRequest req, HttpServletResponse resp) {

		int maxRecordAmount = 10;
		long startTime = 0;
		boolean minimizaData = false;
		boolean orderByDate = false;
		long topicId = -1;

		String timeStr = req.getParameter(KEY_TIME);
		if (timeStr != null)
			startTime = Long.parseLong(timeStr);
		String maxStr = req.getParameter(KEY_MAX_RECORD_AMOUNT);
		if (maxStr != null) {
			maxRecordAmount = Integer.parseInt(maxStr);
		}
		String topicIdStr = req.getParameter(KEY_TOPIC_ID);
		if (topicIdStr != null) {
			topicId = Long.parseLong(topicIdStr);
			if (topicId>0) {
				String json = null;
				List<Post> postList = postDao.getCommentsForTopic(topicId, startTime, maxRecordAmount);
				if (postList!=null) {
					List<Comment> comments = new ArrayList<>();
					for (Post post : postList) {
						Profile profile = null;
						try {
							profile = profilesDao.getProfile(post.getOwnerPk());
						} catch (ProfileNotFoundException e) {
							logger.error("profile not found looking for comments, pk: "+post.getOwnerPk());
						}
						Comment comment = new Comment(post.getPubTime(), post.getOwnerPk(), profile.getName(), null, post.getRaw());
						comments.add(comment);
					}
					resp.setStatus(HttpStatus.OK_200);
					JSONArray jsonArray = new JSONArray(comments);
					return jsonArray.toString();
				}else {
					resp.setStatus(HttpStatus.OK_200);
					return new JsonObject().toString();
				}
			}
		}

		JsonObject responseObj = new JsonObject();
		responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "topicId must not be less than 1");
		resp.setStatus(HttpStatus.BAD_REQUEST_400);
		return responseObj.toString();
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
		jsonObject.addProperty("pubTime",topic.getPubTime());
		jsonObject.addProperty("profName",profile.getName());
		jsonObject.addProperty("profPk",profile.getPk());
		return jsonObject.toString();
	}
}
