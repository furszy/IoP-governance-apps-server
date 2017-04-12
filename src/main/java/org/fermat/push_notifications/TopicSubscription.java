package org.fermat.push_notifications;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mati on 12/04/17.
 */
@Entity
public class TopicSubscription implements Serializable{

    static final long serialVersionUID = 42241511232133L;

    @PrimaryKey
    private long topicId;
    @SecondaryKey(relate = Relationship.ONE_TO_MANY)
    private Set<String> subscriptors;


    public TopicSubscription(long topicId) {
        this.topicId = topicId;
    }

    public TopicSubscription() {
    }

    public void addSubscriptionDevice(String devicePushId) {
        if (subscriptors==null){
            subscriptors=new HashSet<>();
        }
        subscriptors.add(devicePushId);
    }

    public long getTopicId() {
        return topicId;
    }

    public Set<String> getSubscriptors() {
        return subscriptors;
    }

    public boolean removeSubscriptionDevice(String deviceId) {
        return subscriptors.remove(deviceId);
    }
}
