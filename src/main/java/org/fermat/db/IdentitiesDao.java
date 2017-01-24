package org.fermat.db;

import com.sleepycat.collections.TransactionRunner;
import org.fermat.db.exceptions.CantSaveIdentityException;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by mati on 25/09/16.
 */
public class IdentitiesDao {

    private DatabaseFactory databaseFactory;

    private IdentityView identityView;

    public IdentitiesDao(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
        identityView = new IdentityView(databaseFactory);
    }

    public Map<IdentityKey,IdentityData> getIdentities(){
        return new HashMap<>(identityView.getIdentityMap());
    }

    public IdentityData getIdentity(String name){
        return identityView.getIdentityMap().get(new IdentityKey(name));
    }

    public void saveIdentity(IdentityData identity) throws CantSaveIdentityException {
        TransactionRunner transactionRunner = new TransactionRunner(databaseFactory.getEnvironment());
        try {
            transactionRunner.run(()->identityView.getIdentityMap().put(new IdentityKey(identity.getName()),identity));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param name is the hash of the identity's public key
     * @return
     */
    public boolean isIdentityOnline(String name){
        return identityView.getIdentityMap().containsKey(new IdentityKey(name));
    }

}
