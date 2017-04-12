package org.fermat.internal_forum.endpoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.fermat.ArraysUtils;
import org.fermat.Context;
import org.fermat.CryptoBytes;
import org.fermat.KeyEd25519Java;
import org.fermat.forum.ResponseMessageConstants;
import org.fermat.internal_forum.db.CantSavePostException;
import org.fermat.internal_forum.db.PostDao;
import org.fermat.internal_forum.endpoints.base.AuthEndpoint;
import org.fermat.internal_forum.model.Topic;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.fermat.blockchain.IoPrpcLocalClient.executeGetBestBlockHash;
import static org.fermat.blockchain.IoPrpcLocalClient.executeGetContracts;
import static org.fermat.forum.ResponseMessageConstants.BEST_CHAIN_HEIGHT_HASH;
import static org.fermat.forum.ResponseMessageConstants.ERROR_DETAIL;
import static org.fermat.forum.ResponseMessageConstants.TOPIC;
import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.*;

public class RequestCCServlet extends AuthEndpoint {

	private static final Logger logger = Logger.getLogger(RequestCCServlet.class);

	private PostDao postDao;

	public RequestCCServlet() {
		postDao = Context.getPostDao();
	}

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setStatus(HttpStatus.OK_200);
		resp.getWriter().println("EmbeddedJetty");
	}

	@Override
	public JsonObject doPost(HttpServletRequest req, HttpServletResponse resp, String profilePublicKey,JsonObject msg) {
		JsonObject responseObj = new JsonObject();
		int blockHeight = 0;
		JsonArray jsonHashes = null;
		if (msg.has("blockHeight")) {
			blockHeight = msg.get("blockHeight").getAsInt();
		}
		if (msg.has("hashes")) {
			try {
				jsonHashes = msg.get("hashes").getAsJsonArray();
				logger.info("json hashes: "+jsonHashes.toString());
			}catch (Exception e){
				logger.error("### "+msg.get("hashes"));
				e.printStackTrace();
			}
		}
		String output = null;
		try {
			logger.info("block height: "+blockHeight+", hashes: "+ ((jsonHashes!=null)?jsonHashes.toString():"null"));
			output = executeGetContracts(blockHeight,jsonHashes);
			String bestBlockHash = executeGetBestBlockHash();
			if (output != null && !output.equals("")) {
				JsonObject jsonObject = (JsonObject) new JsonParser().parse(output);
				jsonObject.addProperty(BEST_CHAIN_HEIGHT_HASH, bestBlockHash);
				resp.setStatus(HttpStatus.OK_200);
				responseObj = jsonObject;
			} else {
				//todo testear esto.
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(BEST_CHAIN_HEIGHT_HASH, bestBlockHash);
				resp.setStatus(HttpStatus.OK_200);
				responseObj = msg;
			}
		}catch (Exception e){
			logger.error("output returned from core value: "+output,e);
			resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
			responseObj.addProperty(ERROR_DETAIL,"server error: "+e.getMessage());
		}
		return responseObj;
	}

}
