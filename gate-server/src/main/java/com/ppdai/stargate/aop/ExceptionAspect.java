package com.ppdai.stargate.aop;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
@Slf4j
@Order(11)
public class ExceptionAspect {

    @Around("ResourcePointCuts.apiController()")
    public Object handleException(ProceedingJoinPoint apiMethod) {
        Object retVal = null;
        try {
            retVal = apiMethod.proceed();
        } catch (BaseException e) {
            log.info(e.getMessage());
            retVal = Response.mark(e.getMessageType(), e.getMessage());
        } catch (Throwable throwable) {
            String class_method = apiMethod.getSignature().getDeclaringTypeName() + "." + apiMethod.getSignature().getName();
            String class_method_args = Arrays.toString(apiMethod.getArgs());
            UUID uuid = UUID.randomUUID();
            String msg = String.format("[%s] %s, class_method=%s, class_method_args=%s", uuid, throwable.getMessage(), class_method, class_method_args);
            log.error(msg, throwable);

            String stackTrace = ExceptionUtils.getStackTrace(throwable);
            stackTrace = stackTrace.length() > 600 ? stackTrace.substring(0, 599) : stackTrace;
            retVal = Response.emark(MessageType.UNKNOWN,
                    "未知错误，请联系负责团队寻求更多帮助，定位GUID为[" + uuid + "]。",
                    stackTrace);
        }

        return retVal;
    }

}
