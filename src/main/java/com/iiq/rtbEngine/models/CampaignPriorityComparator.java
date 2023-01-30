package com.iiq.rtbEngine.models;

import java.util.Comparator;

public class CampaignPriorityComparator implements Comparator<CampaignConfig> {
    public int compare(CampaignConfig campaignConfig1, CampaignConfig campaignConfig2) {
        return campaignConfig1.getPriority() - campaignConfig2.getPriority();
    }
}
