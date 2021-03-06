package nutti.mumemo;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import javazoom.jlgui.basicplayer.BasicPlayerException;

import nutti.lib.ImagedPanel;
import nutti.lib.Util;
import nutti.mumemo.Constant.ComponentID;
import nutti.mumemo.SkinConfigFile.SkinID;

public class CommentWriter extends IComponent implements ActionListener
{
	private static final String COMMENT_BUTTON_NAME = "Comment";
	private static final String CREATE_TAG_BUTTON_NAME = "Create Tag";

	private ImagedPanel				m_CommWriter;					// コメントライター
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
		m_CommWriter = new ImagedPanel( getSkinFilePath( SkinID.SKIN_ID_COMM_WRITER_BG ) );
		m_CommWriter.setBounds( 390, 90, 200, 200 );
		m_CommWriter.setBackground( Color.WHITE );
		m_CommWriter.setLayout( null );

		// コメント入力エリア
		m_CommInputArea = new JTextArea();
		m_CommInputArea.setBounds( 20, 30, 160, 100 );
		m_CommInputArea.addMouseListener( m_CommInputAreaML );
		m_CommInputArea.setLineWrap( true );
		m_CommInputArea.setBackground( Color.BLACK );
		m_CommInputArea.setForeground( Color.WHITE );
		m_CommInputArea.setBorder( new EtchedBorder( EtchedBorder.RAISED, Color.WHITE, Color.GRAY ) );
		m_CommInputArea.setFont( m_CommInputArea.getFont().deriveFont( 11.0f ) );
		m_CommInputArea.setText( "Input Comment Here!" );
		m_CommWriter.add( m_CommInputArea );
		m_ClickedCommInputAreaFirst = false;

		// コメント入力ボタン
		m_CommButton = new JButton();
		m_CommButton.setBounds( 160, 140, 20, 20 );
		m_CommButton.addActionListener( this );
		m_CommButton.setActionCommand( COMMENT_BUTTON_NAME );
		m_CommButton.setContentAreaFilled( false );
		m_CommButton.setBorderPainted( false );
		m_CommWriter.add( m_CommButton );

		// 選択可能なタグリスト
		m_TagSelectList = new JComboBox();
		m_TagSelectList.setBounds( 20, 140, 130, 20 );
		m_TagSelectList.addItem( "Select Tags" );
		m_CommWriter.add( m_TagSelectList );

		// 新しいタグ名入力エリア
		m_NewTagNameInputArea = new JTextField();
		m_NewTagNameInputArea.setBounds( 20, 170, 130, 20 );
		m_CommWriter.add( m_NewTagNameInputArea );

		// 新しいタグ作成ボタン
		m_CreateTagBtn = new JButton();
		m_CreateTagBtn.setBounds( 160, 170, 20, 20 );
		m_CreateTagBtn.addActionListener( this );
		m_CreateTagBtn.setActionCommand( CREATE_TAG_BUTTON_NAME );
		m_CreateTagBtn.setContentAreaFilled( false );
		m_CreateTagBtn.setBorderPainted( false );
		m_CommWriter.add( m_CreateTagBtn );

		setupSkins();

		mainWnd.add( m_CommWriter );
	}

	public void actionPerformed( ActionEvent event )
	{
		String cmd = event.getActionCommand();

		if( cmd.equals( COMMENT_BUTTON_NAME ) ){
			// タグが選択されていない場合は無視
			if( m_TagSelectList.getSelectedIndex() != 0 ){
				String options[] = new String [ 1 ];
				options[ 0 ] = m_CommInputArea.getText();
				ArrayList < Integer > tagList = new ArrayList < Integer > ();
				tagList.add( m_TagSelectList.getSelectedIndex() - 1 );
				// コメントファイルにコメント追加
				Date date = new Date();
				m_CommFileHandler.addComment( m_PlayTime, date.getTime(), Config.getInst().getCommName(), options[ 0 ], tagList );
				// コメント欄消去
				m_CommInputArea.setText( "" );
				// メッセージ送信
				m_MsgMediator.postMsg( ComponentID.COM_ID_COMMENT_WRITER, "Create Comment", options );
			}
		}
		else if( cmd.equals( CREATE_TAG_BUTTON_NAME ) ){
			// ※バグ、音楽再生していない時は追加できないようにする。
			String options[] = new String [ 1 ];
			options[ 0 ] = m_NewTagNameInputArea.getText();
			// タグが存在しない場合、追加
			if( !m_CommFileHandler.tagExist( options[ 0 ] ) ){
				m_CommFileHandler.addTag( options[ 0 ] );
				m_TagSelectList.addItem( options[ 0 ] );
				m_NewTagNameInputArea.setText( "" );
			}
			// メッセージ送信
			m_MsgMediator.postMsg( ComponentID.COM_ID_COMMENT_WRITER, "Create Tag", options );
		}
	}


	public void procMsg( ComponentID from, String msg )
	{
	}

	public void procMsg( ComponentID from, String msg, String[] options )
	{
		switch( from ){
 			case COM_ID_PLAY_CONTROLLER:
 				if( msg.equals( "Update Time" ) ){
					m_PlayTime = Integer.parseInt( options[ 0 ] );
				}
 			case COM_ID_PLAY_LIST:
 				if( msg.equals( "Prepare Comment Data" ) ){
					//String[] elms = options[ 2 ].split( "/" );
					long musicLen = Long.parseLong( options[ 1 ] );
					String musicName = options[ 2 ];
					String filePath = Constant.COMMENT_FILE_DIR  + "/[" + options[ 1 ] + "](" + options[ 9 ] + ") " + musicName + Constant.COMMENT_FILE_SUFFIX;
					// メタデータ
					if( m_MetaDataHandler.getCommentFilePath( musicName, musicLen ) == null ){
						m_MetaDataHandler.addMetaData( musicName, musicLen, filePath );
					}
					// コメントデータ
					if( Util.fileExist( filePath ) ){
						m_CommFileHandler.loadFile( filePath );
					}
					else{
						m_CommFileHandler.createFile( filePath );
						m_CommFileHandler.buildHeader( musicName, 0 );
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

	public void procMsg( ComponentID from, int msg, String[] options )
	{
		switch( from ){
			case COM_ID_PLAY_LIST:
			case COM_ID_PLAY_CONTROLLER:
				if( msg == Constant.MsgID.MSG_ID_STOP.ordinal() ){
					m_CommFileHandler.closeFile();
					cleanupTags();
				}
				break;
			case COM_ID_APP_MAIN:
				if( msg == Constant.MsgID.MSG_ID_APP_TERM.ordinal() ){
					m_CommFileHandler.closeFile();
				}
				break;
			case COM_ID_MENU:
				if( msg == Constant.MsgID.MSG_ID_SKIN_CHANGED.ordinal() ){
					setupSkins();
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

	private String getSkinFilePath( SkinID id )
	{
		return Constant.SKIN_FILES_DIR + "/" + Config.getInst().getSkinName() + "/" + SkinConfigFile.getInst().getSkinFileName( id );
	}

	private void setupSkins()
	{
		m_CommButton.setIcon( new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_COMMENT_BUTTON ) ) );
		m_CreateTagBtn.setIcon( new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_ADD_TAG_BUTTON ) ) );
		m_CommWriter.setImage( getSkinFilePath( SkinID.SKIN_ID_COMM_WRITER_BG ) );
		m_CommWriter.repaint();
	}

}
