package org.fermat.internal_forum.db;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.*;
import com.sleepycat.je.util.DbBackup;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.impl.Store;
import com.sleepycat.persist.model.EntityModel;
import org.apache.log4j.Logger;
import org.fermat.internal_forum.model.Profile;
import org.fermat.internal_forum.model.Topic;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 * Created by mati on 25/09/16.
 */
public class InternalDatabaseFactory {

    /** A logger for this class */
    private final static Logger LOG = Logger.getLogger(InternalDatabaseFactory.class);

    private Environment env;

    // Nombre de com.db del catalogo de objetos
    private static final String CLASS_CATALOG = "java_class_catalog_internal";

    private StoreConfig storeConfig;

    /**
     * Catalogo que guarda la descripción de clases de objetos serializados para poder re armarlos luego.
     */
    private StoredClassCatalog javaCatalog;
    private StoredClassCatalog profilesDbCatalog;

    // database name
    private static final String FORUM_STORE = "forum_store";
    private static final String PROFILE_STORE = "profile_store";

    // databases
    private Database forumDb;
//    private Database profilesDb;

    private EntityStore topicsStore;
    private EntityStore profilesStore;



    public InternalDatabaseFactory(String homeDirectory) throws DatabaseException, FileNotFoundException {

        LOG.info("Opening environment in: " + homeDirectory);

        // Enviroment configuration
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
        // Database file
        env = new Environment(new File(homeDirectory), envConfig);

        // inicializacion de las com.db
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(true);
        dbConfig.setAllowCreate(true);

//        forumDb = env.openDatabase(null, FORUM_STORE, dbConfig);
        //profilesDb = env.openDatabase(null,PROFILE_STORE,dbConfig);

//        javaCatalog = new StoredClassCatalog(forumDb);
//        profilesDbCatalog = new StoredClassCatalog(profilesDb);

        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setReadOnly(false);
        storeConfig.setTransactional(true);
        storeConfig.setAllowCreate(true);
        profilesStore = new EntityStore(env,PROFILE_STORE,storeConfig);

        topicsStore = new EntityStore(env,FORUM_STORE,storeConfig);
    }

    public void close() throws DatabaseException {
        LOG.info("Closing db manager..");
        if (forumDb!=null)
            forumDb.close();
//        if (profilesDb!=null)
//            profilesDb.close();
        javaCatalog.close();
        if (profilesDbCatalog!=null)
            profilesDbCatalog.close();
        profilesStore.close();
        topicsStore.close();
        env.close();
    }

    public Environment getEnvironment() {
        return env;
    }

    public final StoredClassCatalog getClassCatalog() {
        return javaCatalog;
    }

    public final Database getForumDb() {
        return forumDb;
    }

    public final EntityStore getProfileStore() {
        return profilesStore;
    }

//    public final Database getProfileDb() {
//        return profilesDb;
//    }
    public final EntityStore getTopicsStore(){
        return topicsStore;
    }

    public ClassCatalog getProfilesDbCatalog() {
        return profilesDbCatalog;
    }

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
        File file = new File(env.getHome().getAbsolutePath()+"/backup");
        if (!file.exists()){
            file.mkdirs();
        }
        Path dest = file.toPath();
        Path source = env.getHome().toPath();
        Files.copy(source, dest,StandardCopyOption.REPLACE_EXISTING);
    }

    private void saveLastBackupFileNumber(long lastFileCopiedInPrevBackup) throws IOException {
        File file = new File(env.getHome().getAbsolutePath()+"/backup_util.dat");
        boolean b = (!file.exists())?file.createNewFile():file.delete();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(String.valueOf(lastFileCopiedInPrevBackup));
        fileWriter.flush();
        fileWriter.close();
    }

    public long getLastBackupNumber() throws IOException {
        File file = new File(env.getHome().getAbsolutePath()+"/backup_util.dat");
        if (file.exists()){
            BufferedReader br = new BufferedReader(new FileReader(file));
            long lastBackupNumber = Long.parseLong(br.readLine());
            br.close();
            return lastBackupNumber;
        }else {
            return 0;
        }
    }
}
