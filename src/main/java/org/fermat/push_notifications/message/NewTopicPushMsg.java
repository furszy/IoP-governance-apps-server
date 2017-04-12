package org.fermat.push_notifications.message;

import com.google.gson.Gson;

import static org.fermat.push_notifications.message.PushCodeType.PUSH_NEW_TOPIC;

/**
 * Created by mati on 11/04/17.
 */
public class NewTopicPushMsg extends BasePushMessage<NewTopicPushMsg> {

    private long newTopicId;
    private String title;

    public NewTopicPushMsg(long newTopicId,String title) {
        super(PUSH_NEW_TOPIC);
        this.newTopicId = newTopicId;
        this.title = title;
    }

    @Override
    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public NewTopicPushMsg fromJson(String s) {
        return new Gson().fromJson(s,NewTopicPushMsg.class);
    }
}
