package org.fermat.internal_forum.db;

/**
 * Created by mati on 13/04/17.
 */
public class TopicNotFounException extends Exception {
    private String topicId;

    public TopicNotFounException(String topicId) {
        super(topicId);
    }


    public String getTopicId() {
        return topicId;
    }
}
