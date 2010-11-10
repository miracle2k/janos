package net.sf.janos.web.exception;

public class JanosWebException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9018018944644190573L;

	public JanosWebException() {
		super();
	}
	
	public JanosWebException(String message) {
		super(message);
	}
	
	public JanosWebException(String message, Throwable t) {
		super(message, t);
	}
	
	public JanosWebException(Throwable t) {
		super(t);
	}
	
}
