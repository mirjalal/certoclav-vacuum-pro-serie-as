package com.certoclav.app.database;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;

import com.certoclav.app.AppConstants;
import com.certoclav.app.model.Autoclave;
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

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import static com.certoclav.app.database.Profile.FIELD_CLOUD_ID;

/**
 * CertoClavDatabase class is responsible for the communication of the
 * application with the sqlite database.
 *
 * @author Iulia Rasinar <iulia.rasinar@nordlogic.com>
 */
public class DatabaseService {
    private final String TAG = getClass().getSimpleName();
    private final Context mContext;


    Dao<Profile, Integer> profileDao;
    Dao<User, Integer> userDao;
    Dao<Protocol, Integer> protocolDao;
    Dao<ProtocolEntry, Integer> protocolEntryDao;
    Dao<Message, Integer> messageDao;
    Dao<Controller, Integer> controllerDao;
    Dao<Video, Integer> videoDao;
    Dao<UserController, Integer> userControllerDao; //helper Dao in order to realize m,n table between User and Controller table

    private DatabaseHelper mDatabaseHelper;


    /**
     * Constructor
     *
     * @param context the context of calling application
     */
    public DatabaseService(final Context context) {
        this.mContext = context;
        mDatabaseHelper = getHelper();


        profileDao = getHelper().getProfileDao();
        userDao = getHelper().getUserDao();
        protocolDao = getHelper().getProtocolDao();
        protocolEntryDao = getHelper().getProtocolEntryDao();
        messageDao = getHelper().getMessageDao();
        controllerDao = getHelper().getControllerDao();
        videoDao = getHelper().getVideoDao();
        userControllerDao = getHelper().getUserControllerDao();

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

    public int updateProtocolErrorMessage(int protocol_id, int errorMessage) {
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
            // TODO Auto-generated catch block
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
            int x = protocolDao.create(protocol);
            return x;

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
            return userDao.create(user);
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
            /** query for object in the database with id equal profileId */
            return profileDao.queryForAll();
        } catch (SQLException e) {
            Log.e(TAG, "Database exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Database exception", e);
        }

        return null;
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
            // TODO Auto-generated catch block
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
            //    TableUtils.clearTable(mDatabaseHelper.getConnectionSource(), ProtocolEntry.class);
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
            //    TableUtils.clearTable(mDatabaseHelper.getConnectionSource(), ProtocolEntry.class);
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;


    }


    public int deleteUser(User user) {

        try {
            return userDao.delete(user);
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

    public int updateUserPassword(String email_user_id, String newPassword) {
        try {
            UpdateBuilder<User, Integer> updateBuilder = userDao
                    .updateBuilder();

            updateBuilder.where().eq("email", email_user_id);

            /** query for object in the database with id equal profileId */
            updateBuilder.updateColumnValue("password", newPassword);

            int r = updateBuilder.update();
            return r;
        } catch (java.sql.SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;

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


    public void fillDatabaseWithProgramIfEmpty() {
        try {

            if (getProfiles() != null) {
                if (getProfiles().size() > 0) {
                    return;
                }
            }

            Controller controller = null;

            if (getControllers() != null) {
                if (getControllers().size() == 0) {
                    controller = Autoclave.getInstance().getController();
                    insertController(controller);
                } else {
                    controller = getControllers().get(0);
                }
            } else {
                controller = new Controller("unknown", "unknown", "unknown", "unknown", 0, "unknown");
                Autoclave.getInstance().setController(controller);
                insertController(controller);
            }


            //Standardprofile
            Profile profile1 = new Profile("", 1, "134 \u00B0C SOLID", 1, 4, 134, 210, 0, 3, "134 \u00B0C   2.1bar   4min\nSolid instruments and textiles\nWrapped or unwrapped", true, true, controller, 1);
            Profile profile2 = new Profile("", 1, "134 \u00B0C POROUS", 3, 4, 134, 210, 0, 7, "134 \u00B0C   2.1bar   4min\nPorous instruments and textiles\nWrapped or unwrapped", true, true, controller, 2);
            Profile profile3 = new Profile("", 1, "134 \u00B0C HOLLOW", 3, 4, 134, 210, 0, 10, "134 \u00B0C   2.1bar   4min\nHollow instruments\nWrapped or unwrapped", true, true, controller, 3);
            Profile profile4 = new Profile("", 1, "121 \u00B0C SOLID", 1, 20, 121, 110, 0, 3, "121 \u00B0C   1.1bar   20min\nSolid instruments and textiles\nWrapped or unwrapped", true, true, controller, 4);
            Profile profile5 = new Profile("", 1, "121 \u00B0C POROUS", 3, 20, 121, 110, 0, 7, "121 \u00B0C   1.1bar   20min\nPorous instruments and textiles\nWrapped or unwrapped", true, true, controller, 5);
            Profile profile6 = new Profile("", 1, "121 \u00B0C HOLLOW", 3, 20, 121, 110, 0, 10, "121 \u00B0C   1.1bar   20min\nHollow instruments\nWrapped or unwrapped", true, true, controller, 6);
            Profile profile7 = new Profile("", 1, "USER DEFINED", 3, 5, 134, 210, 0, 10, "Vacuum times: User defined\nSterilization temperature: User defined\nDry time: User defined", true, true, controller, 7);
            Profile profile8 = new Profile("", 1, "BD HELIX TEST", 3, 4, 134, 210, 0, 7, "Test to evaluate the capacity\nof penetration of the steam\nin hollow loads", true, true, controller, 8);
            Profile profile9 = new Profile("", 1, "VACUUM TEST", 0, 0, 0, -80, 15, 0, "-0.8bar   15min\nVacuum Test\nRecommended on maintenance", true, true, controller, 9);
            Profile profile10 = new Profile("", 1, "CLEAN", 3, 5, 105, 20, 0, 10, "105 \u00B0C   0.2bar   5min\nClean the autoclave", true, true, controller, 10);
            Profile profile11 = new Profile("", 1, "PRION", 3, 19, 135, 210, 0, 10, "134 \u00B0C   2.1bar   19min\nPrion sterilization", true, true, controller, 11);
            Profile profile12 = null;
            if(AppConstants.IS_CERTOASSISTANT == false) {
                profile12 = new Profile("", 1, "LIQUID", 1, 20, 121, 110, 0, 0, "Liquid sterilization\n121 \u00B0C   1.1bar   20min\nWith media sensor", true, true, controller, 12);
            }
            int result = 0;
            result = insertProfile(profile1);
            result = insertProfile(profile2);
            result = insertProfile(profile3);
            result = insertProfile(profile4);
            result = insertProfile(profile5);
            result = insertProfile(profile6);
            result = insertProfile(profile7);
            result = insertProfile(profile8);
            result = insertProfile(profile9);
            result = insertProfile(profile10);
            result = insertProfile(profile11);

            result = insertProfile(profile12);

        } catch (Exception e) {

        }
    }


}