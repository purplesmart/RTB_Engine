package com.iiq.rtbEngine.components;

import com.iiq.rtbEngine.util.HandlerType;

public interface IHandler {

    HandlerType getHandlerType();
    Integer getHandleTypeValue();
    String HandleRequest(Integer profileId, Integer attributeId );

}
