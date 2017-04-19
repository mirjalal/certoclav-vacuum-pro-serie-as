package com.certoclav.app.database;



import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A simple demonstration object we are creating and persisting to the database.
 */
@DatabaseTable(tableName = "videos")
public class Video {

	Video() {
		// needed by ormlite
	}

	public Video(String path, String description, String downloadUrl) {
		this.path = path;
		this.description = description;
		this.downloadUrl = downloadUrl;
	}

	@DatabaseField(columnName = "cloud_id")
	private String cloudId;

	@DatabaseField(columnName = "version")
	private int version;
	
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
	
	@DatabaseField(generatedId = true, columnName = "video_id")
	private int videoId;
	
	@DatabaseField(columnName = "path")
	private String path;
	
	@DatabaseField(columnName = "description")
	private String description;
	
	@DatabaseField(columnName = "url")
	private String downloadUrl;
	
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}


	
	

	


}




