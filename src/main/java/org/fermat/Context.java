package org.fermat;

import org.fermat.db.DatabaseFactory;
import org.fermat.db.IdentitiesDao;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by mati on 16/12/16.
 */
public class Context {

    private static IdentitiesDao dao;

    private static String iopCoreDir = "/home/mati/IoP-data/IoP-data-1";

    public static void init() {
        try {
            File file = new File("");
            DatabaseFactory databaseFactory = new DatabaseFactory(file.getAbsolutePath());
            dao = new IdentitiesDao(databaseFactory);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static IdentitiesDao getDao() {
        return dao;
    }

    public static void setIopCoreDir(String iopCoreDir) {
        Context.iopCoreDir = iopCoreDir;
    }

    public static String getIopCoreDir() {
        return iopCoreDir;
    }
}
