package nutti.mumemo;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;


public class MusicPlayer implements Runnable
{
	private enum StatusFlag
	{
		PLAY,
		TERM,
		STATUS_FLAG_TOTAL
	}

	private BitSet			m_StatusFlags;
	private String			m_FileName;

	public MusicPlayer()
	{
		m_StatusFlags = new BitSet( StatusFlag.STATUS_FLAG_TOTAL.ordinal() );
	}

	private void playMP3( String fileName )
	{
		try{
			File file = new File( fileName );
			AudioInputStream ais = AudioSystem.getAudioInputStream( file );
			AudioInputStream aisRaw = null;
			AudioFormat fmt = ais.getFormat();
			AudioFormat fmtRaw = new AudioFormat(	AudioFormat.Encoding.PCM_SIGNED,
													fmt.getSampleRate(),
													16,
													fmt.getChannels(),
													fmt.getChannels() * 2,
													fmt.getSampleRate(),
													false );
			aisRaw = AudioSystem.getAudioInputStream( fmtRaw, ais );
			playRawWavData( fmtRaw, aisRaw );
			ais.close();
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}

	private void playRawWavData( AudioFormat fmt, AudioInputStream ais ) throws LineUnavailableException, IOException
	{
		byte[] data = new byte[ 4096 ];
		SourceDataLine line = getSrcLine( fmt );		// オーディオバッファ
		if( line != null ){
			// 再生開始
			line.start();
			int readBytes = 0;
			int writeBytes = 0;
			// ストリーミング再生を行う。
			while( readBytes != -1 && m_StatusFlags.get( StatusFlag.PLAY.ordinal() ) ){
				readBytes = ais.read( data, 0, data.length );	// ファイルからオーディオデータを読み込む
				if( readBytes != -1 ){
					writeBytes = line.write( data, 0, readBytes );	// 再生
				}

			}
			// 再生停止
			if( m_StatusFlags.get( StatusFlag.PLAY.ordinal() ) ){
				line.drain();		// オーディオバッファ上のデータ全て再生されるまで待つ
			}
			line.stop();
			line.close();
			ais.close();
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
			while( !m_StatusFlags.get( StatusFlag.TERM.ordinal() ) ){
				if( m_StatusFlags.get( StatusFlag.PLAY.ordinal() ) ){
					playMP3( m_FileName );
					m_StatusFlags.clear( StatusFlag.PLAY.ordinal() );
				}
				Thread.sleep( 1 );
			}
		}
		catch( InterruptedException e ){
			e.printStackTrace();
		}
	}

	public void play( String fileName )
	{
		m_StatusFlags.set( StatusFlag.PLAY.ordinal() );
		m_FileName = fileName;
	}

	public void stop()
	{
		m_StatusFlags.clear( StatusFlag.PLAY.ordinal() );
		m_FileName = "";
	}

}
