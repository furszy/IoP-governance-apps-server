package org.fermat;

import org.fermat.db.DatabaseFactory;
import org.fermat.db.IdentitiesDao;
import org.fermat.notifications.AdminNotificationUpdate;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by mati on 16/12/16.
 */
public class Context {

    private static IdentitiesDao dao;

    private static String iopCoreDir = "/home/mati/IoP-data/IoP-data-1";

    private static AdminNotificationUpdate adminNotificationUpdate;

    private static String forumUrl,apiKey,adminUsername;

    /** start time in millis */
    private static long startTime;

    static{
        startTime = System.currentTimeMillis();
    }


    public static void init() {
        try {
            File file = new File("");
            DatabaseFactory databaseFactory = new DatabaseFactory(file.getAbsolutePath());
            dao = new IdentitiesDao(databaseFactory);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
}
