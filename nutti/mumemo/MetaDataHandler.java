package nutti.mumemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Savepoint;
import java.util.HashMap;
import java.util.Map;

import javazoom.jlgui.basicplayer.BasicPlayerException;

import nutti.mumemo.Constant.ComponentID;

public class MetaDataHandler
{

	Map < String, String >		m_CommentFilePath = new HashMap < String, String > ();	// コメントファイルリスト
	String						m_FileName;

	public MetaDataHandler()
	{
	}

	// メタデータのロード
	public void loadMetaDataFile( String fileName )
	{
		m_FileName = fileName;

		try{
			// 全エントリの読み込み
			FileInputStream in = new FileInputStream( m_FileName );

			while( true ){
				int len;
				byte[] buf;
				String musicName;			// 音楽名
				String commFilePath;		// コメントファイルパス

				if( ( len = in.read() ) == -1 ){
					break;
				}
				buf = new byte [ len ];
				if( in.read( buf, 0, len ) == -1 ){
					break;
				}
				musicName = new String( buf, "UTF-8" );

				if( ( len = in.read() ) == -1 ){
					break;
				}
				buf = new byte [ len ];
				if( in.read( buf, 0, len ) == -1 ){
					break;
				}
				commFilePath = new String( buf, "UTF-8" );
				m_CommentFilePath.put( musicName, commFilePath );
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

	// メタデータの追加
	public void addMetaData( String musicName, String commFilePath )
	{
		try{
			// 既に同名のエントリが存在する場合は、無視
			if( m_CommentFilePath.get( musicName ) != null ){
				return;
			}

			// エントリをファイルに追記する
			FileOutputStream out = new FileOutputStream( m_FileName, true );
			int musicNameLen = musicName.getBytes( "UTF-8" ).length;
			int commFilePathLen = commFilePath.getBytes( "UTF-8" ).length;

			out.write( musicNameLen );
			out.write( musicName.getBytes( "UTF-8" ), 0, musicNameLen );
			out.write( commFilePathLen );
			out.write( commFilePath.getBytes( "UTF-8" ), 0, commFilePathLen );
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

	public String getCommentFilePath( String musicName )
	{
		return m_CommentFilePath.get( musicName );
	}

}
