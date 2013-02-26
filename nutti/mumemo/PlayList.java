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

	// 音楽情報
	private class MusicInfo
	{
		long			m_Length;		// 音楽の長さ
		int				m_Freq;			// 周波数
		int				m_Bits;			// ビット数
		long			m_BitRate;		// ビットレート
		String			m_Format;		// ファイルフォーマット
		int				m_Channel;		// チャンネル数
		String			m_Composer;		// 作曲家
		String			m_Title;		// タイトル
		boolean			m_IsCBR;		// CBR形式ならtrue
	}

	private JPanel					m_PlayList;				// プレイリスト
	private JList					m_MusicList;			// 音楽リスト
	private DefaultListModel		m_DefListModel;			// JList項目追加用
	private JScrollPane				m_MusicListScrollBar;	// スクロールバー

	private DropTarget				m_DropTarget;			// ドラック＆ドロップ

	private MetaDataHandler			m_MetaDataHandler;
	private PlayListFileHandler		m_PlayListFileHandler;	// プレイリストファイルハンドラ

	private Map						m_AudioInfo;			// 曲情報
	private BasicPlayerListener		m_BasicListener = new BasicPlayerListener()
	{
		public void stateUpdated( BasicPlayerEvent event )
		{
		}
		public void opened( Object stream, Map properties )
		{
			m_AudioInfo = properties;
		}
		public void progress( int bytesread, long microseconds, byte[] pcmdata, Map properties )
		{
		}
		public void setController( BasicController controller )
		{
		}
	};

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
					options[ 0 ] = m_PlayListFileHandler.getMusicInfo( index ).m_FilePath;
					File file = new File( options[ 0 ] );
					options[ 1 ] = Long.toString( file.length() );
					options[ 2 ] = m_PlayListFileHandler.getMusicInfo( index ).m_MusicTitle;
					m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_LIST, Constant.MsgID.MSG_ID_STOP.ordinal(), null );
					m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_LIST, ComponentID.COM_ID_COMMENT_WRITER, "Prepare Comment Data", options );
					m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_LIST, Constant.MsgID.MSG_ID_PLAY.ordinal(), options );
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


	public void procMsg( ComponentID from, String msg )
	{
		switch( from ){
			case COM_ID_PLAY_CONTROLLER:
				if( msg.equals( "Play Button Pushed" ) ){
					int idx = m_MusicList.getSelectedIndex();
					String[] options = new String[ 3 ];
					options[ 0 ] = m_PlayListFileHandler.getMusicInfo( idx ).m_FilePath;
					File file = new File( options[ 0 ] );
					options[ 1 ] = Long.toString( file.length() );
					options[ 2 ] = m_PlayListFileHandler.getMusicInfo( idx ).m_MusicTitle;
					loadMusicInfo( options[ 0 ] );
					m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_LIST, Constant.MsgID.MSG_ID_STOP.ordinal(), null );
					m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_LIST, ComponentID.COM_ID_COMMENT_WRITER, "Prepare Comment Data", options );
					m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_LIST, Constant.MsgID.MSG_ID_PLAY.ordinal(), options );
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

	private MusicInfo loadMusicInfo( String filePath )
	{
		MusicInfo info = new MusicInfo();

		BasicPlayer player = new BasicPlayer();
		File file = new File( filePath );
		player.addBasicPlayerListener( m_BasicListener );
		try{
			player.open( file );
			// 現在の音楽再生位置を取得（秒単位）
			info.m_Length = Long.parseLong( m_AudioInfo.get( "audio.length.bytes" ).toString() );
			// ファイルタイプを取得
			String type = m_AudioInfo.get( "audio.type" ).toString();
			// .mp3の場合
			if( type.equals( "MP3" ) ){
				// ビットレートの取得
				info.m_BitRate = Long.parseLong( m_AudioInfo.get( "mp3.bitrate.nominal.bps" ).toString() );
				// チャンネル数の取得
				info.m_Channel = Integer.parseInt( m_AudioInfo.get( "mp3.channels" ).toString() );
				// CBR or VBR
				if( Boolean.parseBoolean( m_AudioInfo.get( "mp3.vbr" ).toString() ) == false ){
					info.m_IsCBR = true;
				}
				else{
					info.m_IsCBR = false;
				}
				// サンプルレートの取得
				info.m_Freq = ( int ) ( Double.parseDouble( m_AudioInfo.get( "mp3.frequency.hz" ).toString() ) );
				// ファイルフォーマットの取得
				info.m_Format = m_AudioInfo.get( "mp3.version.encoding" ).toString();
			}



		}
		catch( BasicPlayerException e ){
			e.printStackTrace();
		}

		return info;
	}
}
