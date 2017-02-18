package org.fermat;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.fermat.conf.ServerConf;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class EmbeddedJettyMain {


	public static void main(String[] args) throws Exception {

		// logger

		ServerConf serverConf = new ServerConf();
		serverConf.configLogger();

		Context.init();

		// creates a custom logger and log messages
		Logger logger = Logger.getLogger(EmbeddedJettyMain.class);

		logger.info("INIT");



		// args

		int port = -1;
		String iopDir = null;

		for (int i = 0; i < args.length; i=i+2) {

			if (args[i].equals("-port")){
				port = Integer.parseInt(args[i+1]);
			}

			if (args[i].equals("-datadir")){
				iopDir = args[i+1];
			}

			if (args[i].equals("-url")){
				Context.setForumUrl(args[i+1]);
			}

			if (args[i].equals("-apikey")){
				Context.setApiKey(args[i+1]);
			}

			if (args[i].equals("-admin")){
				Context.setAdminUsername(args[i+1]);
			}

		}

		if (port==-1){
			port = 7070;
		}

		if (iopDir!=null && !iopDir.equals("")){
			logger.info("IoP datadir = "+iopDir);
			Context.setIopCoreDir(iopDir);
		}

		try{
			URL url = new URL(Context.getForumUrl());
		}catch (Exception e){
			logger.error("bad url forum, "+Context.getForumUrl());
			System.exit(1);
		}

//		executor = Executors.newSingleThreadScheduledExecutor();
//		executor.scheduleAtFixedRate(new Runnable() {
//			@Override
//			public void run() {
//				executeGenerate();
//			}
//		},5,5, TimeUnit.MINUTES);





		Server server = new Server(port);
        ServletContextHandler handler = new ServletContextHandler(server, "/fermat");
		handler.addServlet(ExampleServlet.class, "/");
		handler.addServlet(ExampleServlet.class, "/admin_notif");
		handler.addServlet(RegisterUserServlet.class, "/register");
		handler.addServlet(RequestKeyServlet.class,"/requestkey");
		handler.addServlet(RequestTopicServlet.class,"/getTopic");
		handler.addServlet(RequestProposalContractsServlet.class,"/requestproposals");
		handler.addServlet(RequestProposalContractsFullTxServlet.class,"/requestproposalsfulltx");
        handler.addServlet(RequestProposalContractsNewServlet.class,"/requestproposalsnew");
		handler.addServlet(RequestIoPsServlet.class,"/requestcoins");

		server.start();

		server.join();


	}

	private static void executeGenerate(){

		StringBuilder output = new StringBuilder();

		File file = new File(Context.getIopCoreDir());

		System.out.println("file exist: "+file.exists());
		System.out.println(file.getAbsolutePath());

		Process p;
		try {//IoP-qt -datadir="/home/mati/IoP-data/IoP-data-1"
			p = Runtime.getRuntime().exec(new String[]{"IoP-cli","-datadir="+file.getAbsolutePath(),"generate",String.valueOf(1)});
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

	}

}