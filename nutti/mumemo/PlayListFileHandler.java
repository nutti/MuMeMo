package nutti.mumemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import nutti.lib.LibException;
import nutti.lib.Util;

public class PlayListFileHandler
{

	public class MusicInfo
	{
		String			m_FilePath;		// ファイルパス
		String			m_FileName;		// ファイル名
		long			m_FileSize;		// ファイルサイズ
		long			m_Length;		// 音楽の長さ
		long			m_Freq;			// 周波数
		long			m_Bits;			// ビット数
		long			m_BitRate;		// ビットレート
		String			m_Format;		// ファイルフォーマット
		long			m_Channel;		// チャンネル数
		String			m_Composer;		// 作曲家
		String			m_Title;		// タイトル
		long			m_IsCBR;		// CBR形式ならtrue
		long			m_PlayCount;	// 再生回数
	}

	private String							m_FileName;				// ファイル名
	private Map < String, MusicInfo >		m_MusicList;			// 音楽リスト（キー:ファイルパス、値:音楽情報）
	private ArrayList < String >			m_FilePathListArray;	// ファイルパスリスト

	private Map						m_AudioInfo;			// 曲情報
	private BasicPlayerListener		m_BasicListener = new BasicPlayerListener()
	{
		public void stateUpdated( BasicPlayerEvent event )
		{
		}
		public void opened( Object stream, Map properties )
		{
			m_AudioInfo = properties;
		}
		public void progress( int bytesread, long microseconds, byte[] pcmdata, Map properties )
		{
		}
		public void setController( BasicController controller )
		{
		}
	};


	public PlayListFileHandler()
	{
		cleanup();
	}

	void load( String fileName )
	{
		closeFile();
		m_FileName = fileName;

		// 全エントリの読み込み
		try {
			FileInputStream in = new FileInputStream( m_FileName );

			// バージョンチェック
			long version = Util.loadLong( in );
			if( version != Constant.MUMEMO_VERSION ){
				return;
			}

			while( true ){
				MusicInfo info = new MusicInfo();
				int len = 0;

				len = Util.loadInt( in );
				info.m_FilePath = Util.loadStringUTF8( in, len );
				len = Util.loadInt( in );
				info.m_FileName = Util.loadStringUTF8( in, len );
				info.m_FileSize = Util.loadLong( in );
				info.m_Length = Util.loadLong( in );
				info.m_Freq = Util.loadLong( in );
				info.m_Bits = Util.loadLong( in );
				info.m_BitRate = Util.loadLong( in );
				len = Util.loadInt( in );
				info.m_Format = Util.loadStringUTF8( in, len );
				info.m_Channel = Util.loadLong( in );
				len = Util.loadInt( in );
				info.m_Composer = Util.loadStringUTF8( in, len );
				len = Util.loadInt( in );
				info.m_Title = Util.loadStringUTF8( in, len );
				info.m_IsCBR = Util.loadLong( in );
				info.m_PlayCount = Util.loadLong( in );

				m_MusicList.put( info.m_FilePath, info );
				m_FilePathListArray.add( info.m_FilePath );
			}
		}
		catch( FileNotFoundException e ){
			e.printStackTrace();
		}
		catch( IOException e ){
			e.printStackTrace();
		}
		catch( LibException e ){
			e.printStackTrace();
		}
	}

