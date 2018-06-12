package client.exceptions;

public class UnexceptedServerNounceException extends Exception {

	private static final long serialVersionUID = -111025690551653684L;

	public UnexceptedServerNounceException() {}

	public UnexceptedServerNounceException(String paramString)
	{
		super(paramString);
	}

	public UnexceptedServerNounceException(String paramString, Throwable paramThrowable)
	{
		super(paramString, paramThrowable);
	}

	public UnexceptedServerNounceException(Throwable paramThrowable)
	{
		super(paramThrowable);
	}
	
}
