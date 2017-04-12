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
    private Database profilesDb;



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

        forumDb = env.openDatabase(null, FORUM_STORE, dbConfig);
        profilesDb = env.openDatabase(null,PROFILE_STORE,dbConfig);

        javaCatalog = new StoredClassCatalog(forumDb);
        profilesDbCatalog = new StoredClassCatalog(profilesDb);


//        try {
//            EntryBinding<PostKey> identityKeyBinding = new SerialBinding<>(javaCatalog, PostKey.class);
//            EntryBinding<Topic> identityDataBinding = new SerialBinding<>(javaCatalog, Topic.class);
//
//
//            PostKey postKey = new PostKey(2);
//            Topic topic = new Topic("rqw dsadaa", "titusada lo", "dwadwa", new ArrayList<>(), "raw2", null);
//            topic.setId(2);
//
//            DatabaseEntry keyDatabaseEntry = new DatabaseEntry();
//            DatabaseEntry valueDatabaseEntry = new DatabaseEntry();
//            identityKeyBinding.objectToEntry(postKey, keyDatabaseEntry);
//            identityDataBinding.objectToEntry(topic, valueDatabaseEntry);
//        OperationStatus operationStatus = forumDb.put(null, keyDatabaseEntry, valueDatabaseEntry);

//        LOG.info("value saved: op_status -> " + operationStatus.toString());

//        if (operationStatus == OperationStatus.SUCCESS) {
//            DatabaseEntry keyDatabaseEntry2 = new DatabaseEntry();
//            DatabaseEntry valueDatabaseEntry2 = new DatabaseEntry();
//            OperationStatus op = forumDb.get(null, keyDatabaseEntry, valueDatabaseEntry2, null);
//            if (op == OperationStatus.SUCCESS) {
//                LOG.info("value retrieved: op_status -> " + op.toString());
//                LOG.info("object: " + identityDataBinding.entryToObject(valueDatabaseEntry2).toString());
//            }
//        }
//
//            DatabaseEntry databaseEntry = new DatabaseEntry();
//            DatabaseEntry valueDb = new DatabaseEntry();
//            Cursor cursor = forumDb.openCursor(null, null);
//            while (cursor.getNext(databaseEntry, valueDb, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
////            LOG.info("key: " + identityKeyBinding.entryToObject(databaseEntry).toString());
//                try {
//                    if (valueDb.getSize() > 1)
//                        LOG.info("object: " + identityDataBinding.entryToObject(valueDb).toString());
//                    else
//                        LOG.error("fail");
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//
//            cursor.close();



//        storeConfig = new StoreConfig();
//        storeConfig.setReadOnly(true);

//        }catch (Exception e){
//            e.printStackTrace();
//        }

    }

    public void close() throws DatabaseException {
        LOG.info("Closing db manager..");
        forumDb.close();
        profilesDb.close();
        javaCatalog.close();
        profilesDbCatalog.close();
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

    public final Database getProfileStore() {
        return profilesDb;
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