	public MusicInfo addItem( String filePath )
	{
		// ファイルが閉じられていた場合は終了
		if( fileClosed() ){
			return null;
		}

		// 既に同名のエントリが存在する場合は、無視
		if( m_MusicList.get( filePath ) != null ){
			return null;
		}

		// ファイル情報を取得
		MusicInfo info = loadMusicInfo( filePath );

		try{
			// 初回時のみバージョン情報保存
			if( !Util.fileExist( m_FileName ) ){
				FileOutputStream out = new FileOutputStream( m_FileName, true );
				Util.saveLong( out, Constant.MUMEMO_VERSION );
				out.close();
			}

			// エントリをファイルに追記する
			FileOutputStream out = new FileOutputStream( m_FileName, true );


			Util.saveInt( out, Util.getStringUTF8Byte( info.m_FilePath ) );
			Util.saveStringUTF8( out, info.m_FilePath );
			Util.saveInt(out, Util.getStringUTF8Byte( info.m_FileName ) );
			Util.saveStringUTF8( out, info.m_FileName );
			Util.saveLong( out, info.m_FileSize );
			Util.saveLong( out, info.m_Length );
			Util.saveLong( out, info.m_Freq );
			Util.saveLong( out, info.m_Bits );
			Util.saveLong( out, info.m_BitRate );
			Util.saveInt( out, Util.getStringUTF8Byte( info.m_Format ) );
			Util.saveStringUTF8( out, info.m_Format );
			Util.saveLong( out, info.m_Channel );
			Util.saveInt( out, Util.getStringUTF8Byte( info.m_Composer ) );
			Util.saveStringUTF8( out, info.m_Composer );
			Util.saveInt( out, Util.getStringUTF8Byte( info.m_Title ) );
			Util.saveStringUTF8( out,info.m_Title );
			Util.saveLong( out, info.m_IsCBR );
			Util.saveLong( out, info.m_PlayCount );

			m_MusicList.put( filePath, info );
			m_FilePathListArray.add( filePath );

			if( m_FilePathListArray.size() != m_MusicList.size() ){
				System.out.println( "Some Bugs." );
			}

		}
		catch( FileNotFoundException e ){
			e.printStackTrace();
			return null;
		}
		catch( UnsupportedEncodingException e ){
			e.printStackTrace();
			return null;
		}
		catch( IOException e ){
			e.printStackTrace();
			return null;
		}

		return info;
	}

	public int getEntryTotal()
	{
		if( fileClosed() ){
			return -1;
		}
		return m_MusicList.size();
	}

	public PlayListFileHandler.MusicInfo getMusicInfo( int idx )
	{
		if( fileClosed() ){
			return null;
		}

		return m_MusicList.get( m_FilePathListArray.get( idx ) );
	}

	public boolean isExist( String filePath )
	{
		if( fileClosed() ){
			return false;
		}

		return m_MusicList.get( filePath ) != null;
	}

	public void closeFile()
	{
		cleanup();
	}

	private void cleanup()
	{
		m_MusicList = new HashMap < String, MusicInfo > ();
		m_FilePathListArray = new ArrayList < String > ();
		m_FileName = "";
	}

	private boolean fileClosed()
	{
		return m_FileName.equals( "" );
	}

	private MusicInfo loadMusicInfo( String filePath )
	{
		MusicInfo info = new MusicInfo();
		Object obj;

		BasicPlayer player = new BasicPlayer();
		File file = new File( filePath );
		player.addBasicPlayerListener( m_BasicListener );
		try{
			player.open( file );
			// ファイルパス
			info.m_FilePath = filePath;
			// ファイル名
			info.m_FileName = file.getName();
			// ファイルサイズの取得
			info.m_FileSize = file.length();
			// 現在の音楽再生位置を取得（秒単位）
			obj = m_AudioInfo.get( "audio.length.bytes" );
			info.m_Length = obj != null ? Long.parseLong( obj.toString() ) : 0;
			// 曲名の取得
			obj = m_AudioInfo.get( "title" );
			info.m_Title = obj != null ? obj.toString() : "";
			// 作曲者の取得
			obj = m_AudioInfo.get( "author" );
			info.m_Composer = obj != null ? obj.toString() : "";
			// ファイルタイプを取得
			obj = m_AudioInfo.get( "audio.type" );
			String type = obj != null ? obj.toString() : "Unknown Type";
			// .mp3の場合
			if( type.equals( "MP3" ) ){
				// ビットレートの取得
				obj = m_AudioInfo.get( "mp3.bitrate.nominal.bps" );
				info.m_BitRate = obj != null ? Long.parseLong( obj.toString() ) : 0;
				// チャンネル数の取得
				obj = m_AudioInfo.get( "mp3.channels" );
				info.m_Channel = obj != null ? Long.parseLong( obj.toString() ) : 0;
				// CBR or VBR
				obj = m_AudioInfo.get( "mp3.vbr" );
				if( obj != null ){
					if( Boolean.parseBoolean( obj.toString() ) == false ){
						info.m_IsCBR = 1;
					}
					else{
						info.m_IsCBR = 0;
					}
				}
				else{
					info.m_IsCBR = -1;
				}
				// サンプルレートの取得
				obj = m_AudioInfo.get( "mp3.frequency.hz" );
				info.m_Freq = obj != null ? ( long ) ( Double.parseDouble( obj.toString() ) ) : 0;
				// ファイルフォーマットの取得
				obj = m_AudioInfo.get( "mp3.version.encoding" );
				info.m_Format = obj != null ? obj.toString() : "";
			}



		}
		catch( BasicPlayerException e ){
			e.printStackTrace();
		}

		return info;
	}
}
