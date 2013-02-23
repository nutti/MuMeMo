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

public class CommentFileHandler
{
	private class Header
	{
		int			m_MusicNameBytes;	// 曲名のバイト数
		String		m_MusicName;		// 曲名
		int			m_MusicLen;			// 曲の長さ
	}

	private class Tag
	{
		int			m_TagNameBytes;		// タグ名バイト数
		String		m_TagName;			// タグ名
	}

	private class Entry
	{
		long						m_Date;				// エントリ作成日
		int						m_AuthorBytes;		// エントリ作成者名のバイト数
		String					m_Author;			// エントリ作成者名
		int						m_CommentBytes;		// コメントバイト数
		String					m_Comment;			// コメント
		ArrayList < Integer >	m_TagList;			// 関連タグリスト（インデックスで指定）
	}

	private class FileFormat
	{
		Header									m_Header;
		ArrayList < Tag >						m_Tags;			// タグ一覧
		Map < Integer, ArrayList < Entry > >	m_Entries;		// エントリー一覧（キー:音楽データにおける場所、値:エントリリスト）
	}

	FileFormat				m_Format;
	String					m_FileName;
	boolean					m_NeedUpdate;				// ファイルの更新が必要か？

	public CommentFileHandler()
	{
		cleanup();
	}

	public void createFile( String fileName )
	{
		cleanup();
		m_FileName = fileName;
		saveFile();
	}

