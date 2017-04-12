package org.fermat.push_notifications;

import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.*;
import com.sleepycat.je.util.DbBackup;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Created by mati on 25/09/16.
 */
public class PushDatabaseFactory {

    /** A logger for this class */
    private final static Logger LOG = Logger.getLogger(PushDatabaseFactory.class);

    private Environment env;

    // Nombre de com.db del catalogo de objetos
    private static final String CLASS_CATALOG = "java_class_catalog_push";

    private StoreConfig storeConfig;

    /**
     * Catalogo que guarda la descripción de clases de objetos serializados para poder re armarlos luego.
     */
    private StoredClassCatalog javaCatalog;

    // database name
    private static final String PUSH_STORE = "push_store";

    // databases
    private Database pushDb;
    private EntityStore store;



    public PushDatabaseFactory(String homeDirectory) throws DatabaseException, FileNotFoundException {

        LOG.info("Opening environment in: " + homeDirectory);

//         Enviroment configuration
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
//         Database file
        env = new Environment(new File(homeDirectory), envConfig);

//         inicializacion de las com.db
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(true);
        dbConfig.setAllowCreate(true);

        pushDb = env.openDatabase(null, PUSH_STORE, dbConfig);
        javaCatalog = new StoredClassCatalog(pushDb);


//        StoreConfig storeConfig = new StoreConfig();
//
//        EnvironmentConfig envConfig2 = new EnvironmentConfig();
//        envConfig2.setAllowCreate(true);
//        envConfig2.setTransactional(true);
//        storeConfig.setTransactional(true);
//        storeConfig.setAllowCreate(true);
//
//        // Open the environment and entity store
//        env = new Environment(new File(homeDirectory), envConfig2);
//        store = new EntityStore(env, PUSH_STORE, storeConfig);


    }

    public void close() throws DatabaseException {
        LOG.info("Closing db manager..");
        pushDb.close();
        javaCatalog.close();
        if (store != null) {
            try {
                store.close();
            } catch(DatabaseException dbe) {
                System.err.println("Error closing store: " +
                        dbe.toString());
                System.exit(-1);
            }
        }
        env.close();
    }

    public Environment getEnvironment() {
        return env;
    }

    public final StoredClassCatalog getClassCatalog() {
        return javaCatalog;
    }

    public final Database getPushDb() {
        return pushDb;
    }

//    public final EntityStore getStore(){
//        return store;
//    }


    public void backupDb() throws IOException {
        // save everything to disk
        env.sync();

        // Find the file number of the last file in the previous backup
        // persistently, by either checking the backup archive, or saving
        // state in a persistent file.
        long lastFileCopiedInPrevBackup = getLastBackupNumber();

        DbBackup backupHelper = new DbBackup(env, lastFileCopiedInPrevBackup);

        // Start backup, find out what needs to be copied.
        // If multiple environment subdirectories are in use,
        // the getLogFilesInBackupSet returns the log file
        // name prefixed with the dataNNN/ directory in which
        // it resides.
        backupHelper.startBackup();
        try {
            String[] filesForBackup = backupHelper.getLogFilesInBackupSet();

            // Copy the files to archival storage.
            copyBackupFiles(filesForBackup);
            // Update our knowlege of the last file saved in the backup set,
            // so we can copy less on the next backup
            lastFileCopiedInPrevBackup = backupHelper.getLastFileInBackupSet();
            saveLastBackupFileNumber(lastFileCopiedInPrevBackup);
        }
        finally {
            // Remember to exit backup mode, or all log files won't be cleaned
            // and disk usage will bloat.
            backupHelper.endBackup();
        }
    }

    private void copyBackupFiles(String[] filesForBackup) throws IOException {
        // Sería bueno mandarlos a otro servidor y no copiarlos a otro directorio..
        File file = new File(env.getHome().getAbsolutePath()+"/backup/push_db/");
        if (!file.exists()){
            file.mkdirs();
        }
        Path dest = file.toPath();
        Path source = env.getHome().toPath();
        Files.copy(source, dest,StandardCopyOption.REPLACE_EXISTING);
    }

    private void saveLastBackupFileNumber(long lastFileCopiedInPrevBackup) throws IOException {
        File file = new File(env.getHome().getAbsolutePath()+"/push_db/backup_util.dat");
        boolean b = (!file.exists())?file.createNewFile():file.delete();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(String.valueOf(lastFileCopiedInPrevBackup));
        fileWriter.flush();
        fileWriter.close();
    }

    public long getLastBackupNumber() throws IOException {
        File file = new File(env.getHome().getAbsolutePath()+"/push_db/backup_util.dat");
        if (file.exists()){
            BufferedReader br = new BufferedReader(new FileReader(file));
            long lastBackupNumber = Long.parseLong(br.readLine());
            br.close();
            return lastBackupNumber;
        }else {
            throw new FileNotFoundException("file: "+ file.getName()+" not found");
        }
    }
}
