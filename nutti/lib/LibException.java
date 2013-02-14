package nutti.lib;

public class LibException extends Throwable
{
	String		m_ErrMsg;

	public LibException( String errMsg )
	{
		m_ErrMsg = errMsg;
	}

	public void printErrorMsg()
	{
		System.out.println( m_ErrMsg );
	}
}
