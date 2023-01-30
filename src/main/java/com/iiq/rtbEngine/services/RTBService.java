package com.iiq.rtbEngine.services;

import com.iiq.rtbEngine.components.HandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RTBService {

    @Autowired
    private HandlerFactory handlerFactory;

    public String HandleRequest( int actionTypeId,Integer attributeId,Integer profileId){
        return handlerFactory.getHandler(actionTypeId).HandleRequest(attributeId, profileId);
    }

}
