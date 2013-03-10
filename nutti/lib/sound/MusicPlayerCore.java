package nutti.lib.sound;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.tritonus.share.sampled.TAudioFormat;
import org.tritonus.share.sampled.file.TAudioFileFormat;


public class MusicPlayerCore implements Runnable
{
	private enum StatusFlag
	{
		PLAY,
		PAUSE,
		SEEK,
		TERM,
		STATUS_FLAG_TOTAL
	}

	private BitSet			m_StatusFlags;
	private String			m_FileName;
	private Map				m_AudioInfo;

	public MusicPlayerCore()
	{
		m_StatusFlags = new BitSet( StatusFlag.STATUS_FLAG_TOTAL.ordinal() );

		Thread thread = new Thread( this );
		thread.start();
	}

	protected Map deepCopy(Map src)
    {
        HashMap map = new HashMap();
        if (src != null)
        {
            Iterator it = src.keySet().iterator();
            while (it.hasNext())
            {
                Object key = it.next();
                Object value = src.get(key);
                map.put(key, value);
            }
        }
        return map;
    }
	
	private void playMP3( String fileName )
	{
		try{
			File file = new File( fileName );
			AudioInputStream ais = AudioSystem.getAudioInputStream( file );
			AudioInputStream aisRaw = null;
			
			
			
			AudioFileFormat audioFmt = AudioSystem.getAudioFileFormat( file );
			AudioFormat fmt = ais.getFormat();
			m_AudioInfo = fmt.properties();
			
			
			
			if (audioFmt instanceof TAudioFileFormat)
            {
                // Tritonus SPI compliant audio file format.
                m_AudioInfo = ((TAudioFileFormat) audioFmt).properties();
                // Clone the Map because it is not mutable.
                m_AudioInfo = deepCopy( m_AudioInfo );
            }
            else m_AudioInfo = new HashMap();
            // Add JavaSound properties.
            if (audioFmt.getByteLength() > 0) m_AudioInfo.put("audio.length.bytes", new Integer(audioFmt.getByteLength()));
            if (audioFmt.getFrameLength() > 0) m_AudioInfo.put("audio.length.frames", new Integer(audioFmt.getFrameLength()));
            if (audioFmt.getType() != null) m_AudioInfo.put("audio.type", (audioFmt.getType().toString()));
            // Audio format.
            AudioFormat audioFormat = audioFmt.getFormat();
            if (audioFormat.getFrameRate() > 0) m_AudioInfo.put("audio.framerate.fps", new Float(audioFormat.getFrameRate()));
            if (audioFormat.getFrameSize() > 0) m_AudioInfo.put("audio.framesize.bytes", new Integer(audioFormat.getFrameSize()));
            if (audioFormat.getSampleRate() > 0) m_AudioInfo.put("audio.samplerate.hz", new Float(audioFormat.getSampleRate()));
            if (audioFormat.getSampleSizeInBits() > 0) m_AudioInfo.put("audio.samplesize.bits", new Integer(audioFormat.getSampleSizeInBits()));
            if (audioFormat.getChannels() > 0) m_AudioInfo.put("audio.channels", new Integer(audioFormat.getChannels()));
            if (audioFormat instanceof TAudioFormat)
            {
                // Tritonus SPI compliant audio format.
                Map addproperties = ((TAudioFormat) audioFormat).properties();
                m_AudioInfo.putAll(addproperties);
            }
			
			
            Object obj = m_AudioInfo.get( "mp3.channels" );
			
			
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
				// 再生中だけどポーズの場合
				while( m_StatusFlags.get( StatusFlag.PAUSE.ordinal() ) ){
					try{
						line.flush();
						Thread.sleep( 1 );
					}
					catch( InterruptedException e ){
						e.printStackTrace();
					}
				}
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

	public void play()
	{
		m_StatusFlags.set( StatusFlag.PLAY.ordinal() );
	}

	public void stop()
	{
		m_StatusFlags.clear( StatusFlag.PLAY.ordinal() );
		m_FileName = "";
	}

	public void open( String fileName )
	{
		m_FileName = fileName;
	}

	public void seek( long pos )
	{

	}

	public void pause()
	{
		m_StatusFlags.set( StatusFlag.PAUSE.ordinal() );
	}

	public void resume()
	{
		m_StatusFlags.clear( StatusFlag.PAUSE.ordinal() );
	}
}