package com.dnatech.community.entity;

/**
 * Created by zyk on 2016/5/19.
 */
public class QRCodeContentsEntity  {
	public String userId = "";
	public String communityId = "";
	public String devId = "";
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCommunityId() {
		return communityId;
	}

	public void setCommunityId(String communityId) {
		this.communityId = communityId;
	}

	public String getClientId() {
		return devId;
	}

	public void setClientId(String dev) {
		this.devId = dev;
	}

	@Override
	public String toString() {
		return "{" +
				"userId='" + userId + '\'' +
				", communityId='" + communityId + '\'' +
				", clientId='" + devId + '\'' +
				'}';
	}
}
