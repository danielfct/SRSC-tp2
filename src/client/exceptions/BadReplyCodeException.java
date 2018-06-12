package client.exceptions;

public class BadReplyCodeException extends Exception {

	private static final long serialVersionUID = -111025690551653684L;

	public BadReplyCodeException() {}

	public BadReplyCodeException(String paramString)
	{
		super(paramString);
	}

	public BadReplyCodeException(String paramString, Throwable paramThrowable)
	{
		super(paramString, paramThrowable);
	}

	public BadReplyCodeException(Throwable paramThrowable)
	{
		super(paramThrowable);
	}
	
}
