package org.fermat;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Enumeration;

public class RequestIoPsServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(RequestIoPsServlet.class);


	public RequestIoPsServlet() {
	}

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

        logger.info("RequestIoPsServlet");

        String address = req.getParameter("address");

        executeGetCoins(address);

		resp.setStatus(HttpStatus.OK_200);
		resp.getWriter().println("ok");
	}


    private String executeGetCoins(String address){

        StringBuilder output = new StringBuilder();

        File file = new File(Context.getIopCoreDir());

        System.out.println("file exist: "+file.exists());
        System.out.println(file.getAbsolutePath());

        Process p;
        try {//IoP-qt -datadir="/home/mati/IoP-data/IoP-data-1"
            p = Runtime.getRuntime().exec(new String[]{"IoP-cli","-datadir="+file.getAbsolutePath(),"sendtoaddress",address, String.valueOf(1005)});
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(output.toString());

        return output.toString();

    }

}
