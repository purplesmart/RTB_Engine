package com.iiq.rtbEngine.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class HandlerFactory {

    private static Map<Integer, IHandler> handlerMap = null;

    @Autowired
    private HandlerFactory(List<IHandler> handlers) {
        handlerMap = handlers.stream()
                .collect(Collectors.toMap(IHandler::getHandleTypeValue, Function.identity()));
    }

    public IHandler getHandler(int act) {
        return Optional.ofNullable(handlerMap.get(act)).orElseThrow(IllegalArgumentException::new);
    }
}
