package com.dnatech.community.entity;

/**
 * Created by zyk on 2016/5/20.
 */
public class UserInfo  {

	private static UserInfo user;
	public static UserInfo getInstance(){
		if(user == null) {
			user = new UserInfo();
		}
		return user;
	}

	public UserInfo(){
		user = this;
	}

	public String id;//在数据库中的主键
	public String fullName;//姓名
	public String sex;//性别
	public String age;//年龄
	public String phone;//手机号码
	public String deviceId;//手机的唯一标示码
	public String parentId;//父ID
	public String remark;//备注
	public String userName;//登陆账号
	public String password;//密码
	public String authCode;//登录的auth_code

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}
}
