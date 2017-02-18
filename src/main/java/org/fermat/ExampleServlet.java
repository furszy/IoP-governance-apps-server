package org.fermat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.fermat.forum.ForumClientDiscourseImp;
import org.fermat.forum.discourse.DiscouseApiConstants;

public class ExampleServlet extends HttpServlet {


	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {



		resp.setStatus(HttpStatus.OK_200);
		resp.getWriter().println(serverStatus(req));
	}

	private String serverStatus(HttpServletRequest req) {

		StringBuilder stringBuilder = new StringBuilder();

		String ipAddress = req.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = req.getRemoteAddr();
		}

		stringBuilder.append("Server status:")
				.append("\n")
				.append("Online time: "+ convertStartTime(System.currentTimeMillis()-Context.getStartTime()))
				.append("\n")
				.append("IoP datadir: "+Context.getIopCoreDir())
				.append("\n")
				.append("Forum url: "+ DiscouseApiConstants.FORUM_URL)
				.append("\n")
				.append("Your ip: "+ipAddress);

		return stringBuilder.toString();

	}

	private String convertStartTime(long millis){
		return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
				TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
				TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
	}
}
