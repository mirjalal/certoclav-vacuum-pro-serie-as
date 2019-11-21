package com.certoclav.app.database;


import com.certoclav.app.AppConstants;
import com.certoclav.app.model.Autoclave;
import com.certoclav.library.certocloud.CloudUser;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * A simple demonstration object we are creating and persisting to the database.
 */
@DatabaseTable(tableName = "users")
public class User {
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_USER_CLOUD_ID = "cloud_id";
    public static final String FIELD_USER_EMAIL = "email";
    public static final String FIELD_USER_LOCAL = "is_local";
    public static final String FIELD_PASSWORD = "password";
    public static final String FIELD_PASSWORD_EXPITE = "date_password_expired";
    public static final String FIELD_LOGIN_ATTEMPT = "login_attempt";
    public static final String FIELD_BLOCKED_TILL = "blocked_till";

    // id is generated by the database and set on the object automatically
    @DatabaseField(generatedId = true, columnName = FIELD_USER_ID)
    private int userId;

    @DatabaseField(columnName = FIELD_USER_EMAIL)
    @SerializedName("username")
    private String email;

    @DatabaseField(columnName = FIELD_USER_CLOUD_ID)
    private String cloudId;

    @DatabaseField(columnName = "version")
    private int version;

    @DatabaseField(columnName = "firstName")
    @SerializedName("firstname")
    private String firstName;

    @DatabaseField(columnName = "lastName")
    @SerializedName("lastname")
    private String lastName;

    @DatabaseField(columnName = "mobile")
    private String mobile;

    private String tokenForCloud;

    @DatabaseField(columnName = "landline")
    private String landline;

    @DatabaseField(columnName = FIELD_PASSWORD)
    private String password;

    @DatabaseField(columnName = "company_name")
    private String companyName;

    @DatabaseField(columnName = "company_phone")
    private String companyPhone;

    @DatabaseField(columnName = "company_website")
    private String companyWebsite;


    @DatabaseField(columnName = "date", dataType = DataType.DATE)
    private Date date;

    @DatabaseField(columnName = FIELD_BLOCKED_TILL, dataType = DataType.DATE)
    private Date dateBlockedTill;

    @DatabaseField(columnName = FIELD_PASSWORD_EXPITE, dataType = DataType.DATE)
    private Date passwordExpireDate;

    @DatabaseField(columnName = "isAdmin")
    private Boolean isAdmin;

    public Boolean isBlocked() {
        return loginAttempt > AppConstants.MAX_LOGIN_ATTEMPTS && Autoclave.getInstance().isFDAEnabled();
    }

    public Boolean isBlockedByDate() {
        return this.dateBlockedTill != null && this.dateBlockedTill.after(new Date()) && Autoclave.getInstance().isFDAEnabled();
    }

    public int getLoginAttemptCount() {
        return loginAttempt;
    }

    public void setBlockedByDate(Date dateBlockedTill) {
        this.dateBlockedTill = dateBlockedTill;
    }

    public void resetLoginAttempt() {
        this.loginAttempt = 0;
    }

    public void increaseLoginAttempt() {
        //Only if the FDA has been enabled
        if (Autoclave.getInstance().isFDAEnabled())
            this.loginAttempt++;
        else
            this.loginAttempt = 0;
    }

    @DatabaseField(columnName = FIELD_LOGIN_ATTEMPT)
    private int loginAttempt;

    @DatabaseField(columnName = "is_visible")
    private Boolean isVisible;

    @DatabaseField(columnName = FIELD_USER_LOCAL)
    private Boolean isLocal;


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }


    public String getEmail() {
        return email;
    }

    public boolean getIsLocalAdmin() {
        return isAdmin && isLocal && !CloudUser.getInstance().isSuperAdmin();
    }

    User() {
        // needed by ormlite
    }

    /*
     * Note: controller must be a object (not NULL)
     */
    public User(String firstName, String cloudId, String lastName, String email, String mobile, String landline, String companyName, String companyPhone, String companyWebsite, String password, Date date, Boolean isAdmin, Boolean isLocal) {
        this.email = email;
        this.cloudId = cloudId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile = mobile;
        this.landline = landline;
        this.password = password;
        this.companyName = companyName;
        this.companyPhone = companyPhone;
        this.companyWebsite = companyWebsite;
        this.date = date;
        this.passwordExpireDate = new Date(date.getTime() + AppConstants.PASSWORD_EXPIRE);
        this.isAdmin = isAdmin;
        this.isVisible = true;
        this.isLocal = isLocal;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    @Override
    public String toString() {
        String test = "test";
        return test;

    }

    public Boolean getIsLocal() {
        return isLocal;
    }

    public void setIsLocal(Boolean isLocal) {
        this.isLocal = isLocal;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }

    public Boolean isAdmin() {
        return isAdmin || CloudUser.getInstance().isSuperAdmin();
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getFirstName() {
        return firstName != null ? firstName : "";
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getLandline() {
        return landline;
    }

    public void setLandline(String landline) {
        this.landline = landline;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getEmail_user_id() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTokenForCloud() {
        return tokenForCloud;
    }

    public void setTokenForCloud(String tokenForCloud) {
        this.tokenForCloud = tokenForCloud;
    }

    public void setPasswordExpireDate(Date passwordExpireDate) {
        this.passwordExpireDate = passwordExpireDate;
    }

    public Date getPasswordExpireDate() {
        return passwordExpireDate;
    }


    public boolean isPasswordExpired() {
        return (passwordExpireDate == null || passwordExpireDate.before(new Date()))
                && Autoclave.getInstance().isFDAEnabled();
    }
}




