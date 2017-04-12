package org.fermat;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.fermat.forum.ResponseMessageConstants.IOP_RATE_USD;

public class RequestIopRateUsdServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(RequestIopRateUsdServlet.class);


	public RequestIopRateUsdServlet() {
	}

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

        logger.info("RequestIopRateUsdServlet");


        JSONObject jsonObject = new JSONObject();

        jsonObject.put(IOP_RATE_USD,Context.getExtraData().getMonthRateIoP().toEngineeringString());

		resp.setStatus(HttpStatus.OK_200);
		resp.getWriter().println(jsonObject.toString());
	}



}
