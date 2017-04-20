package org.fermat.internal_forum.db;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.collections.StoredEntrySet;
import com.sleepycat.collections.StoredSortedMap;
import org.fermat.internal_forum.model.Profile;


/**
 * Created by mati on 25/09/16.
 */
public class ProfilesView {

    /** public key -> profile */
    private StoredSortedMap<String,Profile> profilesMap;

    private InternalDatabaseFactory db;

    public ProfilesView(InternalDatabaseFactory databaseFactory) {

        this.db = databaseFactory;
        ClassCatalog classCatalog = databaseFactory.getProfilesDbCatalog();

        EntryBinding<String> identityKeyBinding = new SerialBinding<>(classCatalog, String.class);
        EntryBinding<Profile> identityDataBinding = new SerialBinding<>(classCatalog,Profile.class);

        profilesMap = new StoredSortedMap<>(databaseFactory.getProfileStore(),identityKeyBinding,identityDataBinding,true);

    }

    public StoredSortedMap<String,Profile> getProfilesMap() {
        return profilesMap;
    }

    public Profile getProfile(String pk){
        return profilesMap.get(pk);
    }

    public final StoredEntrySet getProfilesEntrySet(){
        return (StoredEntrySet) profilesMap.entrySet();
    }

    public InternalDatabaseFactory getDatabaseFactory() {
        return db;
    }
}
