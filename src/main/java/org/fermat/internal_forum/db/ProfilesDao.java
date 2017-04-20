package org.fermat.internal_forum.db;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Put;
import com.sleepycat.je.WriteOptions;
import org.fermat.db.exceptions.CantSaveIdentityException;
import org.fermat.internal_forum.model.Profile;
import org.fermat.internal_forum.model.Topic;

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

//    public Profile getProfile(String pk){
//        return profilesView.getProfile(pk);
//    }

    public synchronized void saveProfile(Profile profile) throws CantSaveIdentityException {
        try {
            saveProfile(profile,false);
        } catch (Exception e) {
            throw new CantSaveIdentityException("cant save identity",e);
        }
    }

    public boolean containsProfile(String pk){
        try {
            return getProfile(pk)!=null;
        } catch (ProfileNotFoundException e) {
            return false;
        }
    }


    public void addPushDeviceId(String profilePublicKey, String devicePushId) throws CantUpdateProfileException, CantSaveIdentityException, ProfileNotFoundException {
        Profile data = getProfile(profilePublicKey);
        if (data!=null) {
            data.addDeviceId(devicePushId);
            saveProfile(data);
        }else
            throw new ProfileNotFoundException("profile not found with pk "+profilePublicKey);
    }

    public boolean saveProfile(Profile profile,boolean updateIfExist) throws CantSaveIdentityException {
        if (profile.getPk()==null || profile.getPk().length()<10) throw new IllegalArgumentException("Invalid profile pk, pk"+profile.getPk());
        try {
            DatabaseEntry theKey = buildEntryRecord(profile.getPk(),String.class);
            DatabaseEntry theData = buildEntryRecord(profile,Profile.class);

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
                    (updateIfExist)?Put.OVERWRITE:Put.NO_OVERWRITE,    // If the record exists,
                    // overwrite it.
                    wo);              // WriteOptions instance.

            return true;
        } catch (Exception e) {
            // Exception handling goes here
            throw new CantSaveIdentityException("Cant save profile",e);
        }
    }

    public Profile getProfile(String pk) throws ProfileNotFoundException {
        DatabaseEntry theKey = buildEntryRecord(pk,String.class);
        DatabaseEntry valueDatabaseEntry = new DatabaseEntry();
        OperationStatus op = databaseFactory.getForumDb().get(null, theKey, valueDatabaseEntry, null);
        if (op == OperationStatus.SUCCESS) {
            Profile profile =  buildFromEntryRecord(valueDatabaseEntry,Profile.class);
            return profile;
        }else {
            throw new ProfileNotFoundException("profile not found, with pk "+pk);
        }
    }


    public boolean updateProfile(String profilePublicKey, String name) throws ProfileNotFoundException, CantUpdateProfileException {
        Profile profile = getProfile(profilePublicKey);
        if (profile!=null) {
            profile.setName(name);
            try {
                return saveProfile(profile,true);
            } catch (Exception e) {
                // Exception handling goes here
                throw new CantUpdateProfileException(e);
            }
        }else{
            throw new ProfileNotFoundException("Profile not found, pk: "+profilePublicKey);
        }
    }

    private <T> DatabaseEntry buildEntryRecord(T obj,Class<T> objType){
        EntryBinding<T> keyBinding = new SerialBinding<>(databaseFactory.getClassCatalog(), objType);
        DatabaseEntry theKey = new DatabaseEntry();
        keyBinding.objectToEntry(obj, theKey);
        return theKey;
    }

    private <T> T buildFromEntryRecord(DatabaseEntry databaseEntry,Class<T> clazz){
        EntryBinding<T> identityKeyBinding = new SerialBinding<>(databaseFactory.getClassCatalog(), clazz);
        return identityKeyBinding.entryToObject(databaseEntry);
    }
}
