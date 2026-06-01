package jmaster.etm.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.IOException;

@ControllerAdvice
public class CustomErrorHandler {
	@ExceptionHandler(IllegalArgumentException.class)
	public void handleConstraintViolationException(IllegalArgumentException exception,
			ServletWebRequest webRequest) throws IOException {
		webRequest.getResponse().sendError(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
	}
}
