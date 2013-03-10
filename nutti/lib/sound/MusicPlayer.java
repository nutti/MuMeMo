package nutti.lib.sound;

import java.io.File;
import java.util.Map;

import javax.swing.plaf.basic.BasicPanelUI;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;


public class MusicPlayer
{
	public enum StatusFlag
	{
		PLAY,
		STOP,
		EOF,
		PAUSE,
		SEEK,
		TERM,
		STATUS_FLAG_TOTAL
	}

	private MusicPlayerCore			m_Player;
	private AudioInfo				m_AudioInfo;

	//private BasicPlayer				m_Player;

	private IMusicPlayerListener	m_PlayerListener;


	public static class AudioInfo
	{
		public long			m_Length;		// 音楽の長さ
		public long			m_Freq;			// 周波数
		public long			m_Bits;			// ビット数
		public long			m_BitRate;		// ビットレート
		public String		m_Format;		// ファイルフォーマット
		public long			m_Channel;		// チャンネル数
		public String		m_Composer;		// 作曲家
		public String		m_Title;		// タイトル
		public long			m_IsCBR;		// CBR形式ならtrue
	}


	private BasicPlayerListener		m_BasicListener = new BasicPlayerListener()
	{
		public void stateUpdated( BasicPlayerEvent event )
		{

			if( event.getCode() == event.EOM ){
				if( m_PlayerListener != null ){
					m_PlayerListener.statusUpdated(StatusFlag.EOF );
				}
			}
			else{
				if( m_PlayerListener != null ){
					m_PlayerListener.statusUpdated( getStatus() );
				}
			}
		}
		public void opened( Object stream, Map properties )
		{
			Object obj;
			// 現在の音楽再生位置を取得（秒単位）
			obj = properties.get( "audio.length.bytes" );
			m_AudioInfo.m_Length = obj != null ? Long.parseLong( obj.toString() ) : 0;
			// 曲名の取得
			obj = properties.get( "title" );
			m_AudioInfo.m_Title = obj != null ? obj.toString() : "";
			// 作曲者の取得
			obj = properties.get( "author" );
			m_AudioInfo.m_Composer = obj != null ? obj.toString() : "";
			// ファイルタイプを取得
			obj = properties.get( "audio.type" );
			String type = obj != null ? obj.toString() : "Unknown Type";
			// .mp3の場合
			if( type.equals( "MP3" ) ){
				// ビットレートの取得
				obj = properties.get( "mp3.bitrate.nominal.bps" );
				m_AudioInfo.m_BitRate = obj != null ? Long.parseLong( obj.toString() ) : 0;
				// チャンネル数の取得
				obj = properties.get( "mp3.channels" );
				m_AudioInfo.m_Channel = obj != null ? Long.parseLong( obj.toString() ) : 0;
				// CBR or VBR
				obj = properties.get( "mp3.vbr" );
				if( obj != null ){
					if( Boolean.parseBoolean( obj.toString() ) == false ){
						m_AudioInfo.m_IsCBR = 1;
					}
					else{
						m_AudioInfo.m_IsCBR = 0;
					}
				}
				else{
					m_AudioInfo.m_IsCBR = -1;
				}
				// サンプルレートの取得
				obj = properties.get( "mp3.frequency.hz" );
				m_AudioInfo.m_Freq = obj != null ? ( long ) ( Double.parseDouble( obj.toString() ) ) : 0;
				// ファイルフォーマットの取得
				obj = properties.get( "mp3.version.encoding" );
				m_AudioInfo.m_Format = obj != null ? obj.toString() : "";
			}

			if( m_PlayerListener != null ){
				m_PlayerListener.opened( m_AudioInfo );
			}

		}
		public void progress( int bytesread, long microseconds, byte[] pcmdata, Map properties )
		{
			if( m_PlayerListener != null ){
				m_PlayerListener.progress( bytesread, microseconds, pcmdata, m_AudioInfo );
			}
		}
		public void setController( BasicController controller )
		{
		}
	};

	public MusicPlayer()
	{
		m_Player = new MusicPlayerCore();
		//m_Player = new BasicPlayer();
		m_AudioInfo = new AudioInfo();
		//m_Player.addBasicPlayerListener( m_BasicListener );
	}

	public void play()
	{
		try{
			m_Player.play();
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}

	public void stop()
	{
		try{
			m_Player.stop();
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}

	public void open( String fileName )
	{
		//File file = new File( fileName );
		try{
			//m_Player.open( file );
			m_Player.open( fileName );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}

	public void close()
	{
		stop();
	}

	public void seek( long pos )
	{
		try{
			m_Player.seek( pos );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}

	public void pause()
	{
		try{
			m_Player.pause();
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}

	public void resume()
	{
		try{
			m_Player.resume();
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}

	public AudioInfo getAudioInfo()
	{
		return m_AudioInfo;
		//return m_Player.getAudioInfo();
	}

	public void setPan( double pan )
	{
		try{
			m_Player.setPan( pan );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}

	public void setVolume( double volume )
	{
		try{
			//m_Player.setGain( volume );
			m_Player.setVolume( volume );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}

	public StatusFlag getStatus()
	{
		StatusFlag[] map = new StatusFlag [ BasicPlayer.SEEKING + 2 ];

		map[ BasicPlayer.PLAYING + 1 ] = StatusFlag.PLAY;
		map[ BasicPlayer.STOPPED  + 1 ] = StatusFlag.STOP;
		map[ BasicPlayer.SEEKING + 1 ] = StatusFlag.SEEK;
		map[ BasicPlayer.PAUSED + 1 ] = StatusFlag.PAUSE;

		//return map[ m_Player.getStatus() + 1 ];
		return m_Player.getStatus();
	}

	public void setMusicPlayerListener( IMusicPlayerListener mpl )
	{
		//m_PlayerListener = mpl;
		m_Player.setMusicPlayerListener( mpl );
	}

	public void removeMusicPlayerListener()
	{
		m_PlayerListener = null;
		m_Player.removeMusicPlayerListener();
	}
}
