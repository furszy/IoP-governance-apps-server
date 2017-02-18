package org.fermat.blockchain;

import com.google.gson.JsonArray;
import org.apache.log4j.Logger;
import org.fermat.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by mati on 28/01/17.
 */
public class IoPrpcLocalClient {

    private static final Logger logger = Logger.getLogger(IoPrpcLocalClient.class);

    public static String executeGetContracts(int blockHeight, JsonArray jsonHashes){

        StringBuilder output = new StringBuilder();

        File file = new File(Context.getIopCoreDir());

        System.out.println("file exist: "+file.exists());
        System.out.println(file.getAbsolutePath());

        Process p;
        try {//IoP-qt -datadir="/home/mati/IoP-data/IoP-data-1"
            String[] command = null;
            if (jsonHashes!=null){
                command = new String[jsonHashes.size()+4];
                command[0] = "IoP-cli";
                command[1] = "-datadir="+file.getAbsolutePath();
                command[2] = "dumpCC";
                command[3] = String.valueOf(blockHeight);
                int jsonPos = 0;
                for (int i = 4; i < command.length; i++) {
                    command[i] = jsonHashes.get(jsonPos).getAsString();
                    jsonPos++;
                }
                logger.info("command: "+ Arrays.toString(command));
            }else {
                command =  new String[]{"IoP-cli","-datadir="+file.getAbsolutePath(),"dumpCC",String.valueOf(blockHeight)};
            }
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

            p.destroyForcibly();


        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }


        return output.toString();

    }


    public static String executeGetBestBlockHash() {

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

            p.destroyForcibly();

        } catch (Exception e) {
            e.printStackTrace();
        }


        return output.toString();

    }



}
