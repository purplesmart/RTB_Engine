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
    Map<Integer, Map<Integer, Integer>> campaignToProfileToCapacity = new HashMap<>();
    Map<Integer, Integer> cappedCampaigns = new HashMap<>();
    List<CampaignConfig> campaignConfig;

    @Autowired
    CampaignCapacityDao campaignCapacityDao;

    /**
     * This init the data structure of attributes to campaigns; key attributes and the relevant campaigns
     * @param allCampaignAttributes
     */
    public void initAttributeToCampaign(Map<Integer, List<Integer>> allCampaignAttributes) {
        allCampaignAttributes.forEach((campaignId, attributes) -> attributes.forEach((att) -> {
            if (!attributeToCampaign.containsKey(att))
                attributeToCampaign.put(att, new ArrayList<>());
            attributeToCampaign.get(att).add(campaignId);
        }));
    }

    /**
     * Loading and build campaigns to profile usages to prevent more campaigns showing to profile over the capacity
     * @param campaignConfig
     */
    public void initProfileCampaignCapacityDao(List<CampaignConfig> campaignConfig) {
        this.campaignConfig = campaignConfig;
        CampaignPriorityComparator campaignPriorityComparator = new CampaignPriorityComparator();
        campaignPriorityComparator.thenComparing(CampaignConfig::getCampaignId);
        this.campaignConfig.sort(campaignPriorityComparator);
        campaignCapacityDao.createTable();
        campaignToProfileToCapacity = campaignCapacityDao.getAllProfileCampaignCapacity();
        if (campaignToProfileToCapacity != null) {
            campaignToProfileToCapacity
                    .forEach((campaignId, profileIdAndCapacity) ->
                            profileIdAndCapacity.forEach((profileId, capacity) -> {
                                if (campaignToProfileToCapacity.get(campaignId).get(profileId) == capacity)
                                    cappedCampaigns.put(campaignId, profileId);
                            }));
        } else {
            campaignToProfileToCapacity = new HashMap<>();
            campaignConfig.forEach(c -> {
                if (!campaignToProfileToCapacity.containsKey(c.getCampaignId()))
                    campaignToProfileToCapacity.put(c.getCampaignId(), new HashMap<>());
            });
        }
    }

    /**
     * When campaign sent to the profile, it saved to follow the capacity usage
     * @param campaignId
     * @param profileId
     */
    public void updateProfileCampaign(Integer campaignId, Integer profileId) {
        int capacity = campaignToProfileToCapacity.get(campaignId).get(profileId) - 1;
        campaignCapacityDao.updateTable(campaignId, profileId, capacity);
        campaignToProfileToCapacity.get(campaignId).put(campaignId, capacity);
        if (campaignToProfileToCapacity.get(campaignId).get(profileId) == 0)
            cappedCampaigns.put(campaignId, profileId);
    }

    /**
     * In order to save capacity calculation, the map cappedCampaigns hold the campaign to profile capacity over
     * @param campaignId
     * @param profileId
     * @return
     */
    public boolean isCampaignCapped(Integer campaignId, Integer profileId) {
        return cappedCampaigns.containsKey(campaignId) && cappedCampaigns.get(campaignId) == profileId;
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

    /**
     * 1. Get relevant campaigns by attributes.
     * 2. If there is no campaigns, return UNMATCHED.
     * 3. Else save the campaign capacity and return the campaign.
     * @param profileId
     * @param attributes
     * @return
     */
    public String getCampaignByAttributes(Integer profileId, Set<Integer> attributes) {

        List<Integer> relevantCampaigns = getRelevantCampaignsIdByAttributes(attributes);

        if (relevantCampaigns.size() == 0)
            return UNMATCHED;

        return processRelevantCampaigns(profileId, relevantCampaigns);
    }

    /**
     * Check if relevant campaign used all his capacity on a specific profile.
     * @param profileId
     * @param relevantCampaigns
     * @return
     */
    private synchronized String processRelevantCampaigns(Integer profileId, List<Integer> relevantCampaigns) {
        Optional<String> campaignId = campaignConfig.stream()
                .filter(c -> relevantCampaigns.contains(c.getCampaignId()) && !isCampaignCapped(c.getCampaignId(), profileId))
                .map(c -> String.valueOf(c.getCampaignId()))
                .findFirst();

        return getRelevantCampaign(profileId, campaignId);
    }

    /**
     * Calculating the campaigns by relevant attributes.
     * 1. Get a map the attribute is the key and relevant campaigns as values.
     * 2. Filter if the attribute don't have campaigns.
     * 3. Flat the map to a list of campaigns ( with duplicity ).
     * 4. Group the list by the campaignId.
     * 5. Get the campaigns that match the attributes demand.
     * 6. Return relevant campaigns.
     * @param attributes
     * @return
     */
    private List<Integer> getRelevantCampaignsIdByAttributes(Set<Integer> attributes) {
        return attributes.stream().map(a -> Optional.ofNullable(attributeToCampaign.get(a)))
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList()).stream()
                .flatMap(List::stream).collect(Collectors.groupingBy(e -> e))
                .entrySet().stream()
                .filter(n -> n.getValue().size() == attributes.size())
                .mapToInt(Map.Entry::getKey).boxed().collect(Collectors.toList());
    }

    /**
     * Save the new campaign capacity to the specific profile.
     * @param profileId
     * @param campaignId
     * @return
     */
    private String getRelevantCampaign(Integer profileId, Optional<String> campaignId) {
        if (campaignId.isPresent()) {
            updateProfileCampaign(Integer.valueOf(campaignId.get()), profileId);
            return campaignId.get();
        } else {
            return CAPPED;
        }
    }
}
