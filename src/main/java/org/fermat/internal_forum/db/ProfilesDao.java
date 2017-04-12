package org.fermat.internal_forum.db;

import com.sleepycat.collections.TransactionRunner;
import org.fermat.internal_forum.model.Profile;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mati on 25/09/16.
 */
public class ProfilesDao {

    private InternalDatabaseFactory databaseFactory;

    private ProfilesView profilesView;

    public ProfilesDao(InternalDatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
        profilesView = new ProfilesView(databaseFactory);
    }

    public Map<String,Profile> getProfiles(){
        return new HashMap<>(profilesView.getProfilesMap());
    }

    public Profile getProfile(String pk){
        return profilesView.getProfile(pk);
    }

    public synchronized void saveProfile(Profile profile) throws CantSavePostException {
        TransactionRunner transactionRunner = new TransactionRunner(databaseFactory.getEnvironment());
        try {
            transactionRunner.run(()->profilesView.getProfilesMap().put(profile.getPk(),profile));
        } catch (Exception e) {
            throw new CantSavePostException(e);
        }
    }

    public boolean containsProfile(String pk){
        return profilesView.getProfilesMap().containsKey(pk);
    }


    public void addPushDeviceId(String profilePublicKey, String devicePushId) throws CantUpdateProfileException {
        TransactionRunner transactionRunner = new TransactionRunner(databaseFactory.getEnvironment());
        try {
            transactionRunner.run(()->profilesView.getProfilesMap().get(profilePublicKey).addDeviceId(devicePushId));
        } catch (Exception e) {
            throw new CantUpdateProfileException(e);
        }
    }
}
