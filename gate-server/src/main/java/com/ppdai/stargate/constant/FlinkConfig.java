package com.ppdai.stargate.constant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlinkConfig<T> {

    private String key;
    private T defaultValue;


}