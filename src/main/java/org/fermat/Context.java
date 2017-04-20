package org.fermat;

import org.fermat.blockchain.ContributionContractsCalculator;
import org.fermat.db.DatabaseFactory;
import org.fermat.db.IdentitiesDao;
import org.fermat.extra_data.ExtraData;
import org.fermat.extra_data.MarketCapApiClient;
import org.fermat.internal_forum.db.InternalDatabaseFactory;
import org.fermat.internal_forum.db.PostDao;
import org.fermat.internal_forum.db.ProfilesDao;
import org.fermat.internal_forum.endpoints.NotificationDispatcher;
import org.fermat.notifications.AdminNotificationUpdate;
import org.fermat.push_notifications.PushDao;
import org.fermat.push_notifications.PushDao2;
import org.fermat.push_notifications.PushDatabaseFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;

/**
 * Created by mati on 16/12/16.
 */
public class Context {

    public static final String EXTRA_DATA_FILENAME = "extra_data.dat";


    private static IdentitiesDao dao;

    private static InternalDatabaseFactory databaseFactory;
    private static PostDao postDao;
    private static ProfilesDao profilesDao;

    private static String iopCoreDir = "/home/mati/IoP-data/IoP-data-1";

    private static AdminNotificationUpdate adminNotificationUpdate;

    private static String forumUrl,apiKey,adminUsername;

    private static ExtraData extraData;

    /** start time in millis */
    private static long startTime;

    static{
        startTime = System.currentTimeMillis();
        extraData = ExtraData.loadExtraData(new File(EXTRA_DATA_FILENAME));
        if (extraData==null){
            BigDecimal bigDecimal = null;
            try {
                bigDecimal = new MarketCapApiClient().getIoPPrice();
            } catch (Exception e) {
                e.printStackTrace();
            }
            extraData = new ExtraData(bigDecimal,1);
            extraData.saveExtraData(new File(EXTRA_DATA_FILENAME));
        }
    }

    private static PushDao2 pushDao;
    private static NotificationDispatcher notificationDispatcher;


    public static void init() {
        File file = new File("");
//        try {
//            DatabaseFactory databaseFactory = new DatabaseFactory(file.getAbsolutePath());
//            dao = new IdentitiesDao(databaseFactory);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        try{
            file = new File("db/");
            if (!file.exists()){
                file.mkdirs();
            }
            databaseFactory = new InternalDatabaseFactory(file.getAbsolutePath());
            postDao = new PostDao(databaseFactory);
            profilesDao = new ProfilesDao(databaseFactory);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            file = new File("db/push/");
            if (!file.exists()){
                file.mkdirs();
            }
            PushDatabaseFactory pushDatabaseFactory = new PushDatabaseFactory(file.getAbsolutePath());
            pushDao = new PushDao2(pushDatabaseFactory);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        notificationDispatcher = new NotificationDispatcher(pushDao);

    }

    public static IdentitiesDao getDao() {
        return dao;
    }

    public static void setIopCoreDir(String iopCoreDir) {
        Context.iopCoreDir = iopCoreDir;
    }

    public static void setAdminNotification(AdminNotificationUpdate.AdminNotificationType type, String message, int appMinVersion){
        adminNotificationUpdate = new AdminNotificationUpdate(type,message,appMinVersion);
    }

    public static AdminNotificationUpdate getAdminNotificationUpdate() {
        return adminNotificationUpdate;
    }

    public static String getIopCoreDir() {
        return iopCoreDir;
    }

    public static long getStartTime() {
        return startTime;
    }

    public static void setForumUrl(String forumUrl) {
        Context.forumUrl = forumUrl;
    }

    public static void setApiKey(String apiKey) {
        Context.apiKey = apiKey;
    }

    public static String getForumUrl() {
        return forumUrl;
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static String getAdminUsername() {
        return adminUsername;
    }

    public static void setAdminUsername(String adminUsername) {
        Context.adminUsername = adminUsername;
    }

    public static PostDao getPostDao() {
        return postDao;
    }

    public static ProfilesDao getProfilesDao() {
        return profilesDao;
    }

    public static ExtraData getExtraData() {
        return extraData;
    }

    public static void setExtraData(ExtraData extraData) {
        Context.extraData = extraData;
        extraData.saveExtraData(new File(EXTRA_DATA_FILENAME));
    }

    public static InternalDatabaseFactory getInternalDb() {
        return databaseFactory;
    }

    public static PushDao2 getPushDao() {
        return pushDao;
    }

    public static NotificationDispatcher getNotificationDispatcher() {
        return notificationDispatcher;
    }
}
