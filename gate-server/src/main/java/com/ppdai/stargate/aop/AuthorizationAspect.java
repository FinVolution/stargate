package com.ppdai.stargate.aop;

import com.ppdai.auth.common.identity.Identity;
import com.ppdai.stargate.constant.UserRoleEnum;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Slf4j
@Order(12)
public class AuthorizationAspect {

    @Value("${pauth.spring.filter.audit.userinfo}")
    public String PAUTH_AUDIT_USERINFO;

    @Before("ResourcePointCuts.securityController()")
    public void checkPermissionBeforeSecurityOperations(JoinPoint joinPoint) throws Throwable {

        Identity identity = readUserInfo();
        if (identity != null) {
            String userName = identity.getName();
            String userRole = identity.getRole();
            String[] userRoles = new String[10];

            if (userRole == null) {
                userRole = "null";
            } else {
                userRoles = userRole.split(",");
            }
            if (!ArrayUtils.contains(userRoles, UserRoleEnum.ADMIN.name().toLowerCase())) {
                throw BaseException.newException(MessageType.ERROR,
                        "对不起, 当前用户[%s]的权限为[%s]，而这个操作需要[%s]权限。", userName, userRole, UserRoleEnum.ADMIN.name().toLowerCase());
            }
        } else {
            throw BaseException.newException(MessageType.ERROR, "对不起，无法识别当前的用户身份，请先登录。");
        }

    }

    private Identity readUserInfo() {
        // read user name from request attribute
        Identity identity = null;

        // read user name from request attribute
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = (HttpServletRequest) attributes.getRequest();
        Object userInfo = request.getAttribute(PAUTH_AUDIT_USERINFO);
        if (userInfo instanceof Identity) {
            identity = (Identity) userInfo;
        }

        return identity;
    }

}
