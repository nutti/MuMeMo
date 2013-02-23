package nutti.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class MultipleRunChecker
{
	private String			m_LockFileName;		// ロック用ファイル名
	private FileChannel		m_FC;
	private FileLock		m_FLock;

	public MultipleRunChecker( String lockFileName )
	{
		m_LockFileName = lockFileName;
	}

	public boolean detectMultipleRun() throws IOException
	{
		m_FC = new FileOutputStream( new File( m_LockFileName ) ).getChannel();
		m_FLock = m_FC.tryLock();
		if( m_FLock == null ){
			return true;
		}

		return false;
	}

	public void terminate() throws IOException
	{
		m_FLock.release();
		m_FC.close();
		File file = new File( m_LockFileName );
		file.delete();
	}
}
