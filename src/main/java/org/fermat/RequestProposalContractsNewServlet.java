
package org.fermat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;

import static org.fermat.blockchain.IoPrpcLocalClient.executeGetBestBlockHash;
import static org.fermat.blockchain.IoPrpcLocalClient.executeGetContracts;
import static org.fermat.forum.ResponseMessageConstants.BEST_CHAIN_HEIGHT_HASH;

public class RequestProposalContractsNewServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(RequestProposalContractsNewServlet.class);


    public RequestProposalContractsNewServlet() {
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String ipAddress = req.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = req.getRemoteAddr();
        }

        logger.info("RequestProposalContractsNewServlet, client ip: "+ipAddress);

        int blockHeight = 0;
        JsonArray jsonHashes = null;

        StringBuffer jb = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = req.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) { /*report an error*/ }

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonElement = (JsonObject) jsonParser.parse(jb.toString());

        if (jb.toString()!=null && !jb.toString().equals("")) {

            if (jsonElement.get("blockHeight")!=null) {
                blockHeight = jsonElement.get("blockHeight").getAsInt();
            }

            if (jsonElement.get("hashes")!=null) {
                try {
                    jsonHashes = jsonElement.get("hashes").getAsJsonArray();
                    logger.info("json hashes: "+jsonHashes.toString());
                }catch (Exception e){
                    logger.error("### "+jsonElement.get("hashes"));
                    e.printStackTrace();
                }

            }
        }

        String output = null;
        try {
            logger.info("block height: "+blockHeight+", hashes: "+ ((jsonHashes!=null)?jsonHashes.toString():"null"));
            output = executeGetContracts(blockHeight,jsonHashes);
            String bestBlockHash = executeGetBestBlockHash();
            if (output != null && !output.equals("")) {
                JSONObject jsonObject = new JSONObject(output);
                jsonObject.put(BEST_CHAIN_HEIGHT_HASH, bestBlockHash);

                resp.setStatus(HttpStatus.OK_200);
                resp.getWriter().println(jsonObject.toString());
            } else {

                //todo testear esto.
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(BEST_CHAIN_HEIGHT_HASH, bestBlockHash);

                resp.setStatus(HttpStatus.OK_200);
                resp.getWriter().println(jsonObject.toString());
//
//                resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
//                resp.getWriter().println("fail: getContract null");
//                logger.error("fail: getContract null");
            }
        }catch (Exception e){
            logger.error("output returned from core value: "+output,e);
            resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            resp.getWriter().println("fail:"+e.getMessage());
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int blockHeight = 0;
        JsonArray jsonHashes = null;

        logger.info("RequestProposalContractsNewServlet");

        StringBuffer jb = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = req.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) {
            /*report an error*/
            logger.error(e);
        }

        JsonParser jsonParser = new JsonParser();
        if (jb.toString()!=null && !jb.toString().equals("")) {

            logger.info(jb.toString());

            JsonObject jsonElement = (JsonObject) jsonParser.parse(jb.toString());

            blockHeight = jsonElement.get("blockHeight").getAsInt();


            jsonHashes = jsonElement.get("hashes").getAsJsonArray();

        }else{
            String hashesJson = req.getParameter("hashes");
            if (hashesJson != null && !hashesJson.equals("")) {
                //logger.info("jsonHahes"+hashesJson);
                JsonObject jsonElement = (JsonObject) jsonParser.parse(hashesJson);
                jsonHashes = jsonElement.get("hashes").getAsJsonArray();
            }
        }

        if (jsonHashes!=null)
            logger.info("hashes: "+jsonHashes.toString());

        try {
            String output = executeGetContracts(blockHeight,jsonHashes);
            if (output != null && !output.equals("")) {

//                System.out.println("##############");
//                System.out.println("output: "+output);

                String bestBlockHash = executeGetBestBlockHash();

                JSONObject jsonObject = new JSONObject(output);
                jsonObject.put(BEST_CHAIN_HEIGHT_HASH, bestBlockHash);

                resp.setStatus(HttpStatus.OK_200);
                resp.getWriter().println(jsonObject.toString());
            } else {
                logger.info("executeGetContracts return empty..");
                resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                resp.getWriter().println("IoP core node is not active");
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            resp.getWriter().println("Fail: "+e.getMessage());
        }
    }






    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}
