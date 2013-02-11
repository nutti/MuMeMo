package nutti.mumemo;

import java.util.ArrayList;
import java.util.Map;

public class CommentFileHandler extends IComponent
{
	private class Header
	{
		int			m_MusicNameBytes;	// 曲名のバイト数
		String		m_MusicName;		// 曲名
		int			m_MusicLen;			// 曲の長さ
		int			m_TagTotal;			// タグ数
	}

	private class Tag
	{
		int			m_TagNameBytes;		// タグ名バイト数
		String		m_TagName;			// タグ名
	}

	private class Entry
	{
		int						m_Date;				// エントリ作成日
		//int						m_MusicPos;			// エントリの音楽データにおける場所
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

	public CommentFileHandler( MessageMediator mediator )
	{
		super( mediator, "CommentFileHandler" );
	}



	public void procMsg( String msg )
	{
	}

	public void procMsg( String msg, String[] options )
	{
	}
}
