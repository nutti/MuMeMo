package nutti.mumemo;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import nutti.lib.Util;
import nutti.mumemo.Constant.ComponentID;

public class CommentWriter extends IComponent implements ActionListener
{
	private static final String COMMENT_BUTTON_NAME = "Comment";
	private static final String CREATE_TAG_BUTTON_NAME = "Create Tag";

	private JPanel					m_CommWriter;					// コメントライター
	private JTextArea				m_CommInputArea;				// コメント入力エリア
	private boolean					m_ClickedCommInputAreaFirst;	// コメント入力エリアをクリックしたのが初めての場合
	private JButton					m_CommButton;					// コメントを書くボタン
	private JComboBox				m_TagSelectList;				// 選択可能なタグリスト
	private JTextField				m_NewTagNameInputArea;			// 新しいタグ名入力エリア
	private JButton					m_CreateTagBtn;					// 新しいタグ作成ボタン

	private MetaDataHandler			m_MetaDataHandler;				// メタデータハンドラ
	private CommentFileHandler		m_CommFileHandler;				// コメントファイルハンドラ

	private int						m_PlayTime;						// 再生時間

	// コメント入力エリアへのマウスイベント
	private MouseListener	m_CommInputAreaML = new MouseListener()
	{
		public void mouseClicked( MouseEvent event )
		{
			if( !m_ClickedCommInputAreaFirst ){
				m_CommInputArea.setText( "" );
				m_ClickedCommInputAreaFirst = true;
			}
		}

		public void mousePressed( MouseEvent event )
		{
		}

		public void mouseReleased( MouseEvent event )
		{
		}

		public void mouseEntered( MouseEvent event )
		{
		}

		public void mouseExited( MouseEvent event )
		{
		}
	};

	public CommentWriter( JFrame mainWnd, IMessageMediator mediator, MetaDataHandler meta, CommentFileHandler comm )
	{
		super( mediator, ComponentID.COM_ID_COMMENT_WRITER );

		m_MetaDataHandler = meta;
		m_CommFileHandler = comm;

		// コメントライター領域
		m_CommWriter = new JPanel();
		m_CommWriter.setBounds( 180, 160, 250, 200 );
		m_CommWriter.setBackground( Color.WHITE );
		m_CommWriter.setLayout( null );

		// コメント入力エリア
		m_CommInputArea = new JTextArea( "コメントを入力してください" );
		m_CommInputArea.setBounds( 10, 10, 180, 100 );
		m_CommInputArea.setBackground( Color.GRAY );
		m_CommInputArea.addMouseListener( m_CommInputAreaML );
		m_CommWriter.add( m_CommInputArea );
		m_ClickedCommInputAreaFirst = false;

		// コメント入力ボタン
		m_CommButton = new JButton( COMMENT_BUTTON_NAME );
		m_CommButton.setBounds( 10, 110, 60, 20 );
		m_CommButton.addActionListener( this );
		m_CommButton.setActionCommand( m_CommButton.getText() );
		m_CommWriter.add( m_CommButton );

		// 選択可能なタグリスト
		m_TagSelectList = new JComboBox();
		m_TagSelectList.setBounds( 10, 140, 120, 20 );
		m_TagSelectList.addItem( "Select Tags" );
		m_CommWriter.add( m_TagSelectList );

		// 新しいタグ名入力エリア
		m_NewTagNameInputArea = new JTextField();
		m_NewTagNameInputArea.setBounds( 10, 170, 100, 20 );
		m_CommWriter.add( m_NewTagNameInputArea );

		// 新しいタグ作成ボタン
		m_CreateTagBtn = new JButton( CREATE_TAG_BUTTON_NAME );
		m_CreateTagBtn.setBounds( 110, 170, 80, 20 );
		m_CreateTagBtn.addActionListener( this );
		m_CreateTagBtn.setActionCommand( m_CreateTagBtn.getText() );
		m_CommWriter.add( m_CreateTagBtn );

		mainWnd.add( m_CommWriter );
	}

