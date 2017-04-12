package org.fermat.push_notifications.message;

import com.google.gson.Gson;

import static org.fermat.push_notifications.message.PushCodeType.PUSH_UPDATE_TOPIC;

/**
 * Created by mati on 11/04/17.
 */
public class TopicUpdatePushMsg extends BasePushMessage<TopicUpdatePushMsg> {

    private long newTopicId;

    public TopicUpdatePushMsg(long newTopicId) {
        super(PUSH_UPDATE_TOPIC);
        this.newTopicId = newTopicId;
    }

    @Override
    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public TopicUpdatePushMsg fromJson(String s) {
        return new Gson().fromJson(s,TopicUpdatePushMsg.class);
    }
}
