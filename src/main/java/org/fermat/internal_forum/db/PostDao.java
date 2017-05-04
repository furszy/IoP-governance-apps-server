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
import org.omg.PortableInterceptor.SUCCESSFUL;

import java.io.ObjectStreamClass;
import java.util.*;

/**
 * Created by mati on 25/09/16.
 */
public class PostDao {

    private static final Logger logger = Logger.getLogger(PostDao.class);

    private InternalDatabaseFactory databaseFactory;

    public PostDao(InternalDatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    public Topic getTopics(long id){
        return getPrimaryIndex().get(id);
    }


    public List<Topic> getTopicsAfterTime(long timeInMillis,int maxAmount){
        List<Topic> topics = new ArrayList<>();
        getPrimaryIndex().entities().forEach(topic -> {
            if (topic.getPubTime()>timeInMillis) {
                topics.add(topic);
            }
        });
        return topics;
    }


    public synchronized boolean updateTopic(Topic topic){
        getPrimaryIndex().put(topic);
        return true;
    }

    public synchronized long saveTopic(Topic topic,boolean updateIfExist) throws CantSavePostException{
        try {
            long id = getPostsCount()+1;
            topic.setId(id);
            topic.setPubTime(System.currentTimeMillis());
            if (updateIfExist) {
                updateTopic(topic);
            } else {
                getPrimaryIndex().putNoOverwrite(topic);
            }
            return id;
        }catch (UniqueConstraintException e){
            throw new CantSavePostException("Title already used");
        }

    }

    public boolean deleteTopic(long topicId) {
        EntryBinding<PostKey> identityKeyBinding = new SerialBinding<>(databaseFactory.getClassCatalog(), PostKey.class);
        DatabaseEntry keyDatabaseEntry = new DatabaseEntry();
        PostKey postKey = new PostKey(topicId);
        identityKeyBinding.objectToEntry(postKey,keyDatabaseEntry);
        OperationStatus op = databaseFactory.getForumDb().delete(null, keyDatabaseEntry);
        if (op == OperationStatus.SUCCESS) {
            logger.info("topic removed: " +topicId);
            return true;
        }
        return false;
    }

    public synchronized long savePost(Post post) throws CantSavePostException, TopicNotFounException {
        post.setPubTime(System.currentTimeMillis());
        int id = 0;
        Topic topic = getTopics(post.getTopicId());
        if (topic!=null) {
            List<Post> posts = topic.getPosts();
            if (posts != null) {
                id = posts.size();
            }
            post.setId(id);
            topic.addPost(post);
            updateTopic(topic);
        }else{
            throw new TopicNotFounException(String.valueOf(post.getTopicId()));
        }
        return id;
    }


    public long getPostsCount() {
        return getPrimaryIndex().count();
    }

    private PrimaryIndex<Long,Topic> getPrimaryIndex(){
        return databaseFactory.getTopicsStore().getPrimaryIndex(Long.class,Topic.class);
    }

    public List<Post> getCommentsForTopic(long topicId,long startTime, int maxRecordAmount) {
        Topic topic = getTopics(topicId);
        if (topic!=null)
            return topic.getPosts();
        return null;
    }


    public void dropDatabase() {
        databaseFactory.getTopicsStore().truncateClass(Topic.class);
    }
}
