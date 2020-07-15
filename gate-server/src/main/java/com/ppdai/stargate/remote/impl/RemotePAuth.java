package com.ppdai.stargate.remote.impl;

import com.ppdai.auth.common.constant.GrantType;
import com.ppdai.pauth.client.ApiException;
import com.ppdai.pauth.client.api.OAuth2EndpointApi;
import com.ppdai.pauth.client.model.OAuth2AccessToken;
import com.ppdai.stargate.remote.RemoteAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RemotePAuth implements RemoteAuth {

    @Autowired
    private OAuth2EndpointApi pAuthApi;

    @Override
    public String getAccessToken(String appAuth) {
        String accessToken = null;

        try {
            OAuth2AccessToken o2AccessToken = pAuthApi.issueTokenUsingPOST(GrantType.CLIENT_CREDENTIALS.name(), appAuth, null, null, null, null, null);
            accessToken = o2AccessToken.getAccessToken();

        } catch (ApiException e) {
            log.error("A exception is happened when trying to get a access token from pauth.", e.getMessage());
        } finally {
        }

        return accessToken;
    }

}
