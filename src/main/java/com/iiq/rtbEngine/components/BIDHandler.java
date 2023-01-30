package com.iiq.rtbEngine.components;

import com.iiq.rtbEngine.controller.DbManager;
import com.iiq.rtbEngine.util.HandlerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BIDHandler implements IHandler{


    @Autowired
    private DbManager dbManager;

    @Override
    public HandlerType getHandlerType() {
        return HandlerType.BID;
    }

    @Override
    public Integer getHandleTypeValue() {
        return 1;
    }

    @Override
    public String HandleRequest(Integer profileId, Integer attributeId) {
        Optional.ofNullable(profileId).orElseThrow(IllegalArgumentException::new);
        return dbManager.getCampaignByProfile(profileId);
    }
}
