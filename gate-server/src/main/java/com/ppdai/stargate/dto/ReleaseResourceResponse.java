package com.ppdai.stargate.dto;

import lombok.Data;

@Data
public class ReleaseResourceResponse extends AbstractResponse {
    public ReleaseResourceResponse(int code, String msg) {
        super(code, msg);
    }
}
