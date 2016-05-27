package com.dnatech.community.entity;

/**
 * Created by zyk on 2016/5/24.
 */
public class KeysEntity {

	public String nickName;
	public String keyName;
	public String keyNo;

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public String getKeyNo() {
		return keyNo;
	}

	public void setKeyNo(String keyNo) {
		this.keyNo = keyNo;
	}

	@Override
	public String toString() {
		return "{" +
				"nickName='" + nickName + '\'' +
				", keyName='" + keyName + '\'' +
				", keyNo='" + keyNo + '\'' +
				'}';
	}
}
