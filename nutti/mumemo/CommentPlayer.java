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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import nutti.lib.Util;
import nutti.mumemo.Constant.ComponentID;

public class CommentPlayer extends IComponent
{
	private JPanel					m_CommPlayer;					// コメント表示
	private JComboBox				m_TagSelectList;				// 選択可能なタグリスト
	private JTable					m_CommList;						// コメント一覧
	private DefaultTableModel		m_DefTblModel;					// JList項目追加用
	private JScrollPane				m_CommListScrollBar;			// スクロールバー

	private CommentFileHandler		m_CommFileHandler;				// コメントファイルハンドラ



	public CommentPlayer( JFrame mainWnd, IMessageMediator mediator, CommentFileHandler comm )
	{
		super( mediator, ComponentID.COM_ID_COMMENT_PLAYER );

		m_CommFileHandler = comm;

		// コメントライター領域
		m_CommPlayer = new JPanel();
		m_CommPlayer.setBounds( 10, 300, 580, 150 );
		m_CommPlayer.setBackground( Color.BLACK );
		m_CommPlayer.setLayout( null );

		// 選択可能なタグリスト
		m_TagSelectList = new JComboBox();
		m_TagSelectList.setBounds( 10, 10, 120, 20 );
		m_TagSelectList.addItem( "Select Tags" );
		m_CommPlayer.add( m_TagSelectList );

		// コメント一覧
		String[] colTitles = { "Time", "Date", "Author", "Comment" };
		m_DefTblModel = new DefaultTableModel( colTitles, 0 );
		m_CommList = new JTable( m_DefTblModel );
		m_CommList.setBounds( 10, 40, 560, 100 );
		m_CommList.setForeground( Color.WHITE );
		m_CommList.setBackground( Color.BLACK );
		m_CommList.setDefaultEditor( Object.class, null );
		m_CommListScrollBar = new JScrollPane( m_CommList );
		m_CommListScrollBar.setBounds( 10, 40, 560, 100 );
		m_CommListScrollBar.getViewport().setBackground( Color.BLACK );
		m_CommListScrollBar.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		m_CommPlayer.add( m_CommListScrollBar );


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
					for( int i = 0; i < m_DefTblModel.getRowCount(); ++i ){
						String time = m_DefTblModel.getValueAt( i, 0 ).toString();
						String[] elms = time.split( ":" );
						int pos = Integer.parseInt( elms[ 0 ] ) * 60 + Integer.parseInt( elms[ 1 ] );
						if( curSecond - pos > 5 ){
							m_DefTblModel.removeRow( i );
						}
					}
					// 新しいコメントの追加
					for( int i = 0; i < m_CommFileHandler.getCommentEntriesTotal( curSecond ); ++i ){
						if( m_CommFileHandler.commentReleatedTag( curSecond, i, m_TagSelectList.getSelectedIndex() - 1 ) ){
							SimpleDateFormat fmt = new SimpleDateFormat( "yyyy.MM.dd HH:mm:ss" );
							String[] elms = {	Integer.toString( (int)( curSecond / 60 ) ) + ":" + Integer.toString( curSecond % 60 ),
												fmt.format( new Date( m_CommFileHandler.getCommentedDate( curSecond, i ) ) ),
												m_CommFileHandler.getCommentAuthor( curSecond, i ),
												m_CommFileHandler.getComment( curSecond, i ) };
							m_DefTblModel.addRow( elms );
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
		for( int i = 0; i < m_DefTblModel.getRowCount(); ++i ){
			m_DefTblModel.removeRow( i );
		}
	}
}
