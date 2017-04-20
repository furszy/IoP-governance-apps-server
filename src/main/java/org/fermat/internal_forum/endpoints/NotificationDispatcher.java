package org.fermat.internal_forum.endpoints;

import org.fermat.push_notifications.Firebase;
import org.fermat.push_notifications.PushDao;
import org.fermat.push_notifications.PushDao2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by mati on 12/04/17.
 */
public class NotificationDispatcher {

    private ExecutorService executorService;
    private PushDao2 pushDao;

    public NotificationDispatcher(PushDao2 pushDao) {
        this.executorService = Executors.newFixedThreadPool(5);
        this.pushDao = pushDao;
    }

    public void dispatchTopicNotification(long topicId, Firebase.Type type){
        pushDao.getTopicPushDeviceIds(topicId).forEach(t->{
            try {
                Firebase.pushFCMNotification(t,topicId,type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
