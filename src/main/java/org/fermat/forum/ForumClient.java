package org.fermat.forum;

import org.fermat.forum.discourse.utils.ResponseModel;

public interface ForumClient {

    ForumProfile registerUser(String username, String password, String email,boolean active) throws CantRegisterUserException, Exception;

    String requestKey(String username,String password) throws UserNotActiveException, UserNotFoundException;

    ForumProfile getForumProfile();

    ForumProfile getUser(String username) throws UserNotFoundException;

    boolean connect(String username, String password) throws UserNotFoundException, InactiveUserException;

    boolean trustUser(ForumProfile forumProfile);

    boolean loginUser(String userName, String password);

    String getTopic(int topicId) throws Exception;
}
