package com.ppdai.stargate.po.converter;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;


@Slf4j
@Converter(autoApply = true)
public class ListToJsonStringConverter implements AttributeConverter<List<EnvUrl>, String> {


    @Override
    public String convertToDatabaseColumn(List<EnvUrl> listUrls) {
        try {
            return JSON.toJSONString(listUrls);
        } catch (Exception e) {
            log.info("transforming list to JSON string error：{}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<EnvUrl> convertToEntityAttribute(String dbData) {

        try {
            return JSON.parseArray(dbData, EnvUrl.class);
        } catch (Exception e) {
            log.info("transforming JSON string to List error: {}", e.getMessage());
            return Lists.newArrayList();
        }

    }
}
