package nutti.mumemo;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

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

	private MetaDataHandler			m_MetaDataHandler;

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
					String[] options = new String[ 1 ];
					options[ 0 ] = (String) m_DefListModel.getElementAt( index );
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

		// コメント一覧
		m_DefListModel = new DefaultListModel();
		m_MusicList = new JList( m_DefListModel );
		m_MusicList.setBounds( 10, 10, 400, 100 );
		m_MusicList.setBackground( Color.WHITE );
		m_MusicList.addMouseListener( m_MusicListML );
		m_PlayList.add( m_MusicList );

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
				if( msg.equals( "App Init" ) ){
					ArrayList < String > list = m_MetaDataHandler.getMusicNameList();
					for( String s : list ){
						m_DefListModel.addElement( s );
						m_MusicList.ensureIndexIsVisible( m_DefListModel.size() - 1 );
					}
				}

				break;
			default:
				break;
		}
	}

	public void procMsg( ComponentID from, String msg, String[] options )
	{
	}
}
