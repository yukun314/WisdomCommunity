package com.wisdom.community.mqtt.demo.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by zyk on 2016/5/11.
 * 推送消息的实体类
 * title(标题)和url唯一标示一条消息
 */
public class MessageEntity implements Serializable{
	//消息的标题
	public String title = "";
	//消息的描述
	public String description = "";
	//消息的发布时间
	public Date time ;
	//消息的URL，"订阅者"根据该url获取完整的消息内容
	public String url = "";
	//消息的发布者(个人或组织名称)
	public String publisher = "";
	//有效期
	public Date validity;
	//是否已经阅读
	public boolean isRead = false;
	//订阅的主题(唯一标示消息的出处 如 a小区和b小区都发了一条相同的公告 topic就可以确定那条是a/b发的)
	public String topic="";

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

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Date getValidity() {
		return validity;
	}

	public void setValidity(Date validity) {
		validity = validity;
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean read) {
		isRead = read;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	public String toString() {
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		return "{" +
				"title='" + title + '\'' +
				", description='" + description + '\'' +
				", time=" + df.format(time) +
				", url='" + url + '\'' +
				", publisher='" + publisher + '\'' +
				", Validity=" + df.format(validity) +
				", isRead="+isRead+
				", topic='" + topic + '\'' +
				'}';
	}
}
