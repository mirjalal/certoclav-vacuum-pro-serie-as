package com.certoclav.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.certoclav.app.R;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.sql.SQLException;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application -- change to something appropriate for your app
    public static final String DATABASE_NAME = "helloAndroid.db";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 9;


    // the DAO object we use to access the SimpleData table

    private Dao<User, Integer> userDao = null;
    private Dao<Profile, Integer> profileDao = null;
    private Dao<Protocol, Integer> protocolDao = null;
    private Dao<ProtocolEntry, Integer> protocolEntryDao = null;
    private Dao<Message, Integer> messageDao = null;
    private Dao<Controller, Integer> controllerDao = null;
    private Dao<Video, Integer> videoDao = null;
    private Dao<UserController, Integer> userControllerDao = null;
    private Dao<AuditLog, Integer> auditLogDao;
    private Dao<DeletedProfileModel, Integer> deletedProfileDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, Profile.class);
            TableUtils.createTable(connectionSource, Protocol.class);
            TableUtils.createTable(connectionSource, ProtocolEntry.class);
            TableUtils.createTable(connectionSource, Message.class);
            TableUtils.createTable(connectionSource, Controller.class);
            TableUtils.createTable(connectionSource, Video.class);
            TableUtils.createTable(connectionSource, UserController.class);
            TableUtils.createTable(connectionSource, AuditLog.class);
            TableUtils.createTable(connectionSource, DeletedProfileModel.class);

        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }

    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (oldVersion < 2)
            db.execSQL("ALTER TABLE profile ADD COLUMN " + Profile.FIELD_LAST_USED_TIME + " INTEGER DEFAULT 0");
        if (oldVersion < 3) {
            try {
                TableUtils.createTable(connectionSource, AuditLog.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (oldVersion < 9) {

            try {
                Log.i(DatabaseHelper.class.getName(), "onCreate");
                TableUtils.dropTable(connectionSource, AuditLog.class, true);

                TableUtils.createTable(connectionSource, AuditLog.class);

            } catch (SQLException e) {
                Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
                throw new RuntimeException(e);
            }

        }
    }

    public Dao<DeletedProfileModel, Integer> getDeletedProfileDao() {
        if (deletedProfileDao == null) {
            try {
                deletedProfileDao = getDao(DeletedProfileModel.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return deletedProfileDao;
    }

    public Dao<Profile, Integer> getProfileDao() {
        if (profileDao == null) {
            try {
                profileDao = getDao(Profile.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return profileDao;
    }


    public Dao<User, Integer> getUserDao() {
        if (userDao == null) {
            try {
                userDao = getDao(User.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return userDao;
    }

    public Dao<Protocol, Integer> getProtocolDao() {
        if (protocolDao == null) {
            try {
                protocolDao = getDao(Protocol.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return protocolDao;
    }

    public Dao<ProtocolEntry, Integer> getProtocolEntryDao() {
        if (protocolEntryDao == null) {
            try {
                protocolEntryDao = getDao(ProtocolEntry.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return protocolEntryDao;
    }

    public Dao<Message, Integer> getMessageDao() {
        if (messageDao == null) {
            try {
                messageDao = getDao(Message.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return messageDao;
    }

    public Dao<Controller, Integer> getControllerDao() {
        if (controllerDao == null) {
            try {
                controllerDao = getDao(Controller.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return controllerDao;
    }

    public Dao<AuditLog, Integer> getAuditDao() {
        if (auditLogDao == null) {
            try {
                auditLogDao = getDao(AuditLog.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return auditLogDao;
    }


    public Dao<Video, Integer> getVideoDao() {
        if (videoDao == null) {
            try {
                videoDao = getDao(Video.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return videoDao;
    }

    public Dao<UserController, Integer> getUserControllerDao() {
        if (userControllerDao == null) {
            try {
                userControllerDao = getDao(UserController.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return userControllerDao;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        userDao = null;
        profileDao = null;
        protocolDao = null;
        protocolEntryDao = null;
        auditLogDao = null;
        deletedProfileDao = null;
        messageDao = null;
        controllerDao = null;
        videoDao = null;
        userControllerDao = null;

    }


    public void checkOldDatabaseExistsImport() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            String backupDBPath = DatabaseHelper.DATABASE_NAME;
            File backupDB = new File(sd, backupDBPath);

            long count = 0;
            if (backupDB.exists()) {

                SQLiteDatabase myDataBase = SQLiteDatabase.openDatabase(backupDB.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
                Cursor c = myDataBase.rawQuery("select * from audit_logs", null);
                if (c.moveToFirst()) {

                    for (int i = 0; i < c.getCount(); i++) {

                        try {
                            auditLogDao.create(new AuditLog(
                                    c.getString(c.getColumnIndex(AuditLog.FIELD_USER_EMAIL)),
                                    c.getInt(c.getColumnIndex(AuditLog.FIELD_SCREEN_ID)),
                                    c.getInt(c.getColumnIndex(AuditLog.FIELD_EVENT_ID)),
                                    c.getInt(c.getColumnIndex(AuditLog.FIELD_OBJECT_ID)),
                                    c.getString(c.getColumnIndex(AuditLog.FIELD_OBJECT_VALUE))));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        c.moveToNext();
                        count++;
                    }
                    c.close();
                    myDataBase.close();
                }
            }
            QueryBuilder<AuditLog, Integer> query = auditLogDao.queryBuilder();
            try {
                Log.d("imported", auditLogDao.countOf() + " " + count);
                if (auditLogDao.countOf() == count) {
                    backupDB.delete();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
