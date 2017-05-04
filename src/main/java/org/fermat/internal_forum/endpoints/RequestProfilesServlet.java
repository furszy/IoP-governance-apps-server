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
import java.util.ArrayList;
import java.util.List;

import static org.fermat.forum.ResponseMessageConstants.TOPIC;
import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.*;

public class RequestProfilesServlet extends AuthEndpoint {

	private static final Logger logger = Logger.getLogger(RequestProfilesServlet.class);

	private ProfilesDao profilesDao;

	public RequestProfilesServlet() {
		profilesDao = Context.getProfilesDao();
	}

	@Override
	public String doGetImp(HttpServletRequest req, HttpServletResponse resp) {

		String profKey = req.getParameter(KEY_PUBLIC_KEY);

		List<Profile> list = new ArrayList<>();

		if(profKey!=null){
			try {
				list.add(profilesDao.getProfile(profKey));
			} catch (ProfileNotFoundException e) {
				logger.info("ProfileNotFoundException",e);
			}
		}else {
			list.addAll(profilesDao.getProfiles());
		}

		String json = null;
		JsonArray jsonArray = new JsonArray();
		for (Profile profile : list) {
			jsonArray.add(profile.toJson());
		}
		json = jsonArray.toString();

		return json;
	}


}
