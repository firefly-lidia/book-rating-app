package com.lp.book.rating.app.controller.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice(basePackageClasses = AuthenticationController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class AuthenticationExceptionHandler {





}
