package com.certoclav.app.database;

import com.certoclav.app.util.AuditLogger;
import com.certoclav.library.certocloud.CloudUser;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "audit_logs")
public class AuditLog {

    public static final String FIELD_AUDIT_ID = "audit_id";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_EVENT_ID = "eventname";
    public static final String FIELD_OBJECT_ID = "object_name";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_OBJECT_VALUE = "value";
    public static final String FIELD_SCREEN_ID = "screenid";
    public static final String FIELD_COMMENT = "comment";
    public static final String FIELD_USER_EMAIL = "useremail";

    public AuditLog(User user, int screenId, int eventId, int objectId, String value, String comment) {
        this.user = user;
        this.eventId = eventId;
        this.objectId = objectId;
        this.value = value;
        this.screenId = screenId;
        this.email = CloudUser.getInstance().isSuperAdmin() ? "Raypa Admin" : user.getEmail();
        this.createdDate = new Date();
        this.comment = comment;
    }


    public AuditLog(String email, int screenId, int eventId, int objectId, String value, String comment) {
        this.user = user;
        this.eventId = eventId;
        this.objectId = objectId;
        this.value = value;
        this.screenId = screenId;
        this.email = CloudUser.getInstance().isSuperAdmin() ? "Raypa Admin" : email;
        this.createdDate = new Date();
        this.comment = comment;
    }


    AuditLog() {
        // needed by ormlite
    }

    @DatabaseField(generatedId = true, columnName = FIELD_AUDIT_ID)
    private int auditId;

    // by User
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = FIELD_USER_ID)
    private User user;

    //Screen
    @DatabaseField(columnName = FIELD_SCREEN_ID)
    private int screenId;

    //Which event
    @DatabaseField(columnName = FIELD_EVENT_ID)
    private int eventId;

    //On which object
    @DatabaseField(columnName = FIELD_OBJECT_ID)
    private int objectId;

    //Value if exists
    @DatabaseField(columnName = FIELD_OBJECT_VALUE)
    private String value;

    //Save email if user deleted
    @DatabaseField(columnName = FIELD_USER_EMAIL)
    private String email;

    @DatabaseField(columnName = FIELD_DATE, canBeNull = false, dataType = DataType.DATE_STRING,
            format = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    @DatabaseField(columnName = FIELD_COMMENT)
    private String comment;

    public int getAuditId() {
        return auditId;
    }

    public User getUser() {
        return user;
    }

    public int getEventId() {
        return AuditLogger.getResource(eventId);
    }

    public int getObjectId() {
        return AuditLogger.getResource(objectId);
    }

    public Date getDate() {
        return createdDate;
    }

    public String getValue() {
        return value;
    }

    public int getScreenId() {
        return AuditLogger.getResource(screenId);
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getEmail() {
        return email;
    }

    public String getComment() {
        return comment == null ? "" : comment;
    }
}
