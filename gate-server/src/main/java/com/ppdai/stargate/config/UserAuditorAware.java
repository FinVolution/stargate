package com.ppdai.stargate.config;

import com.ppdai.auth.common.identity.Identity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class UserAuditorAware implements AuditorAware<String> {

    public static final String DEFAULT_SYSTEM_NAME = "system";

    @Value("${pauth.spring.filter.audit.userinfo}")
    public String PAUTH_AUDIT_USERINFO;

    @Override
    public String getCurrentAuditor() {
        String userName = null;

        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            Object userInfo = requestAttributes.getAttribute(PAUTH_AUDIT_USERINFO, 0);
            if (userInfo instanceof Identity){
                Identity identity = (Identity) userInfo;
                userName = identity.getName();
            }
        } catch (Exception e) {
//            log.info("Not able to read the user name by servlet requests. Probably it's a system call.");
        }

        if (userName == null) {
            userName = DEFAULT_SYSTEM_NAME;
        }

        return userName;
    }

}
