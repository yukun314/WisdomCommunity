package com.dnatech.community.entity;

/**
 * Created by zyk on 2016/5/15.
 */
public class SubscribeEntity {
	public String topic;
	public int qos;

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getQos() {
		return qos;
	}

	public void setQos(int qos) {
		this.qos = qos;
	}

	@Override
	public String toString() {
		return "{" +
				", topic='" + topic + '\'' +
				", qos=" + qos +
				'}';
	}
}
