package nutti.mumemo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import nutti.lib.LibException;
import nutti.lib.Util;

public class MetaDataHandler
{

	Map < String, String >		m_CommentFilePath = new HashMap < String, String > ();	// コメントファイルリスト
	String						m_FileName;

	public MetaDataHandler()
	{
		cleanup();
	}

	// メタデータのロード
	public void loadMetaDataFile( String fileName )
	{
		closeFile();
		m_FileName = fileName;

		try{
			// 全エントリの読み込み
			FileInputStream in = new FileInputStream( m_FileName );

			// バージョンチェック
			long version = Util.loadLong( in );
			VersionChecker checker = new VersionChecker();
			checker.checkVersion( version );
			if( !checker.majorVerMatched() || !checker.minorVerUMatched() ){
				return;
			}

			while( true ){
				int len;
				long musicLen;				// 音楽の長さ
				String musicName;			// 音楽名
				String commFilePath;		// コメントファイルパス

				if( ( len = Util.loadInt( in ) ) == -1 ){
					break;
				}

				if( ( musicName = Util.loadStringUTF8( in, len ) ) == null ){
					break;
				}

				if( ( musicLen = Util.loadLong( in ) ) == -1 ){
					break;
				}

				if( ( len = Util.loadInt( in ) ) == -1 ){
					break;
				}
				if( ( commFilePath = Util.loadStringUTF8( in, len ) ) == null ){
					break;
				}
				m_CommentFilePath.put( "[" + Long.toString( musicLen ) + "]" + musicName, commFilePath );
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
		catch( LibException e ){
			e.printStackTrace();
		}
	}

	// メタデータの追加
	public void addMetaData( String musicName, long musicLen, String commFilePath )
	{
		if( fileClosed() ){
			return;
		}

		try{
			String name = "[" + Long.toString( musicLen ) + "]" + musicName;
			// 既に同名のエントリが存在する場合は、無視
			if( m_CommentFilePath.get( name ) != null ){
				return;
			}

			// 初回時のみバージョン情報保存
			if( !Util.fileExist( m_FileName ) ){
				FileOutputStream out = new FileOutputStream( m_FileName, true );
				Util.saveLong( out, Constant.MUMEMO_VERSION );
				out.close();
			}

			// エントリをファイルに追記する
			FileOutputStream out = new FileOutputStream( m_FileName, true );
			int musicNameLen = Util.getStringUTF8Byte( musicName );
			int commFilePathLen = Util.getStringUTF8Byte( commFilePath );

			Util.saveInt( out, musicNameLen );
			Util.saveStringUTF8( out, musicName );
			Util.saveLong( out, musicLen );
			Util.saveInt( out, commFilePathLen );
			Util.saveStringUTF8( out, commFilePath );

			m_CommentFilePath.put( name, commFilePath );

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

	public String getCommentFilePath( String musicName, long musicLen )
	{
		if( fileClosed() ){
			return null;
		}

		String name = "[" + Long.toString( musicLen ) + "]" + musicName;

		return m_CommentFilePath.get( name );
	}

	public ArrayList < String > getMusicNameList()
	{
		if( fileClosed() ){
			return null;
		}

		ArrayList < String > list = new ArrayList < String > ();

		Iterator < String > it = m_CommentFilePath.keySet().iterator();
		while( it.hasNext() ){
			String s = it.next();
			list.add( s );
		}

		return list;
	}

	public void closeFile()
	{
		cleanup();
	}

	private void cleanup()
	{
		m_CommentFilePath = new HashMap < String, String > ();
		m_FileName = "";
	}

	private boolean fileClosed()
	{
		return m_FileName.equals( "" );
	}

}
