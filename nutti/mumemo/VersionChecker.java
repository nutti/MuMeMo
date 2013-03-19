package nutti.mumemo;

public class VersionChecker
{
	private boolean				m_MajorMatched = false;
	private boolean				m_MinorUMatched = false;
	private boolean				m_MinorMMatched = false;
	private boolean				m_MinorLMatched = false;

	public VersionChecker()
	{
	}

	public void checkVersion( long version )
	{
		long major = ( version >> 24 ) & 0xF;
		long minorUpper = ( version >> 16 ) & 0xF;
		long minorMiddle = ( version >> 8 ) & 0xF;
		long minorLower = version & 0xF;

		if( major == Constant.MAJOR_VERSION ){
			m_MajorMatched = true;
		}
		if( minorUpper == Constant.MINOR_UPPER_VERSION ){
			m_MinorUMatched = true;
		}
		if( minorMiddle == Constant.MINOR_MIDDLE_VERSION ){
			m_MinorMMatched = true;
		}
		if( minorLower == Constant.MINOR_LOWER_VERSION ){
			m_MinorLMatched = true;
		}
	}

	public boolean majorVerMatched()
	{
		return m_MajorMatched;
	}

	public boolean minorVerUMatched()
	{
		return m_MinorUMatched;
	}

	public boolean minorVerMMatched()
	{
		return m_MinorMMatched;
	}

	public boolean minorVerLMatched()
	{
		return m_MinorLMatched;
	}
}
