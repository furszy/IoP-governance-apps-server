package org.fermat.notifications;

public class AdminNotificationUpdate {

    public enum AdminNotificationType{

        NONE(0),
        RESTART_BLOCKCHAIN(1),
        UPDATE_APP(2)
        ;

        int id;

        AdminNotificationType(int id) {
            this.id = id;
        }
    }

    private AdminNotificationType type;
    private String message;
    private int minAppVersion;

    public AdminNotificationUpdate(AdminNotificationType type, String message, int minAppVersion) {
        this.type = type;
        this.message = message;
        this.minAppVersion = minAppVersion;
    }

    public AdminNotificationType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public int getMinAppVersion() {
        return minAppVersion;
    }
}
