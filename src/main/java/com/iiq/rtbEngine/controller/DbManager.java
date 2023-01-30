package com.iiq.rtbEngine.controller;

import com.iiq.rtbEngine.db.*;
import com.iiq.rtbEngine.models.CampaignConfig;
import com.iiq.rtbEngine.util.FilesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class DbManager {

	@Autowired
	CampaignsConfigDao campaignsConfigDao;

	@Autowired
	CampaignsDao campaignsDao;

	@Autowired
	ProfilesDao profilesDao;

	@Autowired
	RTBCache rtbCache;

	@Autowired
	CampaignCapacityDao profileCampaignCapacityDao;

	private static final String CSV_DELIM = ",";
	private static final String BASE_PATH = "./resources/";    //should be D:/ on AWS workspace
	private static final String CAMPAIGNS_TABLE_INITIALIZATION_FILE = "campaigns_init.csv";
	private static final String CAMPAIGNS_CAPACITY_TABLE_INITIALIZATION_FILE = "campaign_config_init.csv";

	@PostConstruct
	public void init() {
		//populate Campaigns table from file
		initCampaignsTable();

		//populate Campaign capacity table from file
		initCampaignCapacityTable();

		//init Profile table
		initProfilesTable();

		//init RTB cache
		initRTBCache();

	}

	private void initRTBCache() {

		rtbCache.initAttributeToCampaign(getAllCampaignAttributes());
		rtbCache.initProfileCampaignCapacityDao(new ArrayList<>(campaignsConfigDao.getAllCampaignsConfigs().values()));

	}

	private void initCampaignsTable() {
		// create table in DB
		campaignsDao.createTable();

		// read initialization file
		List<String> lines = FilesUtil.readLinesFromFile(BASE_PATH + CAMPAIGNS_TABLE_INITIALIZATION_FILE);

		// insert campaigns capacities into DB
		for (String line : lines) {
			String[] values = line.split(CSV_DELIM);
			campaignsDao.updateTable(values[0], values[1]);
		}
	}

	private void initCampaignCapacityTable() {

		// create table in DB
		campaignsConfigDao.createTable();

		// read initialization file
		List<String> lines = FilesUtil.readLinesFromFile(BASE_PATH + CAMPAIGNS_CAPACITY_TABLE_INITIALIZATION_FILE);

		// insert campaigns capacities into DB
		for (String line : lines) {
			String[] values = line.split(CSV_DELIM);
			campaignsConfigDao.updateTable(values[0], values[1], values[2]);
		}
	}

	private void initProfilesTable() {
		// create table in DB
		profilesDao.createTable();
	}

	/*****************************************************************************************************************************************
	 ****************************************						DEVELOPER API							*********************************
	 ****************************************************************************************************************************************/

	/**
	 * An API for getting the capacity that is configured for a certain campaign
	 *
	 * @param campaignId
	 * @return the capacity configured for the given campaign, or null in case the given campaignId does not
	 * have any campaign configuration
	 */
	public Integer getCampaignCapacity(int campaignId) {
		return campaignsConfigDao.getCampaignCapacity(campaignId);
	}

	/**
	 * An API for getting the priority that is configured for a certain campaign
	 *
	 * @param campaignId
	 * @return the priority configured for the given campaign, or null in case the given campaignId does not
	 * have any campaign configuration
	 */
	public Integer getCampaignPriority(int campaignId) {
		return campaignsConfigDao.getCampaignPriority(campaignId);
	}

	/**
	 * An API for getting all the attributes that a certain campaign is targeting
	 *
	 * @param campaignId
	 * @return a set of all the attributes that the given campaign targets, or an empty Set if the given
	 * campaignId does not have any campaign configuration
	 */
	public Set<Integer> getCampaignAttributes(int campaignId) {
		return campaignsDao.getCampaignAttributes(campaignId);
	}

	/**
	 * An API for getting configuration object for a certain campaign
	 *
	 * @param campaignId
	 * @return a CampaignConfig object containing the configuration entities for the given campaignId,
	 * or null in case campaignId does not have any campaign configuration
	 */
	public CampaignConfig getCampaignConfig(int campaignId) {
		return campaignsConfigDao.getCampaignConfig(campaignId);
	}

	/**
	 * An API for getting all the attributes that match a certain profile
	 *
	 * @param profileId
	 * @return a set of all the profile IDs that match the given profile, or an empty set in case the given profile
	 * does not have any attribute IDs that match
	 */
	public Set<Integer> getProfileAttributes(int profileId) {
		return profilesDao.getProfileAttributes(profileId);
	}

	/**
	 * An API for updating Profiles table in DB
	 *
	 * @param profileId
	 * @param attributeId
	 */
	public void updateProfileAttribute(int profileId, int attributeId) {
		profilesDao.updateTable(profileId + "", attributeId + "");
	}

	/**
	 * An API for retrieving from DB all the campaign attributes for every campaign.
	 *
	 * @return a Map s.t. key = campaign ID, value = set of campaign attribute IDs
	 */
	public Map<Integer, List<Integer>> getAllCampaignAttributes() {
		return campaignsDao.getAllCampaignAttributes();
	}

	/**
	 * An API for retrieving from DB the campaign configuration for every campaign.
	 *
	 * @return a Map s.t. key = campaign ID, value = campaign configuration object
	 */
	public Map<Integer, CampaignConfig> getAllCampaignsConfigs() {
		return campaignsConfigDao.getAllCampaignsConfigs();
	}

	/**
	 * An API to get campaign by profile attributes
	 * @param profileId
	 * @return Search result: campaign id, unmatched, capped
	 */
	public String getCampaignByProfile(Integer profileId) {
		if (!rtbCache.isProfileAttributeExist(profileId))
			rtbCache.setProfileAttributes(profileId, profilesDao.getProfileAttributes(profileId));
		Set<Integer> attributes = rtbCache.getProfileAttributes(profileId);
		return rtbCache.getCampaignByAttributes(profileId, attributes);
	}
}
