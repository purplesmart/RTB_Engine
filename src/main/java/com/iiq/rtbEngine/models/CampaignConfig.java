package com.iiq.rtbEngine.models;

public class CampaignConfig {
	private int campaignId;
	private int priority;
	private int capacity;
	
	public CampaignConfig(int campaignId,int priority, int capacity) {
		this.campaignId = campaignId;
		this.priority = priority;
		this.capacity = capacity;
	}

	public int getCampaignId() {
		return campaignId;
	}

	public int getPriority() {
		return priority;
	}

	public int getCapacity() {
		return capacity;
	}

}

