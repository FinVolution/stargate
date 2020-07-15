package com.ppdai.stargate.dto;

import lombok.Data;

@Data
public class AddStaticResourceResponse extends AbstractResponse {
    public AddStaticResourceResponse(int code, String msg) {
        super(code, msg);
    }
}
