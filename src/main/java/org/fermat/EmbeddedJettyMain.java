package org.fermat;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.fermat.conf.ServerConf;
import org.fermat.extra_data.ExtraData;
import org.fermat.extra_data.MarketCapApiClient;
import org.fermat.internal_forum.endpoints.*;
import org.fermat.push_notifications.Firebase;
import org.fermat.push_notifications.SuscriptionType;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.fermat.Context.EXTRA_DATA_FILENAME;


public class EmbeddedJettyMain {


	public static void main(String[] args) throws Exception {

		// logger

		ServerConf serverConf = new ServerConf();
		serverConf.configLogger();

		Context.init();

		// creates a custom logger and log messages
		Logger logger = Logger.getLogger(EmbeddedJettyMain.class);

		logger.info("INIT args: "+ Arrays.toString(args));

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

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				ExtraData extraData = Context.getExtraData();
				BigDecimal bigDecimal = null;
				try {
					bigDecimal = new MarketCapApiClient().getIoPPrice();
				} catch (Exception e) {
					e.printStackTrace();
				}
				extraData = new ExtraData(extraData.getMonthRateIoP().add(bigDecimal).divide(new BigDecimal(2),RoundingMode.CEILING));
				extraData.saveExtraData(new File(EXTRA_DATA_FILENAME));

			}
		},1,12, TimeUnit.HOURS);
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					Context.getInternalDb().backupDb();
				} catch (IOException e) {
					logger.error("Cant backup database",e);
				}
			}
		},1,12,TimeUnit.HOURS);


		Server server = new Server(port);
        ServletContextHandler handler = new ServletContextHandler(server, "/fermat");
		handler.addServlet(ExampleServlet.class, "/");
		handler.addServlet(ExampleServlet.class, "/admin_notif");
		handler.addServlet(RegisterUserServlet.class, "/register");
		handler.addServlet(RequestKeyServlet.class,"/requestkey");
		handler.addServlet(RequestTopicServlet.class,"/getTopic");
        handler.addServlet(RequestProposalContractsNewServlet.class,"/requestproposalsnew");
		handler.addServlet(RequestIoPsServlet.class,"/requestcoins");
		handler.addServlet(RequestIopRateUsdServlet.class,"/request_iop_usd_rate_month");

		// forum servlets
		handler.addServlet(RequestRegisterProfileServlet.class,"/profile");
		handler.addServlet(RequestCreateTopicServlet.class,"/create_topic");
		handler.addServlet(RequestCreatePostServlet.class,"/create_post");
		handler.addServlet(org.fermat.internal_forum.endpoints.RequestTopicServlet.class,"/topic");
		handler.addServlet(RequestTopicsServlet.class,"/topics");
		handler.addServlet(RequestCCServlet.class,"/request_cc");
		handler.addServlet(RequestCommentsServlet.class,"/comments");
		handler.addServlet(RegisterPushIdServlet.class,"/reg_id");
		handler.addServlet(SubscribePushTopicServlet.class,"/subcribe");

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