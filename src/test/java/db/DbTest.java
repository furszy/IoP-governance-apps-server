package db;

import org.fermat.Context;
import org.fermat.db.exceptions.CantSaveIdentityException;
import org.fermat.internal_forum.db.*;
import org.fermat.internal_forum.model.Post;
import org.fermat.internal_forum.model.Profile;
import org.fermat.internal_forum.model.Topic;
import org.fermat.push_notifications.Firebase;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mati on 04/05/17.
 */
public class DbTest {

    ProfilesDao profilesDao;
    PostDao postDao;


    @Before
    public void setup(){
        Context.init("db_test/");
        postDao = Context.getPostDao();
        profilesDao = Context.getProfilesDao();
        //drop db
        profilesDao.dropDatabase();
        postDao.dropDatabase();
    }


    @Test
    public void saveProfile(){
        Profile profile = new Profile("dawdaljnd2jn3jnadsjdnasjodnqjena","Matias", Firebase.Type.CONTRIB);
        boolean res = false;
        try {
            res = profilesDao.saveProfile(profile);
        } catch (CantSaveIdentityException e) {
            e.printStackTrace();
        }
        assert res;

    }

    @Test
    public void getProfile(){

        saveProfile();

        Profile profile = null;
        String pk = "dawdaljnd2jn3jnadsjdnasjodnqjena";
        try {
            profile = profilesDao.getProfile(pk);
        }  catch (ProfileNotFoundException e) {
            e.printStackTrace();
        }
        assert profile.getPk().equals(pk) : "Profile not saved";
    }

    @Test
    public void getProfiles(){

        Profile profile1 = new Profile("dawdaljnd2jn3jnadsjdnasjodnqjena","Matias", Firebase.Type.CONTRIB);
        Profile profile2 = new Profile("ggrefqedasdawdwqeqwdasczxcxzcazx","Pepe", Firebase.Type.VOT);
        Profile profile3 = new Profile("vadfascsacasdasdasdasdcxzcxzvzvfb","Carlos", Firebase.Type.CONTRIB);
        Profile profile4 = new Profile("asdsnafjanqwidwfadeawjodowa","Juan", Firebase.Type.VOT);
        Profile profile5 = new Profile("ytyrtjyjhgjhgjkghkjghkgkg","Eustacio", Firebase.Type.CONTRIB);

        try {
            profilesDao.saveProfile(profile1);
            profilesDao.saveProfile(profile2);
            profilesDao.saveProfile(profile3);
            profilesDao.saveProfile(profile4);
            profilesDao.saveProfile(profile5);
        } catch (CantSaveIdentityException e) {
            e.printStackTrace();
        }

        assert profilesDao.getProfiles().size() == 5 : "saved profiles amount is not 5";
    }

    @Test
    public void updateProfile(){
        String updateName = "Juancito";
        try {
            Profile profile = saveProfilePriv();
            profilesDao.updateProfile(profile.getPk(),updateName);
            assert profilesDao.getProfile(profile.getPk()).getName().equals(updateName):"Name is not updated in the db";
        } catch (ProfileNotFoundException e) {
            e.printStackTrace();
        } catch (CantUpdateProfileException e) {
            e.printStackTrace();
        } catch (CantSaveIdentityException e) {
            e.printStackTrace();
        }
    }

    private Profile saveProfilePriv() throws CantSaveIdentityException {
        // save
        Profile profile = new Profile("dawdaljnd2jn3jnadsjdnasjodnqjena","Matias", Firebase.Type.CONTRIB);
        profilesDao.saveProfile(profile);
        return profile;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////   Topics      ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void saveTopic(){
        try {
            assert saveTopicPriv()>0 : "topic id is lower than 1";
        } catch (CantSaveIdentityException e) {
            e.printStackTrace();
        } catch (CantSavePostException e) {
            e.printStackTrace();
        }
    }

