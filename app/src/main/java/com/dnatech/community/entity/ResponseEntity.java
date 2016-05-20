package com.dnatech.community.entity;

/**
 * Created by zyk on 2016/5/20.
 */
public class ResponseEntity {
	public int code = 0;
	public String msg = "";
	public String data = "";

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "{" +
				"code=" + code +
				", msg='" + msg + '\'' +
				", data='" + data + '\'' +
				'}';
	}
}
