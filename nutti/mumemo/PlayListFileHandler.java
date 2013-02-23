package nutti.mumemo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import nutti.lib.LibException;
import nutti.lib.Util;

public class PlayListFileHandler
{

	public class MusicInfo
	{
		public String		m_FilePath;			// ファイルパス
		public String		m_MusicTitle;		// 音楽のタイトル
		public int			m_MusicLen;			// 音楽の長さ
		public long			m_FileSize;			// ファイルサイズ
		public String		m_Author;			// 作者
		public int			m_BitRate;			// ビットレート
	}

	private String							m_FileName;				// ファイル名
	private Map < String, MusicInfo >		m_MusicList;			// 音楽リスト（キー:ファイルパス、値:音楽情報）
	private ArrayList < String >			m_FilePathListArray;	// ファイルパスリスト


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

			while( true ){
				MusicInfo info = new MusicInfo();
				int len = 0;



				len = Util.loadInt( in );
				info.m_FilePath = Util.loadStringUTF8( in, len );
				len = Util.loadInt( in );
				info.m_MusicTitle = Util.loadStringUTF8( in, len );
				info.m_MusicLen = Util.loadInt( in );
				info.m_FileSize = Util.loadLong( in );
				len = Util.loadInt( in );
				info.m_Author = Util.loadStringUTF8( in, len );
				info.m_BitRate = Util.loadInt( in );

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

	void addItem( String filePath, String musicTitle, int musicLen, long fileSize, String author, int bitrate )
	{
		if( fileClosed() ){
			return;
		}

		try{
			// 既に同名のエントリが存在する場合は、無視
			if( m_MusicList.get( filePath ) != null ){
				return;
			}

			// エントリをファイルに追記する
			FileOutputStream out = new FileOutputStream( m_FileName, true );

			Util.saveInt( out, Util.getStringUTF8Byte( filePath ) );
			Util.saveStringUTF8( out, filePath );
			Util.saveInt( out, Util.getStringUTF8Byte( musicTitle ) );
			Util.saveStringUTF8( out, musicTitle );
			Util.saveInt( out, musicLen );
			Util.saveLong( out, fileSize );
			Util.saveInt( out, Util.getStringUTF8Byte( author ) );
			Util.saveStringUTF8( out, author );
			Util.saveInt( out, bitrate );

			MusicInfo info = new MusicInfo();
			info.m_FilePath = filePath;
			info.m_MusicTitle = musicTitle;
			info.m_MusicLen = musicLen;
			info.m_FileSize = fileSize;
			info.m_Author = author;
			info.m_BitRate = bitrate;

			m_MusicList.put( filePath, info );
			m_FilePathListArray.add( filePath );

			if( m_FilePathListArray.size() != m_MusicList.size() ){
				System.out.println( "Some Bugs." );
			}

		}
		catch( FileNotFoundException e ){
			e.printStackTrace();
		}
		catch( UnsupportedEncodingException e ){
			e.printStackTrace();
		}
		catch( IOException e ){
			e.printStackTrace();
		}
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
}
