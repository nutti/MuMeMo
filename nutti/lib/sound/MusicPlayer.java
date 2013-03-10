package nutti.lib.sound;


public class MusicPlayer
{
	private MusicPlayerCore			m_Player;

	public MusicPlayer()
	{
		m_Player = new MusicPlayerCore();
	}

	public void play()
	{
		m_Player.play();
	}

	public void stop()
	{
		m_Player.stop();
	}

	public void open( String fileName )
	{
		m_Player.open( fileName );
	}

	public void seek( long pos )
	{
		m_Player.seek( pos );
	}

	public void pause()
	{
		m_Player.pause();
	}

	public void resume()
	{
		m_Player.resume();
	}
}
