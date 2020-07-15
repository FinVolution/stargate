package com.ppdai.stargate.constant;

public enum JobTypeEnum {

    defaultType("defaultType");

    String description;

    JobTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
