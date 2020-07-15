package com.ppdai.stargate.job;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HandlerRegistry<T extends IHandler> {

    private Map<String, T> taskHandlerMap = new ConcurrentHashMap<>();

    public HandlerRegistry(List<T> taskHandlers) {
        if (!CollectionUtils.isEmpty(taskHandlers)) {
            taskHandlerMap = taskHandlers.stream()
                    .collect(Collectors.toMap(IHandler::getName, Function.identity()));
        }
    }

    public void register(T handler) {
        this.taskHandlerMap.put(handler.getName(), handler);
    }

    public Optional<T> getHandler(String name) {
        return Optional.ofNullable(taskHandlerMap.get(name));
    }
}
