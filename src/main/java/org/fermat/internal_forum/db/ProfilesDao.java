package org.fermat.internal_forum.db;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Put;
import com.sleepycat.je.WriteOptions;
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

    public boolean updateProfile(String profilePublicKey, String name) throws ProfileNotFoundException, CantUpdateProfileException {
        Profile profile = getProfile(profilePublicKey);
        if (profile!=null) {
            profile.setName(name);
            try {
                EntryBinding<String> keyBinding = new SerialBinding<>(databaseFactory.getClassCatalog(), String.class);
                EntryBinding<Profile> dataBinding = new SerialBinding<>(databaseFactory.getClassCatalog(), Profile.class);

                DatabaseEntry theKey = new DatabaseEntry();
                DatabaseEntry theData = new DatabaseEntry();

                keyBinding.objectToEntry(profilePublicKey, theKey);
                dataBinding.objectToEntry(profile, theData);

                WriteOptions wo = new WriteOptions();
                // This sets the TTL using day units. Another variation
                // of setTTL() exists that accepts a TimeUnit class instance.
                wo.setTTL(5);
                // If the record currently exists, update the TTL value
                wo.setUpdateTTL(true);
                profilesView.getDatabaseFactory().getForumDb().put(
                        null,             // Transaction handle.
                        theKey,           // Record's key.
                        theData,          // Record's data.
                        Put.OVERWRITE,    // If the record exists,
                        // overwrite it.
                        wo);              // WriteOptions instance.

                return true;
            } catch (Exception e) {
                // Exception handling goes here
                throw new CantUpdateProfileException(e);
            }
        }else{
            throw new ProfileNotFoundException("Profile not found, pk: "+profilePublicKey);
        }
    }
}
