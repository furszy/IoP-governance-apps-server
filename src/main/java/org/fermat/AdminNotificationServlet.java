package org.fermat;

import org.eclipse.jetty.http.HttpStatus;
import org.fermat.forum.discourse.DiscouseApiConstants;
import org.fermat.notifications.AdminNotificationUpdate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AdminNotificationServlet extends HttpServlet {


	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

    	String key;
		String user;

		Context.setAdminNotification(
				AdminNotificationUpdate.AdminNotificationType.UPDATE_APP,
				"You don't have the latest version of the app",
				5);

		resp.setStatus(HttpStatus.OK_200);
		resp.getWriter().println("OK");
	}

}
