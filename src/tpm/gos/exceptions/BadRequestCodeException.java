package tpm.gos.exceptions;

public class BadRequestCodeException extends Exception {


	private static final long serialVersionUID = -111025690551653684L;

	public BadRequestCodeException() {}

	public BadRequestCodeException(String paramString)
	{
		super(paramString);
	}

	public BadRequestCodeException(String paramString, Throwable paramThrowable)
	{
		super(paramString, paramThrowable);
	}

	public BadRequestCodeException(Throwable paramThrowable)
	{
		super(paramThrowable);
	}
	
}
