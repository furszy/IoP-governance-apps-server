package org.fermat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.fermat.forum.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.fermat.forum.ResponseMessageConstants.BEST_CHAIN_HEIGHT_HASH;

public class RequestProposalContractsServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(RequestProposalContractsServlet.class);


    public RequestProposalContractsServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int blockHeight = 0;

        logger.info("RequestProposalContractsServlet");

        StringBuffer jb = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = req.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) { /*report an error*/ }

        if (jb.toString()!=null && !jb.toString().equals("")) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonElement = (JsonObject) jsonParser.parse(jb.toString());

            blockHeight = jsonElement.get("blockHeight").getAsInt();
        }

        try {
            String output = executeGetContracts(blockHeight);
            if (output != null) {
//            JSONObject jsonObject = new JSONObject(output.toString());
//            JSONArray transactions = jsonObject.getJSONArray("transactions");
                System.out.println("##############");
                System.out.println(output);

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


    private String executeGetContracts(int blockHeight){

        StringBuilder output = new StringBuilder();

        File file = new File(Context.getIopCoreDir());

        System.out.println("file exist: "+file.exists());
        System.out.println(file.getAbsolutePath());

        Process p;
        try {//IoP-qt -datadir="/home/mati/IoP-data/IoP-data-1"
            p = Runtime.getRuntime().exec(new String[]{"IoP-cli","-datadir="+file.getAbsolutePath(),"getproposalcontracts", String.valueOf(blockHeight)});
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
