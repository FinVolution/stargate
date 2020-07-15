package com.ppdai.stargate.dto;

import lombok.Data;

@Data
public class DeleteResourceResponse extends AbstractResponse {
    public DeleteResourceResponse(int code, String msg) {
        super(code, msg);
    }
}
