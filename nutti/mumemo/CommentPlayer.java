package nutti.mumemo;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import nutti.lib.Util;
import nutti.mumemo.Constant.ComponentID;

public class CommentPlayer extends IComponent
{
	private JPanel					m_CommPlayer;					// コメント表示
	private JComboBox				m_TagSelectList;				// 選択可能なタグリスト
	private JList					m_CommList;						// コメント一覧
	private DefaultListModel		m_DefListModel;					// JList項目追加用

	private CommentFileHandler		m_CommFileHandler;				// コメントファイルハンドラ



	public CommentPlayer( JFrame mainWnd, IMessageMediator mediator, CommentFileHandler comm )
	{
		super( mediator, ComponentID.COM_ID_COMMENT_PLAYER );

		m_CommFileHandler = comm;

		// コメントライター領域
		m_CommPlayer = new JPanel();
		m_CommPlayer.setBounds( 10, 160, 250, 200 );
		m_CommPlayer.setBackground( Color.CYAN );
		m_CommPlayer.setLayout( null );

		// 選択可能なタグリスト
		m_TagSelectList = new JComboBox();
		m_TagSelectList.setBounds( 10, 10, 120, 20 );
		m_TagSelectList.addItem( "Select Tags" );
		m_CommPlayer.add( m_TagSelectList );

		// コメント一覧
		m_DefListModel = new DefaultListModel();
		m_CommList = new JList( m_DefListModel );
		m_CommList.setBounds( 10, 50, 230, 100 );
		m_CommList.setBackground( Color.WHITE );
		m_CommPlayer.add( m_CommList );


		mainWnd.add( m_CommPlayer );
	}


	public void procMsg( ComponentID from, String msg )
	{
	}

	public void procMsg( ComponentID from, String msg, String[] options )
	{
		switch( from ){
			case COM_ID_PLAY_CONTROLLER:
				if( msg.equals( "Update Time" ) ){
					int curSecond = Integer.parseInt( options[ 0 ] );
					// 古いコメントの削除
					Pattern pattern = Pattern.compile( "\\[(.+)\\]" );
					for( int i = 0; i < m_DefListModel.getSize(); ++i ){
						Matcher matcher = pattern.matcher( (String) m_DefListModel.getElementAt( i ) );
						if( matcher.find() ){
							String[] elms = matcher.group( 1 ).split( ":" );
							int pos = Integer.parseInt( elms[ 0 ] ) * 60 + Integer.parseInt( elms[ 1 ] );
							if( curSecond - pos > 2 ){
								m_DefListModel.remove( i );
								m_CommList.ensureIndexIsVisible( m_DefListModel.getSize() - 1 );
							}
						}
					}
					// 新しいコメントの追加
					String comment;
					for( int i = 0; i < m_CommFileHandler.getCommentEntriesTotal( curSecond ); ++i ){
						if( m_CommFileHandler.commentReleatedTag( curSecond, i, m_TagSelectList.getSelectedIndex() - 1 ) ){
							comment = "[" + Integer.toString( (int)( curSecond / 60 ) ) + ":" + Integer.toString( curSecond % 60 ) + "] ";
							SimpleDateFormat fmt = new SimpleDateFormat( "yyyy.MM.dd HH:mm:ss" );
							String str = fmt.format( new Date( m_CommFileHandler.getCommentedDate( curSecond, i ) ) );
							comment = comment + str + " ";
							comment = comment + "(" + m_CommFileHandler.getCommentAuthor( curSecond, i ) + ")\n";
							comment = comment + m_CommFileHandler.getComment( curSecond, i );
							m_DefListModel.addElement( comment );
							m_CommList.ensureIndexIsVisible( m_DefListModel.getSize() - 1 );
						}
					}
				}

				break;
			case COM_ID_COMMENT_WRITER:
				if( msg.equals( "Create Tag" ) ){
					m_TagSelectList.addItem( options[ 0 ] );
				}
				break;
			default:
				break;
		}
	}

	public void procMsg( ComponentID from, int msg, String[] options )
	{
		switch( from ){
			case COM_ID_PLAY_LIST:
			case COM_ID_PLAY_CONTROLLER:
				if( msg == Constant.MsgID.MSG_ID_PLAY.ordinal() ){
					// タグ情報の読み込み
					for( int i = 0; i < m_CommFileHandler.getTagEntriesTotal(); ++i ){
						m_TagSelectList.addItem( m_CommFileHandler.getTagName( i ) );
					}
				}
				else if( msg == Constant.MsgID.MSG_ID_STOP.ordinal() ){
					cleanupTags();
				}
				break;
			default:
				break;
		}
	}

	private void cleanupTags()
	{
		m_TagSelectList.removeAllItems();
		m_TagSelectList.addItem( "Select Tags" );
	}

	// コメント全消去
	private void cleanupComments()
	{
		m_DefListModel.clear();
		m_CommList.ensureIndexIsVisible( m_DefListModel.getSize() - 1 );
	}
}
