package org.fermat.internal_forum.endpoints;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.fermat.Context;
import org.fermat.forum.ResponseMessageConstants;
import org.fermat.internal_forum.db.PostDao;
import org.fermat.internal_forum.endpoints.base.AuthEndpoint;
import org.fermat.internal_forum.model.Topic;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.fermat.forum.ResponseMessageConstants.TOPIC;
import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.*;

public class RequestTopicServlet extends AuthEndpoint {

	private static final Logger logger = Logger.getLogger(RequestTopicServlet.class);

	private PostDao postDao;

	public RequestTopicServlet() {
		postDao = Context.getPostDao();
	}

	@Override
	public String doGetImp(HttpServletRequest req, HttpServletResponse resp) {
		JsonObject responseObj = new JsonObject();
		long topicId = -1;
		boolean invArg = false;
		String topicIdStr = req.getParameter(KEY_TOPIC_ID);
		if (topicIdStr!=null){
			topicId = Long.parseLong(topicIdStr);
		}
		if (topicId<1){
			responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "topicId must not be less than 1");
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			invArg = true;
		}
		if (!invArg) {
			Topic topic = postDao.getTopics(topicId);
			if (topic!=null) {
				if (topic.getPosts() != null)
					topic.setPostCount(topic.getPosts().size());
				responseObj.addProperty(TOPIC, topic.toJson());
				resp.setStatus(HttpStatus.OK_200);
			}else {
				responseObj.addProperty(ResponseMessageConstants.INVALID_PARAMETER, "topicId not exist, id: "+topicIdStr);
				resp.setStatus(HttpStatus.BAD_REQUEST_400);
			}
		}
		return responseObj.toString();
	}

}
