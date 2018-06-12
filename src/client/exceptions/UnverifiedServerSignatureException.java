package client.exceptions;

public class UnverifiedServerSignatureException extends Exception {

	private static final long serialVersionUID = -111025690551653684L;

	public UnverifiedServerSignatureException() {}

	public UnverifiedServerSignatureException(String paramString)
	{
		super(paramString);
	}

	public UnverifiedServerSignatureException(String paramString, Throwable paramThrowable)
	{
		super(paramString, paramThrowable);
	}

	public UnverifiedServerSignatureException(Throwable paramThrowable)
	{
		super(paramThrowable);
	}
	
}
