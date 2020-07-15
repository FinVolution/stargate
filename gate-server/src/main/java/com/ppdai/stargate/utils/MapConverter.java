package com.ppdai.stargate.utils;

import java.util.Map;

import javax.persistence.AttributeConverter;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapConverter implements AttributeConverter<Map<String, Object>, String> {

    @Override
    public String convertToDatabaseColumn(Map<String, Object> map) {

        try {
            return JSON.toJSONString(map);
        } catch (Throwable ex) {
            log.error("json converter error : {}", ex);
            return null;
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try {
            return JSON.parseObject(dbData, Map.class);
        } catch (Throwable ex) {
            log.error("Unexpected IOEx decoding json from database: {}", dbData);
            return Maps.newHashMap();
        }
    }

}
