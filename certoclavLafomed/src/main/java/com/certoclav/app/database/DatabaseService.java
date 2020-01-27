package com.certoclav.app.database;

import android.content.Context;
import android.database.SQLException;
import android.os.Environment;
import android.util.Log;

import com.certoclav.app.AppConstants;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.util.AuditLogger;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.bcrypt.BCrypt;
import com.certoclav.library.certocloud.CloudUser;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import static com.certoclav.app.database.Profile.FIELD_CLOUD_ID;
import static com.certoclav.app.database.Profile.FIELD_LAST_USED_TIME;

/**
 * CertoClavDatabase class is responsible for the communication of the
 * application with the sqlite database.
 *
 * @author Iulia Rasinar <iulia.rasinar@nordlogic.com>
 */
public class DatabaseService {
    private final String TAG = getClass().getSimpleName();
    private final Context mContext;
    private static DatabaseService databaseService;


    Dao<Profile, Integer> profileDao;
    Dao<User, Integer> userDao;
    Dao<Protocol, Integer> protocolDao;
    Dao<ProtocolEntry, Integer> protocolEntryDao;
    Dao<Message, Integer> messageDao;
    Dao<Controller, Integer> controllerDao;
    Dao<Video, Integer> videoDao;
    Dao<AuditLog, Integer> auditLogDao;
    Dao<UserController, Integer> userControllerDao; //helper Dao in order to realize m,n table between User and Controller table
    //Deleted Programs to Sync in Cloud
    Dao<DeletedProfileModel, Integer> deletedProfileDao = null;

    private DatabaseHelper mDatabaseHelper;


    /**
     * Constructor
     */
    private DatabaseService() {
        this.mContext = ApplicationController.getContext();
        mDatabaseHelper = getHelper();


        profileDao = getHelper().getProfileDao();
        userDao = getHelper().getUserDao();
        protocolDao = getHelper().getProtocolDao();
        protocolEntryDao = getHelper().getProtocolEntryDao();
        messageDao = getHelper().getMessageDao();
        controllerDao = getHelper().getControllerDao();
        videoDao = getHelper().getVideoDao();
        auditLogDao = getHelper().getAuditDao();
        userControllerDao = getHelper().getUserControllerDao();
        deletedProfileDao = getHelper().getDeletedProfileDao();

        mDatabaseHelper.checkOldDatabaseExistsImport();
    }

    public static DatabaseService getInstance() {
        if (databaseService == null)
            databaseService = new DatabaseService();
        return databaseService;
    }

    /**
     * Releases the helper when done.
     */
    public void close() {
        if (mDatabaseHelper != null) {
            OpenHelperManager.releaseHelper();
            mDatabaseHelper = null;
        }
    }

