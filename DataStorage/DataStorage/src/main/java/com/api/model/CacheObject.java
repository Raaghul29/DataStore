package com.api.model;

public class CacheObject {    

    private String Key;
    private String value;
    private long expiryTime;

	public String getKey() {
		return Key;
	}
	public void setKey(String key) {
		Key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public long getExpiryTime() {
		return expiryTime;
	}
	public void setExpiryTime(long expiryTime) {
		this.expiryTime = expiryTime;
	}
	public CacheObject(String Key, String value, long expiryTime) {
		super();
		this.Key = Key;
		this.value = value;
		this.expiryTime = expiryTime;
	}



	public boolean isExpired() {
		if(expiryTime == 0)
			return false;
		else
			return System.currentTimeMillis() > expiryTime;
    }
}
