package com.ppdai.stargate.aop;

import org.aspectj.lang.annotation.Pointcut;

public class ResourcePointCuts {

    @Pointcut("execution(public * com.ppdai.stargate.controller..*.*(..))")
    public void apiController(){}

    @Pointcut("execution(public * com.ppdai.stargate.controller..*.post*(..))")
    public void securityController(){}
}
