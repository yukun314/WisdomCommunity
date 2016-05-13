package com.wisdom.community.mqtt.demo.entity;

import java.io.Serializable;

/**
 * Created by zyk on 2016/5/11.
 * 推送消息的实体类
 */
public class MessageEntity implements Serializable{
	//消息的标题
	public String title = "";
	//消息的描述
	public String description = "";
	//消息的发布时间
	public String time = "";
	//消息的URL，"订阅者"根据该url获取完整的消息内容
	public String url = "";
	//消息的发布者(个人或组织名称)
	public String publisher = "";

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	@Override
	public String toString() {
		return "{" +
				"title='" + title + '\'' +
				", description='" + description + '\'' +
				", time='" + time + '\'' +
				", url='" + url + '\'' +
				", publisher='" + publisher + '\'' +
				'}';
	}
}
