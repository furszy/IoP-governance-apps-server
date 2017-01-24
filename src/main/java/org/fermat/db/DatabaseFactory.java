package org.fermat.db;

import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by mati on 25/09/16.
 */
public class DatabaseFactory {

    /** A logger for this class */
    private final static Logger LOG = Logger.getLogger(DatabaseFactory.class);

    private Environment env;

    // Nombre de com.db del catalogo de objetos
    private static final String CLASS_CATALOG = "java_class_catalog";

    /**
     * Catalogo que guarda la descripción de clases de objetos serializados para poder re armarlos luego.
     */
    private StoredClassCatalog javaCatalog;

    // database name
    private static final String IDENTITY_STORE = "identity_store";

    // databases
    private Database identityDb;


    public DatabaseFactory(String homeDirectory) throws DatabaseException, FileNotFoundException {

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

        identityDb = env.openDatabase(null, IDENTITY_STORE, dbConfig);

        javaCatalog = new StoredClassCatalog(identityDb);

    }

    public void close() throws DatabaseException {
        LOG.info("Closing db manager..");
        identityDb.close();
        javaCatalog.close();
        env.close();
    }

    public Environment getEnvironment() {
        return env;
    }

    public final StoredClassCatalog getClassCatalog() {
        return javaCatalog;
    }

    public final Database getIdentityDb() {
        return identityDb;
    }
}
