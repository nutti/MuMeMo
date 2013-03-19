package nutti.lib.sound;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.SSLEngineResult.Status;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;

import nutti.lib.sound.MusicPlayer.AudioInfo;
import nutti.lib.sound.MusicPlayer.StatusFlag;

import org.tritonus.share.sampled.TAudioFormat;
import org.tritonus.share.sampled.file.TAudioFileFormat;


public class MusicPlayerCore implements Runnable
{

	private StatusFlag						m_Status;
	private String						m_FileName;
	private Map							m_AudioInfoOrig;

	private AudioInfo					m_AudioInfo;

	private AudioInputStream			m_AudioStreamOrig = null;
	private AudioFormat 				m_AudioFmt = null;
	private File						m_File;
	private AudioInputStream			m_AudioStream;

	private FloatControl				m_PanCtrl;			// パン制御
	private FloatControl				m_VolumeCtrl;		// 音量制御

	private SourceDataLine				m_SrcDataLine;

	private long						m_EncodedTotalBytes;

	private IMusicPlayerListener		m_MusicPlayerListner;

	public MusicPlayerCore()
	{
		m_Status = StatusFlag.STOP;
		m_AudioInfo = new AudioInfo();

		m_PanCtrl = null;
		m_VolumeCtrl = null;

		Thread thread = new Thread( this );
		thread.start();
	}

	private Map copy( Map src )
	{
		HashMap dest = new HashMap();
		if( src != null ){
			Iterator it = src.keySet().iterator();
			while ( it.hasNext() ){
				Object key = it.next();
				Object value = src.get( key );
				dest.put( key, value );
			}
		}
		return dest;
	}

	private void setupAudioInfo()
	{
		Object obj;
		// 現在の音楽再生位置を取得（秒単位）
		obj = m_AudioInfoOrig.get( "audio.length.bytes" );
		m_AudioInfo.m_Length = obj != null ? Long.parseLong( obj.toString() ) : 0;
		// 曲名の取得
		obj = m_AudioInfoOrig.get( "title" );
		m_AudioInfo.m_Title = obj != null ? obj.toString() : "";
		// 作曲者の取得
		obj = m_AudioInfoOrig.get( "author" );
		m_AudioInfo.m_Composer = obj != null ? obj.toString() : "";
		// ファイルタイプを取得
		obj = m_AudioInfoOrig.get( "audio.type" );
		String type = obj != null ? obj.toString() : "Unknown Type";
		// .mp3の場合
		if( type.equals( "MP3" ) ){
			// ビットレートの取得
			obj = m_AudioInfoOrig.get( "mp3.bitrate.nominal.bps" );
			m_AudioInfo.m_BitRate = obj != null ? Long.parseLong( obj.toString() ) : 0;
			// チャンネル数の取得
			obj = m_AudioInfoOrig.get( "mp3.channels" );
			m_AudioInfo.m_Channel = obj != null ? Long.parseLong( obj.toString() ) : 0;
			// CBR or VBR
			obj = m_AudioInfoOrig.get( "mp3.vbr" );
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
			obj = m_AudioInfoOrig.get( "mp3.frequency.hz" );
			m_AudioInfo.m_Freq = obj != null ? ( long ) ( Double.parseDouble( obj.toString() ) ) : 0;
			// ファイルフォーマットの取得
			obj = m_AudioInfoOrig.get( "mp3.version.encoding" );
			m_AudioInfo.m_Format = obj != null ? obj.toString() : "";
		}
	}

	private void playRawWavData( AudioFormat fmt ) throws LineUnavailableException, IOException
	{
		byte[] data = new byte[ 16000 ];
		m_SrcDataLine = getSrcLine( fmt );		// オーディオバッファ
		m_Status = StatusFlag.PLAY;
		if( m_SrcDataLine != null ){
			// 再生開始
			m_SrcDataLine.start();
			int readBytes = 0;
			int writeBytes = 0;
			// ストリーミング再生を行う。
			while( readBytes != -1 && ( m_Status == StatusFlag.PLAY || m_Status == StatusFlag.PAUSE || m_Status == StatusFlag.SEEK ) ){
				// 再生中だけどポーズの場合
				while( m_Status == StatusFlag.PAUSE || m_Status == StatusFlag.SEEK ){
					try{
						m_SrcDataLine.flush();
						Thread.sleep( 1 );
					}
					catch( InterruptedException e ){
						e.printStackTrace();
					}
				}
				try{
					synchronized( m_AudioStream ){
						readBytes = m_AudioStream.read( data, 0, data.length );	// ファイルからオーディオデータを読み込む
					}
					if( readBytes != -1 ){
						writeBytes = m_SrcDataLine.write( data, 0, readBytes );	// 再生

						// 読み込んだバイト数を取得
						long encodedBytes = -1;
						if( m_File instanceof File ){
							try{
								if( m_AudioStreamOrig != null ){
									encodedBytes = m_EncodedTotalBytes - ( (long) m_AudioStreamOrig.available() );
								}
							}
							catch( IOException e ){
								e.printStackTrace();
							}
						}


						m_MusicPlayerListner.progress( encodedBytes, /*m_SrcDataLine.getMicrosecondPosition()*/0, null, m_AudioInfo );
					}
				}
				catch( NullPointerException e ){
				}

			}
			// 再生停止
			if( m_Status == StatusFlag.PLAY || m_Status == StatusFlag.PLAYING ){
				m_SrcDataLine.drain();		// オーディオバッファ上のデータ全て再生されるまで待つ
			}
			else{
				m_SrcDataLine.flush();
			}
			m_SrcDataLine.stop();
			m_SrcDataLine.close();
			m_AudioStream.close();
			m_SrcDataLine = null;
			if( readBytes == -1 ){
				updateStatus( StatusFlag.EOF );
			}
			else if( m_Status == StatusFlag.STOPING ){
				updateStatus( StatusFlag.STOP );
			}
		}
	}

