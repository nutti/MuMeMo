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

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import nutti.mumemo.Constant.ComponentID;

public class PlayList extends IComponent
{

	private JPanel					m_PlayList;			// プレイリスト
	private JList					m_MusicList;		// 音楽リスト
	private DefaultListModel		m_DefListModel;		// JList項目追加用

	private DropTarget				m_DropTarget;		// ドラック＆ドロップ

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
					String[] options = new String[ 3 ];
					//options[ 0 ] = (String) m_DefListModel.getElementAt( index );
					options[ 0 ] = m_PlayListFileHandler.getMusicInfo( index ).m_FilePath;
					File file = new File( options[ 0 ] );
					options[ 1 ] = Long.toString( file.length() );
					options[ 2 ] = m_PlayListFileHandler.getMusicInfo( index ).m_MusicTitle;
					m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_LIST, ComponentID.COM_ID_COMMENT_WRITER, "Prepare Comment Data", options );
					m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_LIST, "Double Clicked", options );
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
						String fileName = f.getName();
						String filePath = f.getPath();
						if( !m_PlayListFileHandler.isExist( filePath ) ){
							options[ count++ ] = fileName;		// ファイル名
							options[ count++ ] = filePath;		// ファイルのパスを取得
							m_DefListModel.addElement( fileName );
							m_PlayListFileHandler.addItem( filePath, fileName, 0, f.length(), "tets", 0 );
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
		m_PlayList.setBounds( 10, 100, 400, 200 );
		m_PlayList.setBackground( Color.YELLOW );
		m_PlayList.setLayout( null );

		// 音楽一覧
		m_DefListModel = new DefaultListModel();
		m_MusicList = new JList( m_DefListModel );
		m_MusicList.setBounds( 10, 10, 400, 100 );
		m_MusicList.setBackground( Color.WHITE );
		m_MusicList.addMouseListener( m_MusicListML );
		m_PlayList.add( m_MusicList );

		m_DropTarget = new DropTarget( m_MusicList, m_FileDropEvent );

		// プレイリストファイルからデータの読み込みを行う。
		m_PlayListFileHandler = new PlayListFileHandler();
		m_PlayListFileHandler.load( Constant.PLAY_LIST_FILE_NAME );
		for( int i = 0; i < m_PlayListFileHandler.getEntryTotal(); ++i ){
			PlayListFileHandler.MusicInfo info = m_PlayListFileHandler.getMusicInfo( i );
			m_DefListModel.addElement( info.m_MusicTitle );
		}
		m_MusicList.ensureIndexIsVisible( m_DefListModel.size() - 1 );

		mainWnd.add( m_PlayList );
	}

	public void procMsg( String msg )
	{
	}

	public void procMsg( String msg, String[] options )
	{
	}

	public void procMsg( ComponentID from, String msg )
	{
		switch( from ){
			case COM_ID_APP_MAIN:
				//if( msg.equals( "App Init" ) ){
				//	ArrayList < String > list = m_MetaDataHandler.getMusicNameList();
				//	for( String s : list ){
				//		m_DefListModel.addElement( s );
				//		m_MusicList.ensureIndexIsVisible( m_DefListModel.size() - 1 );
				//	}
				//}

				break;
			default:
				break;
		}
	}

	public void procMsg( ComponentID from, String msg, String[] options )
	{
	}
}