	public void actionPerformed( ActionEvent event )
	{
		String cmd = event.getActionCommand();

		if( cmd.equals( COMMENT_BUTTON_NAME ) ){
			String options[] = new String [ 1 ];
			options[ 0 ] = m_CommInputArea.getText();
			m_MsgMediator.postMsg( ComponentID.COM_ID_COMMENT_WRITER, "Comment", options );
		}
		else if( cmd.equals( CREATE_TAG_BUTTON_NAME ) ){
			String options[] = new String [ 1 ];
			options[ 0 ] = m_NewTagNameInputArea.getText();
			m_MsgMediator.postMsg( ComponentID.COM_ID_COMMENT_WRITER, "Create Tag", options );
		}
	}

	public void procMsg( String msg )
	{
	//	if( msg.equals( "App Init" ) ){
	//		m_MetaDataHandler.loadMetaDataFile( "meta.dat" );
	//	}
	}

	public void procMsg( String msg, String[] options )
	{
	}

	public void procMsg( ComponentID from, String msg )
	{
		switch( from ){
			case COM_ID_PLAY_CONTROLLER:
				if( msg.equals( "Stop" ) ){
					m_CommFileHandler.saveFile();
				}
			default:
				break;
		}
	}

	public void procMsg( ComponentID from, String msg, String[] options )
	{


		switch( from ){
			case COM_ID_COMMENT_WRITER:
				if( msg.equals( "Comment" ) ){
					ArrayList < Integer > tagList = new ArrayList < Integer > ();
					tagList.add( m_TagSelectList.getSelectedIndex() - 1 );
					m_CommFileHandler.addComment( m_PlayTime, System.currentTimeMillis(), "nutti", options[ 0 ], tagList );
					m_CommInputArea.setText( "" );
				}
				else if( msg.equals( "Create Tag" ) ){
					if( m_CommFileHandler.tagExist( options[ 0 ] ) ){
						m_CommFileHandler.addTag( options[ 0 ] );
						m_TagSelectList.addItem( options[ 0 ] );
						m_NewTagNameInputArea.setText( "" );
					}
				}
				break;
 			case COM_ID_PLAY_CONTROLLER:
/*				if( msg.equals( "Play" ) ){
					String[] elms = options[ 0 ].split( "/" );
					String fileName = elms[ elms.length - 1 ];
					String filePath = Constant.COMMENT_FILE_DIR  + "/" + fileName + Constant.COMMENT_FILE_SUFFIX;
					// メタデータ
					if( m_MetaDataHandler.getCommentFilePath( fileName ) == null ){
						m_MetaDataHandler.addMetaData( fileName, filePath );
					}
					// コメントデータ
					if( Util.fileExist( filePath ) ){
						m_CommFileHandler.loadFile( filePath );
					}
					else{
						m_CommFileHandler.buildHeader( fileName, 0 );
						m_CommFileHandler.createFile( filePath );
					}
					// タグ情報の読み込み
					for( int i = 0; i < m_CommFileHandler.getTagEntriesTotal(); ++i ){
						m_TagSelectList.addItem( m_CommFileHandler.getTagName( i ) );
					}
				}
				else */if( msg.equals( "Stop" ) ){
					m_CommFileHandler.saveFile();
				}
				else if( msg.equals( "Update Time" ) ){
					m_PlayTime = Integer.parseInt( options[ 0 ] );
				}
 			case COM_ID_PLAY_LIST:
 				if( msg.equals( "Prepare Comment Data" ) ){
					String[] elms = options[ 0 ].split( "/" );
					String fileName = elms[ elms.length - 1 ];
					String filePath = Constant.COMMENT_FILE_DIR  + "/" + fileName + Constant.COMMENT_FILE_SUFFIX;
					// メタデータ
					if( m_MetaDataHandler.getCommentFilePath( fileName ) == null ){
						m_MetaDataHandler.addMetaData( fileName, filePath );
					}
					// コメントデータ
					if( Util.fileExist( filePath ) ){
						m_CommFileHandler.loadFile( filePath );
					}
					else{
						m_CommFileHandler.buildHeader( fileName, 0 );
						m_CommFileHandler.createFile( filePath );
					}
					// タグ情報の読み込み
					for( int i = 0; i < m_CommFileHandler.getTagEntriesTotal(); ++i ){
						m_TagSelectList.addItem( m_CommFileHandler.getTagName( i ) );
					}
				}
 				break;
			default:
				break;
		}
	}

}