    public void resetDatabase() {

        ConnectionSource connectionSource = mDatabaseHelper.getConnectionSource();
        try {

            Log.i("DatabaseService", "dropTables");
            TableUtils.dropTable(connectionSource, User.class, true);
            TableUtils.dropTable(connectionSource, Profile.class, true);
            TableUtils.dropTable(connectionSource, Protocol.class, true);
            TableUtils.dropTable(connectionSource, ProtocolEntry.class, true);
            TableUtils.dropTable(connectionSource, Message.class, true);

            Log.i("DatabaseService", "createTables");
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, Profile.class);
            TableUtils.createTable(connectionSource, Protocol.class);
            TableUtils.createTable(connectionSource, ProtocolEntry.class);
            TableUtils.createTable(connectionSource, Message.class);

        } catch (java.sql.SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
        }

    }

    public List<Protocol> getProtocols() {
        try {

            /** query for object in the database with id equal profileId */
            return protocolDao.queryForAll();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);

        }
        return null;
    }


    public Protocol getProtocolByCloudId(String cloudId) {
        try {

            QueryBuilder<Protocol, Integer> protocolQb = protocolDao.queryBuilder();
            protocolQb.where().eq(Protocol.FIELD_PROTOCOL_CLOUD_ID, cloudId);
            return protocolQb.queryForFirst();

        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);

        }
        return null;
    }

    public List<Protocol> getProtocols(long page, long limit, String orderBY, boolean ascending) {

        try {
            QueryBuilder<Protocol, Integer> protocolQb = protocolDao.queryBuilder();
            //QueryBuilder<User, Integer> userQb = userDao.queryBuilder();
            protocolQb.where().eq(Protocol.FIELD_USER_EMAIL, Autoclave.getInstance().getUser().getEmail());
            protocolQb.orderBy(orderBY, ascending);
            protocolQb.offset((page - 1) * limit).limit(limit);
            return protocolQb.query();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }

    public Protocol getProtocolById(int protocolId) {
        try {

            return protocolDao.queryForId(protocolId);
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);

        }
        return null;
    }


    public List<Protocol> getProtocolsWhereNotUploaded() {
        try {

            return protocolDao.queryBuilder().where().eq(Protocol.FIELD_PROTOCOL_UPLOADED, false).query();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } catch (SQLException e2) {
            e2.printStackTrace();
        }
        return null;
    }


    public int updateProtocolEndTime(int protocol_id, Date endTime) {
        try {
            UpdateBuilder<Protocol, Integer> updateBuilder = protocolDao
                    .updateBuilder();
            updateBuilder.where().eq("protocol_id", protocol_id);
            /** query for object in the database with id equal profileId */
            updateBuilder.updateColumnValue("endTime", endTime);

            int r = updateBuilder.update();


            return r;
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }
        return -1;
    }

    public int updateProtocolErrorCode(int protocol_id, int errorMessage) {
        try {
            UpdateBuilder<Protocol, Integer> updateBuilder = protocolDao
                    .updateBuilder();
            updateBuilder.where().eq("protocol_id", protocol_id);
            /** query for object in the database with id equal profileId */
            updateBuilder.updateColumnValue("errorId", errorMessage);

            int r = updateBuilder.update();


            return r;

        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }
        return -1;
    }


    public int updateProtocolIsUploaded(int protocol_id, boolean b) {
        try {
            UpdateBuilder<Protocol, Integer> updateBuilder = protocolDao
                    .updateBuilder();
            updateBuilder.where().eq("protocol_id", protocol_id);
            /** query for object in the database with id equal profileId */
            updateBuilder.updateColumnValue("uploaded", b);

            int r = updateBuilder.update();


            return r;

        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }
        return -1;


    }

    public int updateProtocolCloudId(int protocol_id, String cloudId) {
        try {
            UpdateBuilder<Protocol, Integer> updateBuilder = protocolDao
                    .updateBuilder();
            updateBuilder.where().eq("protocol_id", protocol_id);
            /** query for object in the database with id equal profileId */
            updateBuilder.updateColumnValue(Protocol.FIELD_PROTOCOL_CLOUD_ID, cloudId);

            int r = updateBuilder.update();

            return r;

        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }
        return -1;


    }

    public List<Protocol> getProtocolsForCurrentUserOrderedBySuccess(boolean ascending) {

        try {

            QueryBuilder<Protocol, Integer> protocolQb = protocolDao.queryBuilder();
            //QueryBuilder<User, Integer> userQb = userDao.queryBuilder();
            protocolQb.where().eq(Protocol.FIELD_USER_EMAIL, Autoclave.getInstance().getUser().getEmail());
            protocolQb.orderBy("errorId", ascending);

            return protocolQb.query();


        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }

    public List<Protocol> getProtocolsForCurrentUserOrderedByStartTime(boolean ascending) {

        try {

            QueryBuilder<Protocol, Integer> protocolQb = protocolDao.queryBuilder();
            //QueryBuilder<User, Integer> userQb = userDao.queryBuilder();
            protocolQb.where().eq(Protocol.FIELD_USER_EMAIL, Autoclave.getInstance().getUser().getEmail());
            protocolQb.orderBy(Protocol.FIELD_PROTOCOL_START_TIME, ascending);
            return protocolQb.query();


        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }

    public int insertDeletedProfile(DeletedProfileModel model) {

        try {
            return deletedProfileDao.create(model);
        } catch (java.sql.SQLException e) {
            Log.e(TAG, e.getMessage());
        }
        return -1;
    }

    public List<DeletedProfileModel> getDeletedProfiles() {
        try {

            /** query for object in the database with id equal profileId */
            return deletedProfileDao.queryForAll();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return new ArrayList<>();
    }


    public int deleteDeletedProfile(DeletedProfileModel model) {

        try {
            return deletedProfileDao.delete(model);
        } catch (java.sql.SQLException e) {
            Log.e(TAG, e.getMessage());
        }
        return -1;
    }

    public int updateUserIsLocal(String email_user_id, boolean isLocal) {
        try {
            UpdateBuilder<User, Integer> updateBuilder = userDao
                    .updateBuilder();

            updateBuilder.where().eq("email", email_user_id);

            /** query for object in the database with id equal profileId */
            updateBuilder.updateColumnValue(User.FIELD_USER_LOCAL, isLocal);

            int r = updateBuilder.update();
            return r;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public List<Protocol> getProtocolsForCurrentUserOrderByCycleNumber(boolean ascending) {

        try {

            QueryBuilder<Protocol, Integer> protocolQb = protocolDao
                    .queryBuilder();
            protocolQb.where().eq(Protocol.FIELD_USER_EMAIL, Autoclave.getInstance().getUser().getEmail());
            return protocolQb.orderBy("zyklusNumber", ascending).query();

        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }


    public List<Protocol> getProtocolsOfCurrentUserSortedByProgramName(boolean ascending) {

        try {
            QueryBuilder<Protocol, Integer> protocolQb = protocolDao.queryBuilder();
            //QueryBuilder<User, Integer> userQb = userDao.queryBuilder();
            protocolQb.where().eq(Protocol.FIELD_USER_EMAIL, Autoclave.getInstance().getUser().getEmail());
            protocolQb.orderBy(Protocol.FIELD_PROGRAM_NAME, ascending);
            return protocolQb.query();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }


    public List<Message> getMessages() {
        try {

            /** query for object in the database with id equal profileId */
            return messageDao.queryForAll();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }


    public int addAuditLog(AuditLog auditLog) {

        try {
            int x = auditLogDao.create(auditLog);
            return x;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<AuditLog> getAuditLogs(User user, String orderBy, boolean isAsc) {
        try {

            /** query for object in the database with id equal profileId */
            QueryBuilder<AuditLog, Integer> query = auditLogDao.queryBuilder();
            if (user != null)
                query.where().eq(AuditLog.FIELD_USER_ID, user.getUserId());
            if (orderBy != null)
                query.orderBy(orderBy, isAsc);
            else
                query.orderBy(AuditLog.FIELD_AUDIT_ID, false);
            return query.query();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }

    public List<AuditLog> getPagedAuditLogs(int pageNumber, int limit) {
        try {
            int lastId = auditLogDao.queryBuilder().orderBy(AuditLog.FIELD_AUDIT_ID, false).limit(1L).query().get(0).getAuditId();
            QueryBuilder<AuditLog, Integer> query = auditLogDao.queryBuilder();
            query.where().between(AuditLog.FIELD_AUDIT_ID, lastId - limit * (pageNumber + 1), lastId - limit * pageNumber);
            query.orderBy(AuditLog.FIELD_AUDIT_ID, false);
            return query.query();
        } catch (Exception e) {
            return null;
        }
    }

    public int insertMessage(Message message) {

        try {

            int x = messageDao.create(message);


            return x;

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return -1;
    }

    public int deleteMessage(final Message message) {
        try {

            return messageDao.delete(message);
        } catch (java.sql.SQLException e) {
            Log.e(TAG, e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }
        return -1;
    }


    /**
     * Retrieves all records from the table Profiles.
     *
     * @return the list of all profiles in the db
     */
    public List<Controller> getControllers() {
        try {
            return controllerDao.queryForAll();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }
        return null;
    }


    public int insertController(Controller controller) {

        try {

            int x = controllerDao.create(controller);

            return x;

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int insertVideo(Video video) {

        try {

            int x = videoDao.create(video);

            return x;

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Video> getVideos() {
        try {
            return videoDao.queryForAll();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }

    public int deleteVideo(Video video) {
        try {

            return videoDao.delete(video);
        } catch (java.sql.SQLException e) {
            Log.e(TAG, e.getMessage());
        }

        return -1;
    }

    public int insertProtocol(Protocol protocol) {

        if (isProtcolExists(protocol.getCloudId()))
            return -1;
        try {
            return protocolDao.create(protocol);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean isProtcolExists(String cloudId) {
        if (cloudId.length() <= 0) return false;

        try {

            return protocolDao.queryBuilder().where().eq(FIELD_CLOUD_ID, cloudId).query().size() > 0;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } catch (SQLException e2) {
            e2.printStackTrace();
        }
        return false;


    }
/*
    public int deleteProtocol(final Protocol protocol) {
		try {

			return protocolDao.delete(protocol);
		} catch (java.sql.SQLException e) {
			Log.e(TAG, e.getMessage());
		}
		return -1;
	}
*/


    public List<User> getUsers() { // f°r gro�e Datenmengen ist for (User user :
        // userDao) { ... } besser da die objekte
        // nicht alle auf einmal in eine liste
        // geladen werden m�ssen
        try {

            /** query for object in the database with id equal profileId */
            return userDao.queryForAll();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {

            Log.e(TAG, "Database exception", e);

        }

        return null;
    }

    public User getUserById(int userId) {
        try {

            return userDao.queryForId(userId);
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }

    public User getUserByUsername(String username) {
        try {
            List<User> users = userDao.queryBuilder().where().eq(User.FIELD_USER_EMAIL, username).query();
            if (users.size() > 0)
                return users.get(0);
            return null;
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }


    public List<User> getUserByCloudId(String userId) {
        try {
            return userDao.queryBuilder().where().eq(User.FIELD_USER_CLOUD_ID, userId).query();

        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }

    public int insertUser(User user) {
        try {
            int result = userDao.create(user);
            if (result != -1)
                AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                        AuditLogger.ACTION_USER_CREATED,
                        AuditLogger.OBJECT_EMPTY,
                        user.getEmail(), false);
            return result;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

	/*
    public int deleteUser(final User user) {
		try {

			return userDao.delete(user);
		} catch (java.sql.SQLException e) {
			Log.e(TAG, e.getMessage());
		}
		return -1;
	}
*/

    /**
     * Retrieves a record from the table Profiles with a given id
     *
     * @param profileId the id for which to query the database
     * @return {@link Profile} object with id equals to profileId
     */
    public Profile getProfileById(final int profileId) {
        try {

            /** query for object in the database with id equal profileId */
            return profileDao.queryForId(profileId);
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }


    public List<Profile> getProfileByIndex(final int profileIndex) {
        try {

            /** query for object in the database with id equal profileId */
            return profileDao.queryBuilder().where().eq(Profile.FIELD_INDEX, profileIndex).query();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }


    public List<Profile> getProfilesByName(String name) {

        try {

            return profileDao.queryBuilder().where().eq("name", name).query();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } catch (SQLException e2) {
            e2.printStackTrace();
        }
        return null;

    }

    public List<Profile> getProfilesWhereVisible() {

        try {

            return profileDao.queryBuilder().where().eq("is_visible", true).query();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } catch (SQLException e2) {
            e2.printStackTrace();
        }
        return null;

    }


    public List<Profile> getProfilesWhereLocal() {
        try {

            return profileDao.queryBuilder().where().eq(Profile.FIELD_IS_LOCAL, true).query();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } catch (SQLException e2) {
            e2.printStackTrace();
        }
        return null;
    }


    /**
     * Retrieves all records from the table Profiles.
     *
     * @return the list of all profiles in the db
     */
    public List<Profile> getProfiles() {
        try {
            final Dao<Profile, Integer> profileDao = mDatabaseHelper
                    .getProfileDao();
            QueryBuilder qb = profileDao.queryBuilder();
            qb.orderBy(FIELD_LAST_USED_TIME, false);
            /** query for object in the database with id equal profileId */
            return qb.query();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }


    /**
     * Retrieves all records from the table Profiles.
     *
     * @return the list of all profiles in the db
     */
    public List<Profile> getProfiles(int user_id) {
        try {
            final Dao<Profile, Integer> profileDao = mDatabaseHelper
                    .getProfileDao();
            QueryBuilder qb = profileDao.queryBuilder();
            qb.where().eq("user_id", user_id);
            qb.orderBy(FIELD_LAST_USED_TIME, false);
            /** query for object in the database with id equal profileId */
            return qb.query();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return new ArrayList<>();
    }


    public int updateProfileRecentUsed(int profile_id) {
        try {
            UpdateBuilder<Profile, Integer> updateBuilder = profileDao
                    .updateBuilder();

            updateBuilder.where().eq("profile_id", profile_id);

            /** query for object in the database with id equal profileId */
            updateBuilder.updateColumnValue(FIELD_LAST_USED_TIME, new Date().getTime());

            int r = updateBuilder.update();
            return r;
        } catch (java.sql.SQLException e) {

            e.printStackTrace();
        }
        return -1;

    }


    public int updateProvileVisibility(int profile_id, Boolean isVisible) {
        try {
            UpdateBuilder<Profile, Integer> updateBuilder = profileDao
                    .updateBuilder();

            updateBuilder.where().eq("profile_id", profile_id);

            /** query for object in the database with id equal profileId */
            updateBuilder.updateColumnValue("is_visible", isVisible);

            int r = updateBuilder.update();
            return r;
        } catch (java.sql.SQLException e) {

            e.printStackTrace();
        }
        return -1;


    }


    /**
     * Inserts a profile into db, table profiles
     *
     * @param newProfile the profile to be inserted
     * @return the id of the inserted profile of -1 in case of operation failure
     */
    public int insertProfile(final Profile newProfile) {
        Dao<Profile, Integer> profileDao;
        try {
            profileDao = mDatabaseHelper.getProfileDao();
            return profileDao.create(newProfile);
        } catch (java.sql.SQLException e) {
            Log.e(TAG, e.getMessage());
        }
        return -1;
    }

    public int deleteAllProfiles() {
        try {

            return profileDao.delete(profileDao.queryForAll());
        } catch (java.sql.SQLException e) {
            Log.e(TAG, e.getMessage());
        }
        return -1;
    }

    public int deleteAllVideos() {
        try {

            return videoDao.delete(videoDao.queryForAll());
        } catch (java.sql.SQLException e) {
            Log.e(TAG, e.getMessage());
        }
        return -1;
    }

    // Deletes a profile from db, table profiles

    // @param profile
    //           the profile to be deleted
    // @return the id of the deleted profile of -1 in case of operation failure

    public int deleteProfile(final Profile profile) {

        try {
            return profileDao.delete(profile);
        } catch (java.sql.SQLException e) {
            Log.e(TAG, e.getMessage());
        }
        return -1;
    }

    public List<ProtocolEntry> getProtocolEntrys() {
        try {
            return protocolEntryDao.queryForAll();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }

    public List<ProtocolEntry> getProtocolEntrysByProtocol(int protocol_id) {
        try {
            return protocolEntryDao.queryForAll();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
    }

    public int insertProtocolEntry(ProtocolEntry protocolEntry) {

        try {
            return protocolEntryDao.create(protocolEntry);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int insertProtocolEntry(final List<ProtocolEntry> protocolEntries) {

        try {
            TransactionManager.callInTransaction(mDatabaseHelper.getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (ProtocolEntry protocolEntry : protocolEntries)
                        protocolEntryDao.create(protocolEntry);
                    return null;
                }
            });
            return 1;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int deleteProtocolsEntry(List<Protocol> protocols) {
        try {
            DeleteBuilder db = protocolEntryDao.deleteBuilder();


            Where main = db.where();
            for (Protocol protocol : protocols) {
                main.eq("protocol_id", protocol);
            }
            main.or(protocols.size());
            protocolEntryDao.delete(db.prepare());
            //    TableUtils.clearTable(mDatabaseHelper.getInstance().getConnectionSource(), ProtocolEntry.class);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int deleteProtocolEntry(Protocol protocol) {
        try {
            DeleteBuilder db = protocolEntryDao.deleteBuilder();


            Where main = db.where();
            main.eq("protocol_id", protocol);
            protocolEntryDao.delete(db.prepare());
            //    TableUtils.clearTable(mDatabaseHelper.getInstance().getConnectionSource(), ProtocolEntry.class);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void deleteSyncedProtocols() {
        try {

            DeleteBuilder db = protocolDao.deleteBuilder();
            db.where().eq(Protocol.FIELD_PROTOCOL_UPLOADED, true).and().eq(Protocol.FIELD_USER_EMAIL, CloudUser.getInstance().getEmail());
            protocolDao.delete(db.prepare());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteProtocol(Protocol protocol) {
        try {
            deleteProtocolEntry(protocol);
            DeleteBuilder db = protocolDao.deleteBuilder();
            db.where().eq(Protocol.FIELD_PROTOCOL_CLOUD_ID, protocol.getCloudId()).and()
                    .eq(Protocol.FIELD_PROTOCOL_UPLOADED, true).and()
                    .eq(Protocol.FIELD_USER_EMAIL, CloudUser.getInstance().getEmail());
            protocolDao.delete(db.prepare());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the helper from the manager once per class.
     */
    private DatabaseHelper getHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = OpenHelperManager.getHelper(mContext,
                    DatabaseHelper.class);
        }
        return mDatabaseHelper;
    }

    public int updateUserVisibility(String email_user_id, boolean isVisible) {
        try {
            UpdateBuilder<User, Integer> updateBuilder = userDao
                    .updateBuilder();

            updateBuilder.where().eq("email", email_user_id);

            /** query for object in the database with id equal profileId */
            updateBuilder.updateColumnValue("is_visible", isVisible);

            int r = updateBuilder.update();
            return r;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;


    }


    public int deleteUser(User user) {

        try {
            int result = userDao.delete(user);
            if (result != -1)
                AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                        AuditLogger.ACTION_USER_DELETED,
                        AuditLogger.OBJECT_EMPTY,
                        user.getEmail(),
                        true);
            return result;
        } catch (java.sql.SQLException e) {
            Log.e(TAG, e.getMessage());
        }
        return -1;
    }


    public List<User> getUsersWhereVisible() {
        try {

            return userDao.queryBuilder().where().eq("is_visible", true).query();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } catch (SQLException e2) {
            e2.printStackTrace();
        }

        return null;


    }

    public int updateUserPassword(String email_user_id, String newPassword, boolean addToLog) {
        try {
            UpdateBuilder<User, Integer> updateBuilder = userDao
                    .updateBuilder();

            updateBuilder.where().eq("email", email_user_id);

            /** query for object in the database with id equal profileId */
            updateBuilder.updateColumnValue("password", newPassword);
            updateBuilder.updateColumnValue(User.FIELD_PASSWORD_EXPITE, new Date(new Date().getTime() + AppConstants.PASSWORD_EXPIRE));

            int r = updateBuilder.update();

            if (addToLog)
                AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                        AuditLogger.ACTION_USER_PASSWORD_UPDATED,
                        AuditLogger.OBJECT_EMPTY,
                        null, true);
            return r;
        } catch (java.sql.SQLException e) {
            // 
            e.printStackTrace();
        }
        return -1;

    }

    public int updateUser(User user) {
        try {
            return userDao.update(user);
        } catch (java.sql.SQLException e) {
            //
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * This method stands for to update the user profile.
     * Due to some reasons (actually I really don't know why)
     * userDao.update() method does NOT operates as expected.
     * So, in this method the user will be deleted by its id,
     * then the same user will be created with new id & data.
     *
     * @param user to be deleted
     * @param oldUserID user to be updated
     * @return `1` IF AND ONLY IF insert method succeedes,
     *         `-1` otherwise.
     *
     * @author mirjalal
     */
    public int updateUserProfile(User user, int oldUserID) {
        try {
            userDao.deleteById(oldUserID);
            int result = userDao.create(user);
            if (result == 1)
                AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                        AuditLogger.ACTION_USER_UPDATED,
                        AuditLogger.OBJECT_LOGIN,
                        user.getEmail(), false);

            return result;
        } catch (Exception e) {
            return -1;
        }
    }

    public List<Profile> getProfilesWhithValidCloudId() {

        try {

            return profileDao.queryBuilder().where().notIn(Profile.FIELD_CLOUD_ID, "").query();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } catch (SQLException e2) {
            e2.printStackTrace();
        }
        return null;

    }

    public void createAdminAccountIfNotExistantYet() {
        try {
            if (getUsers() != null) {
                boolean adminAccountExists = false;
                for (User dbUser : getUsers()) {
                    if (dbUser.isAdmin() == true) {
                        adminAccountExists = true;
                    }
                }
                if (adminAccountExists == false) {
                    //create admin acccount
                    User user = new User("Admin", "", "Admin", "Admin", "", "", "", "", "",
                            BCrypt.hashpw("admin", BCrypt.gensalt()),
                            Autoclave.getInstance().getDateObject(), true, true);
                    insertUser(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean exportDB() {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = "/data/" + mContext.getPackageName() + "/databases/" + DatabaseHelper.DATABASE_NAME;
        String backupDBPath = DatabaseHelper.DATABASE_NAME;
        File currentDB = new File(data, currentDBPath);
        Log.d("data", currentDB.getAbsolutePath());
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}