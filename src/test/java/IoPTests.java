import com.google.gson.JsonArray;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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

}
