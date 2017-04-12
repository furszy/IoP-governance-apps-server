//package org.fermat.push_notifications;
//
//import com.sleepycat.persist.model.Entity;
//import com.sleepycat.persist.model.PrimaryKey;
//import com.sleepycat.persist.model.SecondaryKey;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import static com.sleepycat.persist.model.Relationship.ONE_TO_MANY;
//import static com.sleepycat.persist.model.Relationship.ONE_TO_ONE;
//
///**
// * Created by mati on 11/04/17.
// */
//@Entity
//public class PushSubscription {
//
//    @PrimaryKey
//    private String ownerPk;
//
//    @SecondaryKey(relate = ONE_TO_ONE)
//    private String pushDeviceId;
//
//    @SecondaryKey(relate=ONE_TO_MANY)
//    private Set<String> subscriptionCodes = new HashSet<String>();
//
//    public PushSubscription() {
//    }
//
//    public PushSubscription(String ownerPk, String pushDeviceId) {
//        this.ownerPk = ownerPk;
//        this.pushDeviceId = pushDeviceId;
//    }
//
//    public String getPushDeviceId() {
//        return pushDeviceId;
//    }
//
//    public void setPushDeviceId(String pushDeviceId) {
//        this.pushDeviceId = pushDeviceId;
//    }
//
//    public Set<String> getSubscriptionCodes() {
//        return subscriptionCodes;
//    }
//
//    public void setSubscriptionCodes(Set<String> subscriptionCodes) {
//        this.subscriptionCodes = subscriptionCodes;
//    }
//
//    public void addSubscriptionDevice(String id) {
//        subscriptionCodes.add(id);
//    }
//}
