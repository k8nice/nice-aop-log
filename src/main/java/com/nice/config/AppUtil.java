package com.nice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nice.entity.User;
import org.aspectj.lang.JoinPoint;

import javax.servlet.http.HttpServletRequest;

public class AppUtil {
    public static String getArts(JoinPoint joinPoint) {
        return String.valueOf(joinPoint);
    }

    public static User getUser(HttpServletRequest request) {
        return null;
    }

    public static String getExceptionDetail(JsonProcessingException e) {
        return null;
    }

    public static String getArgs(JoinPoint joinPoint) {
        return null;
    }
}
