package com.certoclav.app.database;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "audit_logs")
public class AuditLog {

    public static final String FIELD_AUDIT_ID = "audit_id";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_SCREEN_NAME = "screen_name";
    public static final String FIELD_EVENT_NAME = "eventname";
    public static final String FIELD_OBJECT_NAME = "object_name";
    public static final String FIELD_DATE = "date";

    public AuditLog( User user, String screenName, String eventName, String objectName) {
        this.user = user;
        this.screenName = screenName;
        this.eventName = eventName;
        this.objectName = objectName;
    }

    @DatabaseField(generatedId = true, columnName = FIELD_AUDIT_ID)
    private int auditId;

    // by User
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = FIELD_USER_ID)
    private User user;

    //On which screen
    @DatabaseField(columnName = FIELD_SCREEN_NAME)
    private String screenName;

    //Which event
    @DatabaseField(columnName = FIELD_EVENT_NAME)
    private String eventName;

    //On which object
    @DatabaseField(columnName = FIELD_OBJECT_NAME)
    private String objectName;

    @DatabaseField(columnName = FIELD_DATE, dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date createdDate;

    public int getAuditId() {
        return auditId;
    }

    public User getUser() {
        return user;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getEventName() {
        return eventName;
    }

    public String getObjectName() {
        return objectName;
    }

    public Date getDate() {
        return createdDate;
    }
}
