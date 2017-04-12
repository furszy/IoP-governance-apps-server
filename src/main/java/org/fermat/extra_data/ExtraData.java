package org.fermat.extra_data;

import java.io.*;
import java.math.BigDecimal;

/**
 * Created by mati on 20/03/17.
 */
public class ExtraData implements Serializable{

    private BigDecimal monthRateIoP;

    public ExtraData(BigDecimal monthRateIoP) {
        this.monthRateIoP = monthRateIoP;
    }

    public BigDecimal getMonthRateIoP() {
        return monthRateIoP;
    }

    public void setMonthRateIoP(BigDecimal monthRateIoP) {
        this.monthRateIoP = monthRateIoP;
    }

    public void saveExtraData(File file){
        if (file.exists()){
            file.delete();
        }
        try {
            file.createNewFile();

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);

            fileOutputStream.close();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static ExtraData loadExtraData(File file){
        ExtraData extraData = null;
        if (file.exists()){

            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
                extraData = (ExtraData) objectInputStream.readObject();
                objectInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        return extraData;
    }

}
