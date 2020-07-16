package com.api.exception;

public class CustomException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String message;

	public CustomException(String message) {
		super(message);
	}

}
