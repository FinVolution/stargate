package com.ppdai.stargate.utils;

import com.ppdai.pauth.client.ApiException;
import com.ppdai.pauth.client.api.OAuth2EndpointApi;
import com.ppdai.pauth.client.model.ValidityVO;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.env.Environment;

import javax.servlet.http.HttpServletRequest;

public class TokenValidator {

    private OAuth2EndpointApi pAuthApi;
    private Environment environment;

    public TokenValidator(OAuth2EndpointApi pAuthApi, Environment environment) {
        this.pAuthApi = pAuthApi;
        this.environment = environment;
    }

    public void checkJwtToken(HttpServletRequest httpServletRequest) {
        boolean isTokenValid = false;

        String tokenName = environment.getProperty("pauth.spring.filter.token.name", "jwt-token");
        String jwtToken = httpServletRequest.getHeader(tokenName);
        if (StringUtils.isEmpty(jwtToken)) {
            throw BaseException.newException(MessageType.ERROR, "header中jwt-token为空");
        } else {
            try {
                ValidityVO validityVO = pAuthApi.introspectTokenUsingPOST(jwtToken);
                isTokenValid = validityVO.getIsValid();
            } catch (ApiException e) {
                throw BaseException.newException(MessageType.ERROR, "校验jwtToken失败, err=" + e.getMessage());
            }

            if (!isTokenValid) {
                throw BaseException.newException(MessageType.ERROR, "jwtToken无效, token=%s", jwtToken);
            }
        }
    }
}