	private SourceDataLine getSrcLine( AudioFormat fmt ) throws LineUnavailableException
	{
		SourceDataLine line = null;
		DataLine.Info info = new DataLine.Info( SourceDataLine.class, fmt );
		line = (SourceDataLine) AudioSystem.getLine( info );
		line.open( fmt );

		return line;
	}

	public void run()
	{
		try{

			while( true ){
				if( m_Status == StatusFlag.TERM ){
					break;
				}
				if( m_Status == StatusFlag.PLAY || m_Status == StatusFlag.PLAYING ){
					playRawWavData( m_AudioFmt );
				}
				if( m_Status == StatusFlag.EOF ){
				}
				if( m_Status == StatusFlag.STOPING ){
					updateStatus( StatusFlag.STOP );
				}
				Thread.sleep( 1 );
			}
		}
		catch( InterruptedException e ){
			e.printStackTrace();
		}
		catch( LineUnavailableException e ){
			e.printStackTrace();
		}
		catch( IOException e ){
			e.printStackTrace();
		}
	}

	public void play()
	{
		try {
			synchronized( m_AudioStream ){
				m_AudioStreamOrig = AudioSystem.getAudioInputStream( m_File );
				m_AudioStream = AudioSystem.getAudioInputStream( m_AudioFmt, m_AudioStreamOrig );
			}
		}
		catch( UnsupportedAudioFileException e ){
			e.printStackTrace();
		}
		catch( IOException e ){
			e.printStackTrace();
		}

		updateStatus( StatusFlag.PLAYING );

		while( m_Status != StatusFlag.PLAY ){
			try{
				Thread.sleep( 1 );
			}
			catch( InterruptedException e ){
				e.printStackTrace();
			}
		}
	}

	public void stop()
	{
		// ※要修正！！ ストップ関連がおかしいために、リピート再生が出来ない
		if( m_Status == StatusFlag.STOP || m_Status == StatusFlag.EOF ){
			updateStatus( StatusFlag.STOP );
			return;
		}
		updateStatus( StatusFlag.STOPING );
		while( m_Status != StatusFlag.STOP ){
			try{
				Thread.sleep( 1 );
			}
			catch( InterruptedException e ){
				e.printStackTrace();
			}
		}
	}

	public void open( String fileName )
	{

		close();

		m_FileName = fileName;

		try{
			m_File = new File( fileName );
			m_AudioStreamOrig = AudioSystem.getAudioInputStream( m_File );

			AudioFileFormat audioFmt = AudioSystem.getAudioFileFormat( m_File );
			AudioFormat fmt = m_AudioStreamOrig.getFormat();



			if( audioFmt instanceof TAudioFileFormat ){
				m_AudioInfoOrig = ( (TAudioFileFormat) audioFmt ).properties();
				m_AudioInfoOrig = copy( m_AudioInfoOrig );
			}
			else{
				m_AudioInfoOrig = new HashMap();
			}
			if( audioFmt.getByteLength() > 0 ){
				m_AudioInfoOrig.put( "audio.length.bytes", new Integer(audioFmt.getByteLength() ) );
			}
			if( audioFmt.getFrameLength() > 0 ){
				m_AudioInfoOrig.put( "audio.length.frames", new Integer( audioFmt.getFrameLength() ) );
			}
			if( audioFmt.getType() != null ){
				m_AudioInfoOrig.put( "audio.type", (audioFmt.getType().toString() ) );
			}

			if( fmt.getFrameRate() > 0 ){
				m_AudioInfoOrig.put( "audio.framerate.fps", new Float( fmt.getFrameRate() ) );
			}
			if( fmt.getFrameSize() > 0 ){
				m_AudioInfoOrig.put( "audio.framesize.bytes", new Integer( fmt.getFrameSize() ) );
			}
			if( fmt.getSampleRate() > 0 ){
				m_AudioInfoOrig.put( "audio.samplerate.hz", new Float( fmt.getSampleRate() ) );
			}
			if( fmt.getSampleSizeInBits() > 0 ){
				m_AudioInfoOrig.put( "audio.samplesize.bits", new Integer( fmt.getSampleSizeInBits() ) );
			}
			if( fmt.getChannels() > 0 ){
				m_AudioInfoOrig.put( "audio.channels", new Integer( fmt.getChannels() ) );
			}
			if( fmt instanceof TAudioFormat ){
				Map addProperties = ( (TAudioFormat) fmt ).properties();
				m_AudioInfoOrig.putAll( addProperties );
			}

			setupAudioInfo();


			m_AudioFmt = new AudioFormat(	AudioFormat.Encoding.PCM_SIGNED,
											fmt.getSampleRate(),
											16,
											fmt.getChannels(),
											fmt.getChannels() * 2,
											fmt.getSampleRate(),
											false );


			m_AudioStream = AudioSystem.getAudioInputStream( m_AudioFmt, m_AudioStreamOrig );

			m_EncodedTotalBytes = m_AudioStreamOrig.available();
		}
		catch( Exception e ){
			e.printStackTrace();
		}

		if( m_MusicPlayerListner != null ){
			m_MusicPlayerListner.opened( m_AudioInfo );
		}
	}

