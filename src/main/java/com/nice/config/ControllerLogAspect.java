package com.nice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nice.entity.AppConstants;
import com.nice.entity.JsonUtil;
import com.nice.entity.LogEntity;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(1)
public class ControllerLogAspect {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    ThreadLocal<Long> startTime = new ThreadLocal<>();
    ThreadLocal<LogEntity> webLogThreadLocal = new ThreadLocal<>();
    @Pointcut("execution(* com.nice.controller.*.*(..))")
    public void serviceAspect(){
    }
    @Before("serviceAspect()")
    public void deBefore(JoinPoint joinPoint){
        //接收到请求，记录请求内容
        startTime.set(System.currentTimeMillis());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        webLogThreadLocal.set(new LogEntity());
        webLogThreadLocal.get().setHttpMethod(request.getMethod());
        webLogThreadLocal.get().setClassMethod(joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName());
        webLogThreadLocal.get().setUrl(request.getRequestURL().toString());
        webLogThreadLocal.get().setIp(request.getRemoteAddr());
        webLogThreadLocal.get().setArgs(AppUtil.getArts(joinPoint));
        webLogThreadLocal.get().setLogType(AppConstants.LOG_TYPE_HTTP);
        webLogThreadLocal.get().setReqParams(JsonUtil.objectToJson(request.getParameterMap()));
        webLogThreadLocal.get().setUser(AppUtil.getUser(request));
    }
    @AfterReturning(returning = "result",pointcut = "serviceAspect()")
    public void doAfterReturning(Object result){
        //处理完请求，返回内容
        ObjectMapper mapper = new ObjectMapper();
        try {
            webLogThreadLocal.get().setRespParams(mapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            logger.error(AppUtil.getExceptionDetail(e));
        }
        webLogThreadLocal.get().setSpendTime(System.currentTimeMillis() - startTime.get());
        try{
            logger.info(">>>" + mapper.writeValueAsString(webLogThreadLocal.get()));
        } catch (JsonProcessingException e) {
            logger.error(AppUtil.getExceptionDetail(e));
        }
    }
}
