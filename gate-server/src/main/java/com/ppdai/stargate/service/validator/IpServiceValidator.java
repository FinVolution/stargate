package com.ppdai.stargate.service.validator;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.vi.AddIpRequestVI;
import org.springframework.stereotype.Component;

@Component
public class IpServiceValidator {

    public boolean isIp(String IP) {
        boolean b = false;

        if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String[] s = IP.split("\\.");
            if (Integer.parseInt(s[0]) < 255)
                if (Integer.parseInt(s[1]) < 255)
                    if (Integer.parseInt(s[2]) < 255)
                        if (Integer.parseInt(s[3]) < 255)
                            b = true;
        }
        return b;
    }

    public boolean isSystemUseIp(String IP) {
        if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.[123]{1}")) {
            return false;
        }

        return true;
    }

    public void AddIpValidator(AddIpRequestVI addIpRequestVI) {
        if (!isIp(addIpRequestVI.getNetworkSegment())) {
            throw BaseException.newException(MessageType.ERROR, "网段格式错误, 请使用IP");
        }

        if (addIpRequestVI.getMinIp() < 4) {
            throw BaseException.newException(MessageType.ERROR, "IP范围错误, 请勿使用系统使用IP");
        }
    }
}
