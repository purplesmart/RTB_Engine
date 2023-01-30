package com.iiq.rtbEngine.db;

import com.iiq.rtbEngine.models.CampaignConfig;
import com.iiq.rtbEngine.models.CampaignPriorityComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RTBCache {

    private static final String UNMATCHED = "unmatched";
    private static final String CAPPED = "capped";

    Map<Integer, Set<Integer>> profileToAttribute = new HashMap<>();
    Map<Integer, List<Integer>> attributeToCampaign = new HashMap<>();
    Map<Integer, Integer> campaignToCapacity = new HashMap<>();
    Set<Integer> cappedCampaigns = new HashSet<>();
    List<CampaignConfig> campaignConfig;

    @Autowired
    CampaignCapacityDao campaignCapacityDao;

    @Autowired
    CampaignsConfigDao campaignsConfigDao;


    public void initAttributeToCampaign(Map<Integer, List<Integer>> allCampaignAttributes) {
        allCampaignAttributes.forEach((campaignId, attributes) -> attributes.forEach((att) -> {
            if (!attributeToCampaign.containsKey(att))
                attributeToCampaign.put(att, new ArrayList<>());
            attributeToCampaign.get(att).add(campaignId);
        }));
    }

    public void initProfileCampaignCapacityDao(List<CampaignConfig> campaignConfig) {
        this.campaignConfig = campaignConfig;
        this.campaignConfig.sort(new CampaignPriorityComparator());
        campaignCapacityDao.createTable();
        campaignToCapacity = campaignCapacityDao.getAllProfileCampaignCapacity();
        if(campaignToCapacity != null) {
            campaignToCapacity.forEach((campaignId, capacity) -> {
                if (campaignConfig.get(campaignId).getCapacity() == capacity)
                    cappedCampaigns.add(campaignId);
            });
        }
        else{
            campaignToCapacity = new HashMap<>();
            campaignConfig.forEach(c -> campaignToCapacity.put(c.getCampaignId(), c.getCapacity()));
        }
    }

    public void updateProfileCampaign(Integer profileId, Integer campaignId) {
        int capacity = campaignToCapacity.get(campaignId) - 1;
        campaignToCapacity.put(campaignId, capacity);
        if (campaignToCapacity.get(campaignId) == 0)
            cappedCampaigns.add(campaignId);
    }

    public boolean isCampaignCapped(Integer campaignId) {
        return cappedCampaigns.contains(campaignId);
    }

    public boolean isProfileAttributeExist(Integer profileId) {
        return profileToAttribute.containsKey(profileId);
    }

    public Set<Integer> getProfileAttributes(Integer profileId) {
        return profileToAttribute.get(profileId);
    }

    public void setProfileAttributes(Integer profileId, Set<Integer> profileAttributes) {
        if (!profileToAttribute.containsKey(profileId))
            profileToAttribute.put(profileId, profileAttributes);
    }

    public  String getCampaignByAttributes(Integer profileId,Set<Integer> attributes) {

        Set<Integer> relevantCampaigns = getRelevantCampaignsIdByAttributes(attributes);

        if (relevantCampaigns.size() == 0)
            return UNMATCHED;

        return processRelevantCampaigns(profileId, relevantCampaigns);
    }

    private synchronized String processRelevantCampaigns(Integer profileId, Set<Integer> relevantCampaigns) {
        Optional<String> campaignId = campaignConfig.stream()
                .filter(c -> relevantCampaigns.contains(c.getCampaignId()) && !isCampaignCapped(c.getCampaignId()))
                .map(c -> String.valueOf(c.getCampaignId()))
                .findFirst();

        return getRelevantCampaign(profileId, campaignId);
    }

    private Set<Integer> getRelevantCampaignsIdByAttributes(Set<Integer> attributes) {
        Set<Integer> items = new HashSet<>();
        return attributes.stream().map(a -> attributeToCampaign.get(a))
                .flatMap(List::stream)
                .filter(n -> !items.add(n)).collect(Collectors.toSet());
    }

    private String getRelevantCampaign(Integer profileId, Optional<String> campaignId) {
        if (campaignId.isPresent()) {
            updateProfileCampaign(profileId, Integer.valueOf(campaignId.get()));
            return campaignId.get();
        } else {
            return CAPPED;
        }
    }
}
