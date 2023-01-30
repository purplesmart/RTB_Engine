package com.iiq.rtbEngine.components;

import com.iiq.rtbEngine.controller.DbManager;
import com.iiq.rtbEngine.util.HandlerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class ProfileAttributeHandler implements IHandler{


    @Autowired
    private DbManager dbManager;

    @Override
    public HandlerType getHandlerType() {
        return HandlerType.ProfileAttribute;
    }

    @Override
    public Integer getHandleTypeValue() {
        return 0;
    }

    @Override
    public String HandleRequest(Integer profileId, Integer attributeId) {
        Optional.ofNullable(profileId).orElseThrow(IllegalArgumentException::new);
        Optional.ofNullable(attributeId).orElseThrow(IllegalArgumentException::new);
        dbManager.updateProfileAttribute(profileId, attributeId);
        return "";
    }
}
