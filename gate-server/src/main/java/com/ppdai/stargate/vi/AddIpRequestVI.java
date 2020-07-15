package com.ppdai.stargate.vi;

import lombok.Data;

@Data
public class AddIpRequestVI {

   private String network;

   private String networkSegment;

   private int minIp ;

   private int maxIp;
}
