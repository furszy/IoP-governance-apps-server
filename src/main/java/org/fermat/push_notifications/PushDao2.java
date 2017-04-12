package org.fermat.push_notifications;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Put;
import com.sleepycat.je.WriteOptions;
import org.apache.log4j.Logger;
import org.fermat.internal_forum.db.CantSavePostException;
import org.fermat.internal_forum.db.PostKey;
import org.fermat.internal_forum.model.Post;
import org.fermat.internal_forum.model.Topic;

import java.util.*;

/**
 * Created by mati on 25/09/16.
 */
public class PushDao2 {

    private static final Logger logger = Logger.getLogger(PushDao2.class);

    private PushDatabaseFactory databaseFactory;

    public PushDao2(PushDatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    public synchronized TopicSubscription addTopicSubscription(long topicId,String deviceId) throws CantSavePostException {
        TopicSubscription p = getTopicSubscription(topicId);
        if (p==null){
            p = new TopicSubscription(topicId);
        }
        p.addSubscriptionDevice(deviceId);
        saveOrUpdatePushTopic(p);
        return p;
    }

    public synchronized TopicSubscription removeTopicSubscription(long topicId,String deviceId) throws CantSavePostException{
        TopicSubscription p = getTopicSubscription(topicId);
        if (p==null){
            p = new TopicSubscription(topicId);
        }
        p.removeSubscriptionDevice(deviceId);
        saveOrUpdatePushTopic(p);
        return p;
    }

    public Set<String> getTopicPushDeviceIds(long topicId){
        TopicSubscription topicSubscription = getTopicSubscription(topicId);
        if (topicSubscription!=null){
            return topicSubscription.getSubscriptors();
        }else {
            return new HashSet<>();
        }

    }

    public TopicSubscription getTopicSubscription(long topicId){
        EntryBinding<Long> identityKeyBinding = new SerialBinding<>(databaseFactory.getClassCatalog(), Long.class);
        EntryBinding<TopicSubscription> identityDataBinding = new SerialBinding<>(databaseFactory.getClassCatalog(),TopicSubscription.class);
        DatabaseEntry keyDatabaseEntry = new DatabaseEntry();
        DatabaseEntry valueDatabaseEntry = new DatabaseEntry();
        identityKeyBinding.objectToEntry(topicId,keyDatabaseEntry);
        OperationStatus op = databaseFactory.getPushDb().get(null, keyDatabaseEntry, valueDatabaseEntry, null);
        if (op == OperationStatus.SUCCESS) {
            TopicSubscription topic =  identityDataBinding.entryToObject(valueDatabaseEntry);
            logger.info("object: " +topic.toString());
            return topic;
        }
        return null;
    }

    public synchronized boolean saveOrUpdatePushTopic(TopicSubscription topicSubscription) throws CantSavePostException{
        try {
            EntryBinding<Long> keyBinding = new SerialBinding<>(databaseFactory.getClassCatalog(), Long.class);
            EntryBinding<TopicSubscription> dataBinding = new SerialBinding<>(databaseFactory.getClassCatalog(),TopicSubscription.class);

            DatabaseEntry theKey = new DatabaseEntry();
            DatabaseEntry theData = new DatabaseEntry();

            keyBinding.objectToEntry(topicSubscription.getTopicId(),theKey);
            dataBinding.objectToEntry(topicSubscription,theData);

            WriteOptions wo = new WriteOptions();
            // This sets the TTL using day units. Another variation
            // of setTTL() exists that accepts a TimeUnit class instance.
            wo.setTTL(5);
            // If the record currently exists, update the TTL value
            wo.setUpdateTTL(true);
            databaseFactory.getPushDb().put(
                    null,             // Transaction handle.
                    theKey,           // Record's key.
                    theData,          // Record's data.
                    Put.OVERWRITE,    // If the record exists,
                    // overwrite it.
                    wo);              // WriteOptions instance.

        } catch (Exception e) {
            // Exception handling goes here
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public long getSuscriptionSize() {
        return databaseFactory.getPushDb().count();
    }

}
