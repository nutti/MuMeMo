package nutti.mumemo;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import nutti.mumemo.Constant.ComponentID;

public class PlayList extends IComponent
{

	private JPanel					m_PlayList;				// プレイリスト
	private JList					m_MusicList;			// 音楽リスト
	private DefaultListModel		m_DefListModel;			// JList項目追加用
	private JScrollPane				m_MusicListScrollBar;	// スクロールバー

	private DropTarget				m_DropTarget;			// ドラック＆ドロップ

	private MetaDataHandler			m_MetaDataHandler;
	private PlayListFileHandler		m_PlayListFileHandler;	// プレイリストファイルハンドラ

	private MouseListener			m_MusicListML = new MouseListener()
	{
		public void mouseClicked( MouseEvent event )
		{
			if( event.getClickCount() == 2 ){
				int index = m_MusicList.locationToIndex( event.getPoint() );
				if( !m_MusicList.getCellBounds( index, index ).contains( event.getPoint() ) ){
					index = -1;
				}
				if( index >= 0 ){
					playMusic( index );
				}
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

	private DropTargetAdapter			m_FileDropEvent = new DropTargetAdapter()
	{
		public void drop( DropTargetDropEvent event )
		{
			try{
				Transferable transfer = event.getTransferable();
				if( transfer.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) ){
					event.acceptDrop( DnDConstants.ACTION_COPY_OR_MOVE );
					List < File > fileList = ( List ) ( transfer.getTransferData( DataFlavor.javaFileListFlavor ) );
					String[] options = new String[ fileList.size() * 2 ];
					int count = 0;
					for( File f : fileList ){
						PlayListFileHandler.MusicInfo info = m_PlayListFileHandler.addItem( f.getPath() );
						if( info != null ){
							options[ count++ ] = info.m_Title;		// タイトル
							options[ count++ ] = info.m_FilePath;		// ファイルのパスを取得
							m_DefListModel.addElement( info.m_Title );
						}
					}
					m_MusicList.ensureIndexIsVisible( m_DefListModel.size() - 1 );
					m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_LIST, "File Dropped", options );
				}
			}
			catch( Exception e ){
				e.printStackTrace();
			}
		}
	};

	// コンストラクタ
	public PlayList( JFrame mainWnd, IMessageMediator mediator, MetaDataHandler meta )
	{
		super( mediator, ComponentID.COM_ID_PLAY_LIST );

		m_MetaDataHandler = meta;

		// プレイリスト領域
		m_PlayList = new JPanel();
		m_PlayList.setBounds( 10, 90, 370, 200 );
		m_PlayList.setBackground( Color.YELLOW );
		m_PlayList.setLayout( null );

		// 音楽一覧
		m_DefListModel = new DefaultListModel();
		m_MusicList = new JList( m_DefListModel );
		m_MusicListScrollBar = new JScrollPane( m_MusicList );
		m_MusicListScrollBar.setBounds( 10, 10, 350, 180 );
		m_MusicList.setBounds( 10, 10, 350, 180 );
		m_MusicList.setBackground( Color.WHITE );
		m_MusicList.addMouseListener( m_MusicListML );
		m_PlayList.add( m_MusicListScrollBar );

		m_DropTarget = new DropTarget( m_MusicList, m_FileDropEvent );
		m_MusicList.setDropTarget( m_DropTarget );

		// プレイリストファイルからデータの読み込みを行う。
		m_PlayListFileHandler = new PlayListFileHandler();
		m_PlayListFileHandler.load( Constant.PLAY_LIST_FILE_NAME );
		for( int i = 0; i < m_PlayListFileHandler.getEntryTotal(); ++i ){
			PlayListFileHandler.MusicInfo info = m_PlayListFileHandler.getMusicInfo( i );
			m_DefListModel.addElement( info.m_Title );
		}
		m_MusicList.ensureIndexIsVisible( m_DefListModel.size() - 1 );

		mainWnd.add( m_PlayList );
	}


	public void procMsg( ComponentID from, String msg )
	{
		switch( from ){
			case COM_ID_PLAY_CONTROLLER:
				if( msg.equals( "Play Button Pushed" ) ){
					int idx = m_MusicList.getSelectedIndex();
					playMusic( idx );
				}
				break;
			default:
				break;
		}
	}

	public void procMsg( ComponentID from, String msg, String[] options )
	{
	}

	public void procMsg( ComponentID from, int msg, String[] options )
	{
		switch( from ){
			case COM_ID_APP_MAIN:
				if( msg == Constant.MsgID.MSG_ID_APP_TERM.ordinal() ){
					m_PlayListFileHandler.closeFile();
				}
				break;
			default:
				break;
		}
	}

	private void playMusic( int idx )
	{
		String[] options = new String[ 11 ];
		PlayListFileHandler.MusicInfo info = m_PlayListFileHandler.getMusicInfo( idx );

		options[ 0 ] = info.m_FilePath;
		options[ 1 ] = Long.toString( info.m_FileSize );
		options[ 2 ] = info.m_Title;
		// 詳細データ用
		options[ 3 ] = Long.toString( info.m_Length );
		options[ 4 ] = Long.toString( info.m_Freq );
		options[ 5 ] = Long.toString( info.m_Bits );
		options[ 6 ] = Long.toString( info.m_BitRate );
		options[ 7 ] = info.m_Format;
		options[ 8 ] = Long.toString( info.m_Channel );
		options[ 9 ] = info.m_Composer;
		options[ 10 ] = Long.toString( info.m_IsCBR );

		m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_LIST, Constant.MsgID.MSG_ID_STOP.ordinal(), null );
		m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_LIST, ComponentID.COM_ID_COMMENT_WRITER, "Prepare Comment Data", options );
		m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_LIST, Constant.MsgID.MSG_ID_PLAY.ordinal(), options );
	}

}
