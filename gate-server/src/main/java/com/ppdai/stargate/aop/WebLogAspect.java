package com.ppdai.stargate.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppdai.auth.common.identity.Identity;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.po.AuditLogEntity;
import com.ppdai.stargate.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
@Order(10)
public class WebLogAspect {

    @Autowired
    private AuditService auditService;
    @Value("${pauth.spring.filter.audit.userinfo}")
    public String PAUTH_AUDIT_USERINFO;

    private ObjectMapper mapperObj = new ObjectMapper();

    @Before("ResourcePointCuts.apiController()")
    public void logAccessInfo(JoinPoint joinPoint) throws Throwable {
        //log.info("logAccessInfo");
    }

    @Around("ResourcePointCuts.apiController()")
    public Object logAccessAudit(ProceedingJoinPoint apiMethod) throws Throwable {
        //log.info("logAccessAudit");

        Object retVal = apiMethod.proceed();

        try {
            String userName = null;
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            Object userInfo = request.getAttribute(PAUTH_AUDIT_USERINFO);
            if (userInfo instanceof Identity) {
                Identity identity = (Identity) userInfo;
                userName = identity.getName();
            }

            String http_uri = request.getRequestURI();
            String http_method = request.getMethod();
            String client_ip = request.getRemoteAddr();
            String class_method = apiMethod.getSignature().getDeclaringTypeName() + "." + apiMethod.getSignature().getName();
            String class_method_args = Arrays.toString(apiMethod.getArgs());

            StringBuilder rsLog = new StringBuilder();
            rsLog.append("USER NAME: " + userName);
            rsLog.append(",HTTP_METHOD : " + http_method);
            rsLog.append(",HTTP_URI : " + http_uri);
            rsLog.append(",IP : " + client_ip);
            rsLog.append(",CLASS_METHOD : " + class_method);
            rsLog.append(",CLASS_METHOD_ARGS : " + class_method_args);

            // log.info(rsLog.toString());
            AuditLogEntity action = new AuditLogEntity();
            action.setUserName(userName);
            action.setHttpMethod(http_method);
            action.setHttpUri(http_uri);
            action.setClientIp(client_ip);
            action.setClassMethod(class_method);
            action.setClassMethodArgs(class_method_args);

            if (retVal instanceof Response) {
                Response response = (Response)retVal;
                action.setCode(response.getCode());
            }

            String result = mapperObj.writeValueAsString(retVal);
            result = result.length() > 1024 ? result.substring(0, 1023) : result;
            action.setClassMethodReturn(result);

            // 记录非GET请求访问数据到数据库
            String[] targetMethods = {"POST", "PUT", "DELETE"};
            if (ArrayUtils.contains(targetMethods, http_method)) {
                auditService.recordOperation(action);
            }

        } catch (Exception e) {
            log.info("An exception happened when trying to log access info.");
        }

        return retVal;
    }


}
