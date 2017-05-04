package org.fermat.internal_forum.db;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.je.*;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.impl.Store;
import org.fermat.db.exceptions.CantSaveIdentityException;
import org.fermat.internal_forum.model.Profile;
import org.fermat.internal_forum.model.Topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mati on 25/09/16.
 */
public class ProfilesDao {

    private InternalDatabaseFactory databaseFactory;

    public ProfilesDao(InternalDatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }


    public List<Profile> getProfiles(){
        List<Profile> profiles = new ArrayList<>();
        EntityStore store = databaseFactory.getProfileStore();
        PrimaryIndex<String,Profile> pi = store.getPrimaryIndex(String.class,Profile.class);

        pi.entities().forEach(profile -> {
            profiles.add(profile);
        });
        return profiles;
    }

    public synchronized boolean saveProfile(Profile profile) throws CantSaveIdentityException {
        try {
            return saveProfile(profile,false);
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
        boolean res = false;
        try{
            EntityStore store = databaseFactory.getProfileStore();
            PrimaryIndex<String,Profile> pi = store.getPrimaryIndex(String.class,Profile.class);
            if (updateIfExist){
                pi.put(profile);
                res = true;
            }else {
                res = pi.putNoOverwrite(profile);
            }

        }catch (Exception e){
            // Exception handling goes here
            throw new CantSaveIdentityException("Cant save profile",e);
        }
        return res;
    }

    public Profile getProfile(String pk) throws ProfileNotFoundException {
        EntityStore store = databaseFactory.getProfileStore();
        PrimaryIndex<String,Profile> pi = store.getPrimaryIndex(String.class,Profile.class);
        return pi.get(pk);
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

    public void dropDatabase(){
        databaseFactory.getProfileStore().truncateClass(Profile.class);
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
