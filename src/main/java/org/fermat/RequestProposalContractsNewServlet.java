
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

import static org.fermat.forum.ResponseMessageConstants.BEST_CHAIN_HEIGHT_HASH;

public class RequestProposalContractsNewServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(RequestProposalContractsNewServlet.class);


    public RequestProposalContractsNewServlet() {
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        logger.info("RequestProposalContractsNewServlet");

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
                }catch (Exception e){
                    logger.error("### "+jsonElement.get("hashes"));
                    e.printStackTrace();
                }

            }
        }



        try {
            String output = executeGetContracts(blockHeight,jsonHashes);
            if (output != null) {
//            JSONObject jsonObject = new JSONObject(output.toString());
//            JSONArray transactions = jsonObject.getJSONArray("transactions");
                System.out.println("##############");
                System.out.println("output: "+output);

                String bestBlockHash = executeGetBestBlockHash();

                JSONObject jsonObject = new JSONObject(output);
                jsonObject.put(BEST_CHAIN_HEIGHT_HASH, bestBlockHash);

                resp.setStatus(HttpStatus.OK_200);
                resp.getWriter().println(jsonObject.toString());
            } else {
                resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                resp.getWriter().println("fail");
            }
        }catch (Exception e){
            e.printStackTrace();
            resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            resp.getWriter().println("fail");
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
        } catch (Exception e) { /*report an error*/ }

        JsonParser jsonParser = new JsonParser();
        if (jb.toString()!=null && !jb.toString().equals("")) {

            JsonObject jsonElement = (JsonObject) jsonParser.parse(jb.toString());

            blockHeight = jsonElement.get("blockHeight").getAsInt();


            jsonHashes = jsonElement.get("hashes").getAsJsonArray();

        }else{
            String hashesJson = req.getParameter("hashes");
            if (hashesJson != null && !hashesJson.equals("")) {
                logger.info("jsonHahes"+hashesJson);
                JsonObject jsonElement = (JsonObject) jsonParser.parse(hashesJson);
                jsonHashes = jsonElement.get("hashes").getAsJsonArray();
            }
        }

        logger.info("hashes: "+jsonHashes.toString());

        try {
            String output = executeGetContracts(blockHeight,jsonHashes);
            if (output != null) {
//            JSONObject jsonObject = new JSONObject(output.toString());
//            JSONArray transactions = jsonObject.getJSONArray("transactions");
                System.out.println("##############");
                System.out.println("output: "+output);

                String bestBlockHash = executeGetBestBlockHash();

                JSONObject jsonObject = new JSONObject(output);
                jsonObject.put(BEST_CHAIN_HEIGHT_HASH, bestBlockHash);

                resp.setStatus(HttpStatus.OK_200);
                resp.getWriter().println(jsonObject.toString());
            } else {
                resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                resp.getWriter().println("fail");
            }
        }catch (Exception e){
            e.printStackTrace();
            resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            resp.getWriter().println("fail");
        }
    }

    private String executeGetBestBlockHash() {

        StringBuilder output = new StringBuilder();

        File file = new File(Context.getIopCoreDir());

        System.out.println("file exist: "+file.exists());
        System.out.println(file.getAbsolutePath());

        Process p;
        try {//IoP-qt -datadir="/home/mati/IoP-data/IoP-data-1"
            p = Runtime.getRuntime().exec(new String[]{"IoP-cli","-datadir="+file.getAbsolutePath(),"getbestblockhash"});
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


        return output.toString();

    }


    private String executeGetContracts(int blockHeight, JsonArray jsonHashes){

        StringBuilder output = new StringBuilder();

        File file = new File(Context.getIopCoreDir());

        System.out.println("file exist: "+file.exists());
        System.out.println(file.getAbsolutePath());

        Process p;
        try {//IoP-qt -datadir="/home/mati/IoP-data/IoP-data-1"
            String[] command = null;
            if (jsonHashes!=null){

                command = new String[jsonHashes.size()+3];
                command[0] = "IoP-cli";
                command[1] = "-datadir="+file.getAbsolutePath();
                command[2] = "dumpCC";
                int jsonPos = 0;
                for (int i = 3; i < command.length; i++) {
                    command[i] = jsonHashes.get(jsonPos).getAsString();
                    jsonPos++;
                }
                logger.info("command: "+ Arrays.toString(command));
            }else {
                command =  new String[]{"IoP-cli","-datadir="+file.getAbsolutePath(),"dumpCC"};
            }
            p = Runtime.getRuntime().exec(command);
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


        return output.toString();

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
