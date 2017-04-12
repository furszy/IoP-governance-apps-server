package org.fermat.push_notifications;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import org.apache.log4j.Logger;

import java.util.Set;

/**
 * Created by mati on 11/04/17.
 */
public class PushDao {

    private static final Logger logger = Logger.getLogger(PushDao.class);

    private PushDatabaseFactory pushDatabaseFactory;

    // Index Accessors
    PrimaryIndex<Long,TopicSubscription> pIdx;
//    SecondaryIndex<String,String,PushSubscription> sIdByPushId;
//    SecondaryIndex<Set,String,PushSubscription> sIdx;

    public PushDao(PushDatabaseFactory pushDatabaseFactory) {
        this.pushDatabaseFactory = pushDatabaseFactory;

//        EntityStore store = pushDatabaseFactory.getStore();

        // Primary key for PushSubscription classes
//        pIdx = store.getPrimaryIndex(Long.class, TopicSubscription.class);
//        sIdByPushId = store.getSecondaryIndex(pIdx,String.class,"pushDeviceId");
        // Secondary key for SimpleEntityClass classes
        // Last field in the getSecondaryIndex() method must be
        // the name of a class member; in this case, an
        // SimpleEntityClass.class data member.
//        sIdx = store.getSecondaryIndex(pIdx, Set.class, "subscriptionCodes");
    }

//    public PushSubscription registerPushDevice(String profilePublicKey, String devicePushId) throws CantPushDeviceException {
//        PushSubscription pushSubscription = new PushSubscription(profilePublicKey,devicePushId);
//        return pIdx.put(pushSubscription);
//    }
//
//    public PushSubscription getSubscription(String ownerPk){
//        return pIdx.get(ownerPk);
//    }


    public TopicSubscription addTopicSubscription(long topicId,String deviceId){
        TopicSubscription p = pIdx.get(topicId);
        p.addSubscriptionDevice(deviceId);
        return pIdx.put(p);
    }

    public Set<String> getTopicPushDeviceIds(long topicId){
        return pIdx.get(topicId).getSubscriptors();
    }

//    public PushSubscription addSubscriptionCodeByDeviceId(String devicePushId,SuscriptionType suscriptionType){
//        PushSubscription p = sIdByPushId.get(devicePushId);
//        p.addSubscriptionDevice(suscriptionType.getId());
//        return pIdx.put(p);
//    }

//    public Set<TopicSubscription> getSuscriptions(SuscriptionType suscriptionType){
//        Set<PushSubscription> pushSubscriptions = new HashSet<>();
//        Set<String> subscriptionCodes = new HashSet<>();
//        subscriptionCodes.add(suscriptionType.getId());
//        EntityCursor<PushSubscription> cursor = sIdx.entities(subscriptionCodes,true,null,false);
//        for (PushSubscription pushSubscription : cursor) {
//            pushSubscriptions.add(pushSubscription);
//        }
//        cursor.close();
//        return pushSubscriptions;
//    }

}
