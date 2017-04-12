package org.fermat.internal_forum.db;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.collections.StoredEntrySet;
import com.sleepycat.collections.StoredSortedMap;
import org.fermat.internal_forum.model.Topic;


/**
 * Created by mati on 25/09/16.
 */
public class PostsView {

    private StoredSortedMap<PostKey,Topic> topicMap;
    private InternalDatabaseFactory databaseFactory;

    public PostsView(InternalDatabaseFactory databaseFactory) {

        this.databaseFactory = databaseFactory;

        ClassCatalog classCatalog = databaseFactory.getClassCatalog();

        EntryBinding<PostKey> identityKeyBinding = new SerialBinding<>(classCatalog, PostKey.class);
        EntryBinding<Topic> identityDataBinding = new SerialBinding<>(classCatalog,Topic.class);

        topicMap = new StoredSortedMap<>(databaseFactory.getForumDb(),identityKeyBinding,identityDataBinding,true);

    }

    public StoredSortedMap<PostKey, Topic> getTopicMap() {
        return topicMap;
    }

    public Topic getTopic(long id){
        return topicMap.get(new PostKey(id));
    }

    public final StoredEntrySet getTopicEntrySet(){
        return (StoredEntrySet) topicMap.entrySet();
    }


    public InternalDatabaseFactory getDatabaseFactory() {
        return databaseFactory;
    }
}
