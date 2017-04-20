package org.fermat.internal_forum.db;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.collections.StoredIterator;
import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.je.*;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;
import org.fermat.internal_forum.endpoints.RequestTopicsServlet;
import org.fermat.internal_forum.model.Post;
import org.fermat.internal_forum.model.Topic;

import java.io.ObjectStreamClass;
import java.util.*;

/**
 * Created by mati on 25/09/16.
 */
public class PostDao {

    private static final Logger logger = Logger.getLogger(PostDao.class);

    private InternalDatabaseFactory databaseFactory;

    private PostsView postsView;

    public PostDao(InternalDatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
        postsView = new PostsView(databaseFactory);
    }

    public Map<PostKey,Topic> getPosts(){
        return new HashMap<>(postsView.getTopicMap());
    }

    public Topic getTopics(long id){
        EntryBinding<PostKey> identityKeyBinding = new SerialBinding<>(databaseFactory.getClassCatalog(), PostKey.class);
        EntryBinding<Topic> identityDataBinding = new SerialBinding<>(databaseFactory.getClassCatalog(),Topic.class);
        DatabaseEntry keyDatabaseEntry = new DatabaseEntry();
        DatabaseEntry valueDatabaseEntry = new DatabaseEntry();
        PostKey postKey = new PostKey(id);
        identityKeyBinding.objectToEntry(postKey,keyDatabaseEntry);
        OperationStatus op = databaseFactory.getForumDb().get(null, keyDatabaseEntry, valueDatabaseEntry, null);
        if (op == OperationStatus.SUCCESS) {
            Topic topic =  identityDataBinding.entryToObject(valueDatabaseEntry);
            logger.info("object: " +topic.toString());
            return topic;
        }
        return null;
    }


    public List<Topic> getTopicsAfterTime(long timeInMillis,int maxAmount){
        List<Topic> topics = new ArrayList<>();
        long i = getPostsCount();
        EntryBinding<PostKey> identityKeyBinding = new SerialBinding<>(databaseFactory.getClassCatalog(), PostKey.class);
        EntryBinding<Topic> identityDataBinding = new SerialBinding<>(databaseFactory.getClassCatalog(),Topic.class);
        long postCount = i;
        do {
            DatabaseEntry keyDatabaseEntry = new DatabaseEntry();
            DatabaseEntry valueDatabaseEntry = new DatabaseEntry();
            PostKey postKey = new PostKey(i);
            identityKeyBinding.objectToEntry(postKey,keyDatabaseEntry);
            OperationStatus op = databaseFactory.getForumDb().get(null, keyDatabaseEntry, valueDatabaseEntry, null);
            if (op == OperationStatus.SUCCESS) {
                Topic topic =  identityDataBinding.entryToObject(valueDatabaseEntry);
                if (topic.getPubTime()>timeInMillis) {
                    topics.add(topic);
                }
//                logger.info("object: " +topic.toString());
            }
            i--;
        }while (i>1 && topics.size() < maxAmount);

//        Cursor cursor = databaseFactory.getForumDb().openCursor(null, null);
//
//        DatabaseEntry foundKey =
//                new DatabaseEntry();
//        DatabaseEntry foundData =
//                new DatabaseEntry();
//
//        while (cursor.getNext(foundKey,
//                foundData,
//                LockMode.DEFAULT)
//                == OperationStatus.SUCCESS) {
//
//            try {
//                if (foundData.getSize()>1) {
//
//                    Topic topic = identityDataBinding.entryToObject(foundData);
//                    logger.info("entry: " + topic.toString());
//                }
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//
//        }
//        cursor.close();
//        try {
//            for (Topic topic : getPosts().values()) {
//                logger.info(topic.toString());
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//            StoredIterator<Topic> storedIterator = postsView.getTopicEntrySet().storedIterator();
//            while (storedIterator.hasNext() && i < maxAmount) {
//                try {
//                Topic topic = storedIterator.next();
//                if (topic.getPubTime() > timeInMillis) {
//                    topics.add(topic);
//                }
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                i++;
//            }
        return topics;
    }

    public synchronized long saveTopic(Topic topic) throws CantSavePostException {
        TransactionRunner transactionRunner = new TransactionRunner(databaseFactory.getEnvironment());
        long id = getPostsCount();
        topic.setId(id);
        topic.setPubTime(System.currentTimeMillis());
        try {
            transactionRunner.run(()->postsView.getTopicMap().put(new PostKey(topic.getId()), topic));
            return id;
        } catch (Exception e) {
            throw new CantSavePostException(e);
        }
    }

    public synchronized long savePost(Post post) throws CantSavePostException, TopicNotFounException {
        post.setPubTime(System.currentTimeMillis());
        int id = 0;
        Topic topic = postsView.getTopic(post.getTopicId());
        if (topic!=null) {
            List<Post> posts = topic.getPosts();
            if (posts != null) {
                id = posts.size();
            }
            post.setId(id);
            topic.addPost(post);
            try {
                EntryBinding<PostKey> keyBinding = new SerialBinding<>(databaseFactory.getClassCatalog(), PostKey.class);
                EntryBinding<Topic> dataBinding = new SerialBinding<>(databaseFactory.getClassCatalog(), Topic.class);

                DatabaseEntry theKey = new DatabaseEntry();
                DatabaseEntry theData = new DatabaseEntry();

                keyBinding.objectToEntry(new PostKey(topic.getId()), theKey);
                dataBinding.objectToEntry(topic, theData);

                WriteOptions wo = new WriteOptions();
                // This sets the TTL using day units. Another variation
                // of setTTL() exists that accepts a TimeUnit class instance.
                wo.setTTL(5);
                // If the record currently exists, update the TTL value
                wo.setUpdateTTL(true);
                postsView.getDatabaseFactory().getForumDb().put(
                        null,             // Transaction handle.
                        theKey,           // Record's key.
                        theData,          // Record's data.
                        Put.OVERWRITE,    // If the record exists,
                        // overwrite it.
                        wo);              // WriteOptions instance.

            } catch (Exception e) {
                // Exception handling goes here
                e.printStackTrace();
            }
        }else{
            throw new TopicNotFounException(String.valueOf(post.getTopicId()));
        }

//        try {
//            transactionRunner.run(() -> {
//                topic.addPost(post);
//            });
//        } catch (Exception e) {
//            throw new CantSavePostException(e);
//        }
        return id;
    }


    public long getPostsCount() {
        return databaseFactory.getForumDb().count();
    }

    public List<Post> getCommentsForTopic(long topicId,long startTime, int maxRecordAmount) {
        Topic topic = getTopics(topicId);
        if (topic!=null)
            return topic.getPosts();
        return null;
    }
}