	public void close()
	{
		try{
			stop();
			if( m_AudioStreamOrig != null ){
				m_AudioStreamOrig.close();
			}
		//	m_PanCtrl = null;
		//	m_VolumeCtrl = null;
		}
		catch( IOException e ){
			e.printStackTrace();
		}
	}

	public void seek( long pos )
	{
		long skippedTotal = 0;
		long skipped = 0;
		synchronized( m_AudioStream ){
			StatusFlag prevStatus = m_Status;
			updateStatus( StatusFlag.SEEK );
			m_SrcDataLine.flush();
			try{
				m_AudioStreamOrig = AudioSystem.getAudioInputStream( m_File );
				m_AudioStream = AudioSystem.getAudioInputStream( m_AudioFmt, m_AudioStreamOrig );
			//	m_PanCtrl = null;
			//	m_VolumeCtrl = null;
				while( skippedTotal < pos ){
					skipped = m_AudioStream.skip( pos - skippedTotal );

					if ( skipped == 0 ){
						break;
					}
					skippedTotal = skippedTotal + skipped;
				}
			}
			catch( IOException e ){
				e.printStackTrace();
			}
			catch( UnsupportedAudioFileException e ){
				e.printStackTrace();
			}
			updateStatus( prevStatus );
		}
	}

	public void pause()
	{
		if( m_Status == StatusFlag.PLAY ){
			updateStatus( StatusFlag.PAUSE );
		}
	}

	public void resume()
	{
		if( m_Status == StatusFlag.PAUSE ){
			updateStatus( StatusFlag.PLAY );
		}
	}

	public AudioInfo getAudioInfo()
	{
		return m_AudioInfo;
	}

	// パンの設定（スレッドセーフ）
	public void setPan( double pan )
	{
		//if( m_PanCtrl == null ){
			if ( ( m_SrcDataLine != null ) && ( m_SrcDataLine.isControlSupported( FloatControl.Type.PAN ) ) ){
				m_PanCtrl = (FloatControl) m_SrcDataLine.getControl( FloatControl.Type.PAN );
			 }
		//}
		if( m_PanCtrl == null ){
			return;
		}
		m_PanCtrl.setValue( (float) pan );
	}

	// 音量の設定（スレッドセーフ）
	public void setVolume( double volume )
	{

		//if( m_VolumeCtrl == null ){
			if ( ( m_SrcDataLine != null ) && ( m_SrcDataLine.isControlSupported( FloatControl.Type.MASTER_GAIN ) ) ){
				m_VolumeCtrl = (FloatControl) m_SrcDataLine.getControl( FloatControl.Type.MASTER_GAIN );
			}
		//}
		if( m_VolumeCtrl == null ){
			return;
		}

		double min = m_VolumeCtrl.getMinimum();
		double amp = ( ( 10.0f / 20.0f ) *  m_VolumeCtrl.getMaximum() ) - m_VolumeCtrl.getMinimum();
		double mul = Math.log( 10.0 ) / 20;
		double value = min + ( 1 / mul ) * Math.log( 1 + ( Math.exp( mul * amp ) - 1) * volume );
		m_VolumeCtrl.setValue( (float) value );

	}

	// 状態の取得
	public StatusFlag getStatus()
	{
		return m_Status;
	}

	// 状態の更新
	private void updateStatus( StatusFlag status )
	{
		m_Status = status;
		if( m_MusicPlayerListner != null ){
			m_MusicPlayerListner.statusUpdated( status );
		}
	}

	public void setMusicPlayerListener( IMusicPlayerListener mpl )
	{
		m_MusicPlayerListner = mpl;
	}

	public void removeMusicPlayerListener()
	{
		m_MusicPlayerListner = null;
	}
}