	public void saveFile()
	{
		try {
			if( fileClosed() || !m_NeedUpdate ){
				return;
			}

			FileOutputStream out = new FileOutputStream( m_FileName );

			// ヘッダの保存
			Util.saveInt( out, m_Format.m_Header.m_MusicNameBytes );
			Util.saveStringUTF8( out, m_Format.m_Header.m_MusicName );
			Util.saveInt( out, m_Format.m_Header.m_MusicLen );
			Util.saveInt( out, m_Format.m_Tags.size() );
			Util.saveInt( out, m_Format.m_Entries.size() );

			// タグの保存
			for( Tag tag : m_Format.m_Tags ){
				Util.saveInt( out, tag.m_TagNameBytes );
				Util.saveStringUTF8( out, tag.m_TagName );
			}

			// エントリの保存
			for( Map.Entry < Integer, ArrayList < Entry > >  entries :  m_Format.m_Entries.entrySet() ){
				// エントリ作成時間を保存
				Util.saveInt( out, entries.getKey() );
				Util.saveInt( out,  entries.getValue().size() );
				for( Entry entry : entries.getValue() ){
					Util.saveLong( out, entry.m_Date );
					Util.saveInt( out, entry.m_AuthorBytes );
					Util.saveStringUTF8( out, entry.m_Author );
					Util.saveInt( out, entry.m_CommentBytes );
					Util.saveStringUTF8( out, entry.m_Comment );
					Util.saveInt( out, entry.m_TagList.size() );
					for( Integer tagIdx : entry.m_TagList ){
						Util.saveInt( out, tagIdx );
					}
				}
			}

			out.close();

			m_NeedUpdate = false;

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

	public void loadFile( String fileName )
	{
		cleanup();
		m_FileName = fileName;

		FileInputStream in;
		try {
			in = new FileInputStream( m_FileName );
			// ヘッダの保存
			m_Format.m_Header.m_MusicNameBytes = Util.loadInt( in );
			m_Format.m_Header.m_MusicName = Util.loadStringUTF8( in, m_Format.m_Header.m_MusicNameBytes );
			m_Format.m_Header.m_MusicLen = Util.loadInt( in );
			int tagTotal = Util.loadInt( in );
			int entriesTotal = Util.loadInt( in );

			// タグの保存
			for( int i = 0; i < tagTotal; ++i ){
				Tag tag = new Tag();
				tag.m_TagNameBytes = Util.loadInt( in );
				tag.m_TagName = Util.loadStringUTF8( in, tag.m_TagNameBytes );
				m_Format.m_Tags.add( tag );
			}

			// エントリの保存
			for( int i = 0; i < entriesTotal; ++i ){
				// エントリ作成時間を保存
				ArrayList < Entry > entryList = new ArrayList < Entry > ();
				int entryPos = Util.loadInt( in );
				int entryTotal = Util.loadInt( in );
				for( int j = 0; j < entryTotal; ++j ){
					Entry entry = new Entry();
					entry.m_Date = Util.loadLong( in );
					entry.m_AuthorBytes = Util.loadInt( in );
					entry.m_Author = Util.loadStringUTF8( in, entry.m_AuthorBytes );
					entry.m_CommentBytes = Util.loadInt( in );
					entry.m_Comment = Util.loadStringUTF8( in, entry.m_CommentBytes );
					entry.m_TagList = new ArrayList < Integer > ();
					int tagIdxTotal = Util.loadInt( in );
					for( int k = 0; k < tagIdxTotal; ++k ){
						entry.m_TagList.add( Util.loadInt( in ) );
						//entry.m_TagList.add( Util.loadInt( in ) );
					}
					entryList.add( entry );
				}
				m_Format.m_Entries.put( entryPos, entryList );
			}

			in.close();
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

	public void addTag( String tagName )
	{
		if( fileClosed() ){
			return;
		}

		try{
			Tag tag = new Tag();
			tag.m_TagName = tagName;
			tag.m_TagNameBytes = Util.getStringUTF8Byte( tagName );
			m_Format.m_Tags.add( tag );

			m_NeedUpdate = true;
		}
		catch( UnsupportedEncodingException e ){
			e.printStackTrace();
		}
	}

	public void addComment( int musicPos, long date, String author, String comment, ArrayList < Integer > tagList )
	{
		if( fileClosed() ){
			return;
		}

		try{
			Entry entry = new Entry();
			entry.m_Date = date;
			entry.m_AuthorBytes = Util.getStringUTF8Byte( author );
			entry.m_Author = author;
			entry.m_CommentBytes = Util.getStringUTF8Byte( comment );
			entry.m_Comment = comment;
			entry.m_TagList = tagList;
			// 指定した音楽再生位置に、エントリが存在しない場合
			if( m_Format.m_Entries.get( musicPos ) == null ){
				ArrayList < Entry > entries = new ArrayList < Entry > ();
				entries.add( entry );
				m_Format.m_Entries.put( musicPos, entries );
			}
			// 指定した音楽再生位置に、エントリが存在する場合
			else{
				m_Format.m_Entries.get( musicPos ).add( entry );
			}

			m_NeedUpdate = true;
		}
		catch( UnsupportedEncodingException e ){
			e.printStackTrace();
		}
	}

	public void buildHeader( String musicName, int musicLen )
	{
		if( fileClosed() ){
			return;
		}

		try{
			m_Format.m_Header.m_MusicNameBytes = Util.getStringUTF8Byte( musicName );
			m_Format.m_Header.m_MusicName = musicName;
			m_Format.m_Header.m_MusicLen = musicLen;

			m_NeedUpdate = true;
		}
		catch( UnsupportedEncodingException e ){
			e.printStackTrace();
		}
	}

	public int getTagEntriesTotal()
	{
		return m_Format.m_Tags.size();
	}

	public String getTagName( int idx )
	{
		return m_Format.m_Tags.get( idx ).m_TagName;
	}

	public int getCommentEntriesTotal( int second )
	{
		if( m_Format.m_Entries.get( second ) != null ){
			return m_Format.m_Entries.get( second ).size();
		}
		return 0;
	}

	public String getComment( int second, int idx )
	{
		return m_Format.m_Entries.get( second ).get( idx ).m_Comment;
	}

	public String getCommentAuthor( int second, int idx )
	{
		return m_Format.m_Entries.get( second ).get( idx ).m_Author;
	}

	public ArrayList < Integer > getCommentRelatedTagList( int second, int idx )
	{
		return m_Format.m_Entries.get( second ).get( idx ).m_TagList;
	}

	public long getCommentedDate( int second, int idx )
	{
		return m_Format.m_Entries.get( second ).get( idx ).m_Date;
	}

	public boolean commentReleatedTag( int second, int idx, int tag )
	{
		for( Integer i : m_Format.m_Entries.get( second ).get( idx ).m_TagList ){
			if( i == tag ){
				return true;
			}
		}

		return false;
	}

	public boolean tagExist( String tagName )
	{
		for( Tag tag : m_Format.m_Tags ){
			if( tag.m_TagName.equals( tagName ) ){
				return true;
			}
		}

		return false;
	}

	public void closeFile()
	{
		saveFile();
		cleanup();
	}

	private void cleanup()
	{
		m_Format = new FileFormat();
		m_Format.m_Header = new Header();
		m_Format.m_Tags = new ArrayList < Tag > ();
		m_Format.m_Entries = new HashMap < Integer, ArrayList < Entry > > ();
		m_FileName = "";
		m_NeedUpdate = false;
	}

	private boolean fileClosed()
	{
		return m_FileName.equals( "" );
	}
}
