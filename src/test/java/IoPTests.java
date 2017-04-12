import com.google.gson.JsonArray;
import org.fermat.Context;
import org.fermat.forum.ForumClient;
import org.fermat.forum.ForumClientDiscourseImp;
import org.fermat.forum.discourse.DiscourseApiClient;
import org.fermat.forum.discourse.DiscouseApiConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mati on 15/12/16.
 */
public class IoPTests {


    @Test
    public void executeGetContracts(){

        StringBuilder output = new StringBuilder();

        File file = new File("/home/mati/IoP-data/IoP-data-1");

        System.out.println("file exist: "+file.exists());
        System.out.println(file.getAbsolutePath());

        Process p;
        try {//IoP-qt -datadir="/home/mati/IoP-data/IoP-data-1"
            p = Runtime.getRuntime().exec(new String[]{"IoP-cli","-datadir="+file.getAbsolutePath(),"getproposalcontracts"});
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



        JSONObject jsonObject = new JSONObject(output.toString());
        JSONArray transactions = jsonObject.getJSONArray("transactions");
        System.out.println("##############");
        System.out.println(transactions.toString());
//        return output.toString();
    }


    @Test
    public void profileServerConnectionTest(){

        try {
            Socket socket = new Socket("localhost",16987);

            if (socket.isConnected()){
                System.out.println("Se conectó");
            }else {
                System.out.println("No se conectó");
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Test
    public void getTopic(){

        try{
            ForumClient forumClient;
            forumClient = new ForumClientDiscourseImp(
                    Context.getForumUrl(),
                    Context.getApiKey(),
                    Context.getAdminUsername()
            );
            String topic = forumClient.getTopic(869);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
