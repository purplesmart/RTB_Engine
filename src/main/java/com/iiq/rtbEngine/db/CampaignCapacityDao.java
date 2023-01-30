package com.iiq.rtbEngine.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CampaignCapacityDao {

    @Autowired
    private H2DB h2Db;

    private static final String PROFILE_CAMPAIGN_CAPACITY_TABLE_NAME = "ProfileCampaignCapacity";
    private static final String CAPACITY_COLUMN = "capacity";
    private static final String CAMPAIGN_ID_COLUMN = "campaign_id";
    private static final String CREATE_PROFILE_CAMPAIGN_CAPACITY_TABLE = "CREATE TABLE "+PROFILE_CAMPAIGN_CAPACITY_TABLE_NAME+
            CAMPAIGN_ID_COLUMN + " INTEGER not NULL, " +
            CAPACITY_COLUMN + " INTEGER, " +
            " PRIMARY KEY ( "+CAMPAIGN_ID_COLUMN+"))";
    private static final String UPDATE_STATEMENT = "MERGE INTO "+PROFILE_CAMPAIGN_CAPACITY_TABLE_NAME+" VALUES ( %s, %s)";
    private static final String SELECT_ALL_PROFILE_CAMPAIGN_CAPACITY_STATEMENT = "SELECT * FROM "+PROFILE_CAMPAIGN_CAPACITY_TABLE_NAME;

    public void createTable() {
        try {
            h2Db.executeUpdate(CREATE_PROFILE_CAMPAIGN_CAPACITY_TABLE);
        } catch (SQLException e) {
            System.out.println("Error while trying to create table "+CREATE_PROFILE_CAMPAIGN_CAPACITY_TABLE);
            e.printStackTrace();
        }
    }

    public void updateTable(String campaignId, String capacity) {
        try {
            h2Db.executeUpdate(String.format(UPDATE_STATEMENT, campaignId, capacity));
        } catch (SQLException e) {
            System.out.println("Error while trying to update table "+CREATE_PROFILE_CAMPAIGN_CAPACITY_TABLE+" with campaignId"+campaignId+" capacity"+capacity);
            e.printStackTrace();
        }
    }

    public Map<Integer, Integer> getAllProfileCampaignCapacity() {
        try {
            List<Map<String, String>> result = h2Db.executeQuery(SELECT_ALL_PROFILE_CAMPAIGN_CAPACITY_STATEMENT);
            if(result == null)
                return null;

            Map<Integer,Integer> campaignCapacity = new HashMap<>();

            for(Map<String, String> row : result) {
                Integer campaignId = Integer.parseInt(row.get(CAMPAIGN_ID_COLUMN));
                Integer capacity = Integer.parseInt(row.get(CAPACITY_COLUMN));
                if(!campaignCapacity.containsKey(campaignId))
                    campaignCapacity.put(campaignId,capacity);
            }
            return campaignCapacity;
        } catch (Exception e) {
            System.out.println("Error while trying to retrieve all profile campaign capacity from DB for campaign ");
            e.printStackTrace();
        }
        return null;
    }

}