    private long saveTopicPriv() throws CantSavePostException, CantSaveIdentityException {
        // save profile
        Profile profile = saveProfilePriv();
        // save topic for the profile
        List<String> categories = new ArrayList<>();
        categories.add("Web design");
        byte[] signature = new byte[]{41,6,23,-123,23,51,45,12,78};
        Topic topic = new Topic(profile.getPk(),"Testeo de topico","Subtitulo de testeo",categories,"Esto es un texto de prueba para ver que onda todo",signature,20000);
        long id = postDao.saveTopic(topic,false);
        return id;

    }

    @Test
    public void getTopic(){
        try {
            long topicId = saveTopicPriv();
            Topic topic = postDao.getTopics(topicId);
            assert topic.getId() == topicId: "Topic id is not the same";
        } catch (CantSavePostException e) {
            e.printStackTrace();
        } catch (CantSaveIdentityException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void addComment(){

        try{
            // save profile
            Profile profile = new Profile("dawdaljnd2jn3jnadsjdnasjodnqjena","Matias", Firebase.Type.CONTRIB);
            profilesDao.saveProfile(profile);
            // save topic
            List<String> categories = new ArrayList<>();
            categories.add("Web design");
            byte[] signature = new byte[]{41,6,23,-123,23,51,45,12,78};
            Topic topic = new Topic(profile.getPk(),"Testeo de topico","Subtitulo de testeo",categories,"Esto es un texto de prueba para ver que onda todo",signature,20000);
            long id = postDao.saveTopic(topic,false);
            // save post
            String postRaw = "Comentario 1";
            Post post = new Post(id,profile.getPk(),postRaw,new byte[]{21,54,76,76,123,54,3,21,5,7,3});
            postDao.savePost(post);
            // assert
            assert postDao.getTopics(id).getPosts().get(0).getRaw().equals(postRaw) : "Post raw is not the save as in the db";
        } catch (CantSavePostException e) {
            e.printStackTrace();
        } catch (CantSaveIdentityException e) {
            e.printStackTrace();
        } catch (TopicNotFounException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getComments(){

        try{

            // save profile
            Profile profile = new Profile("dawdaljnd2jn3jnadsjdnasjodnqjena","Matias", Firebase.Type.CONTRIB);
            profilesDao.saveProfile(profile);
            // save topic
            List<String> categories = new ArrayList<>();
            categories.add("Web design");
            byte[] signature = new byte[]{41,6,23,-123,23,51,45,12,78};
            Topic topic = new Topic(profile.getPk(),"Testeo de topico","Subtitulo de testeo",categories,"Esto es un texto de prueba para ver que onda todo",signature,20000);
            long id = postDao.saveTopic(topic,false);
            // save post
            String postRaw1 = "Comentario 1";
            String postRaw2 = "Comentario 2";
            String postRaw3 = "Comentario 3";
            String postRaw4 = "Comentario 4";
            Post post1 = new Post(id,profile.getPk(),postRaw1,new byte[]{21,54,76,76,123,54,3,21,5,7,3});
            Post post2 = new Post(id,profile.getPk(),postRaw2,new byte[]{21,54,76,76,123,54,3,21,5,7,3});
            Post post3 = new Post(id,profile.getPk(),postRaw3,new byte[]{21,54,76,76,123,54,3,21,5,7,3});
            Post post4 = new Post(id,profile.getPk(),postRaw4,new byte[]{21,54,76,76,123,54,3,21,5,7,3});
            postDao.savePost(post1);
            postDao.savePost(post2);
            postDao.savePost(post3);
            postDao.savePost(post4);

            assert postDao.getTopics(id).getPosts().size() == 4 : "Amount of post is not the same as saved..";


        } catch (TopicNotFounException e) {
            e.printStackTrace();
        } catch (CantSaveIdentityException e) {
            e.printStackTrace();
        } catch (CantSavePostException e) {
            e.printStackTrace();
        }
    }

}
