package nutti.mumemo;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.util.Map;

import javax.swing.JFrame;

import nutti.mumemo.Constant.ComponentID;

import javazoom.jlgui.basicplayer.*;

public class MusicPlayerController extends IComponent
{
	//private Thread			m_MusicPlayerThread;
	//private MusicPlayer		m_MusicPlayer;

	private BasicPlayer		m_Player;			// Basic Player
	private Map				m_AudioInfo;		// 再生中の曲情報

	private BasicPlayerListener		m_BasicListener = new BasicPlayerListener()
	{
		public void stateUpdated( BasicPlayerEvent event )
		{
		}
		public void opened( Object stream, Map properties )
		{
		}
		public void progress( int bytesread, long microseconds, byte[] pcmdata, Map properties )
		{
		}
		public void setController( BasicController controller )
		{
		}
	};

	private AdjustmentListener		m_AdjListener = new AdjustmentListener()
	{
		public void adjustmentValueChanged( AdjustmentEvent event )
		{

		}
	};

	public MusicPlayerController( IMessageMediator mediator )
	{
		super( mediator, "MusicPlayerController" );

	//	m_MusicPlayer = new MusicPlayer();
	//	m_MusicPlayerThread = new Thread( m_MusicPlayer );
	//	m_MusicPlayerThread.start();

		m_Player = new BasicPlayer();
		m_Player.addBasicPlayerListener( m_BasicListener );
	}

	public void procMsg( String msg )
	{
		if( msg.equals( "Play" ) ){
			//m_MusicPlayer.play( "first.mp3" );
			//m_Player.play();
		}
		else if( msg.equals( "Stop" ) ){
			//m_MusicPlayer.stop();
			try{
				m_Player.stop();
			}
			catch( BasicPlayerException e ){
				e.printStackTrace();
			}
		}
	}

	public void procMsg( String msg, String[] options )
	{
		if( msg.equals( "Play" ) ){
			if( options.length == 1 ){
				File file = new File( options[ 0 ] );
				try{
					m_Player.open( file );
					m_Player.play();
				}
				catch( BasicPlayerException e ){
					e.printStackTrace();
				}

				//m_MusicPlayer.play( options[ 0 ] );
			}
		}
	}

	@Override
	public void procMsg(ComponentID from, String msg) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void procMsg(ComponentID from, String msg, String[] options) {
		// TODO 自動生成されたメソッド・スタブ

	}
}
