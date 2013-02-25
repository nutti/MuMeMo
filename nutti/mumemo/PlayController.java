package nutti.mumemo;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import nutti.mumemo.Constant.ComponentID;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;


public class PlayController extends IComponent implements ActionListener
{
	enum PlayMode
	{
		PLAY_MODE_ONCE,
		PLAY_MODE_REPEAT,
		PLAY_MODE_TOTAL,
	}

	private static final String PLAY_BUTTON_NAME = "Play";
	private static final String STOP_BUTTON_NAME = "Stop";
	private static final String PAUSE_BUTTON_NAME = "Pause";

	private static final String[] PLAY_MODE_BUTTON_NAME = { "Once", "Replay", "Play Mode" };

	private static final long serialVersionUID = 1L;

	private JPanel			m_PlayCtrl;				// プレイヤーコントロール
	private JButton			m_PlayBtn;				// 再生ボタン
	private JButton			m_StopBtn;				// 停止ボタン
	private JButton			m_PauseBtn;				// 一時停止ボタン
	private JButton			m_PlayModeBtn;			// 再生モードボタン

	private JScrollBar		m_SeekBar;				// シークバー
	private JScrollBar		m_VolumeAdjBar;			// 音量調整バー
	private JScrollBar		m_PanAdjBar;			// パン調整バー
	private JLabel			m_PlayTimeLbl;			// 再生時間

	private BasicPlayer		m_Player;				// Basic Player
	private Map				m_AudioInfo;			// 再生中の曲情報

	private int				m_PrevSec;				// 前回更新時の時間

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
			// 現在の音楽再生位置を取得
			long length = Long.parseLong( m_AudioInfo.get( "audio.length.bytes" ).toString() );		// 音楽のバイト総数
			int curSeekPos = ( int ) ( ( double )bytesread / length * m_SeekBar.getMaximum() );			// 現在のシークバーの位置

			// 音楽の長さを秒単位で取得
			// 全バイト数 / 1秒あたりの転送バイト数（ビットレート）
			String type = m_AudioInfo.get( "audio.type" ).toString();
			long bitrate = Long.parseLong( m_AudioInfo.get( "mp3.bitrate.nominal.bps" ).toString() );	// ビットレート

			long totalSecond = length / ( bitrate / 8 );						// 音楽総再生時間

			// 再生時間を秒単位で取得
			// 読み込んだバイト数 * 音楽の長さ / 音楽のバイト総数
			int curSecond = (int) ( bytesread * totalSecond / length );

			// シークバーの位置を更新しなくてはならない場合
			if( curSeekPos != m_SeekBar.getValue() && !m_SeekBar.getValueIsAdjusting() ){
				m_SeekBar.removeAdjustmentListener( m_AdjListener );

				// 音楽の長さを取得
				int dispTotMin = (int) ( totalSecond / 60 );
				int dispTotSec = (int) ( totalSecond % 60 );
				int dispCurMin = curSecond / 60;
				int dispCurSec = curSecond % 60;

				m_SeekBar.setValue( curSeekPos );
				m_PlayTimeLbl.setText( String.format( "%1$02d:%2$02d / %3$02d:%4$02d", dispCurMin, dispCurSec, dispTotMin, dispTotSec ) );
				m_SeekBar.addAdjustmentListener( m_AdjListener );

			}

			if( Math.abs( m_PrevSec - curSecond ) > 0 ){
				String[] options = new String[ 1 ];
				options[ 0 ] = Integer.toString( curSecond );
				m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_CONTROLLER, "Update Time", options );
				m_PrevSec = curSecond;
			}

			// 連続再生用チェック
			if( m_Player.getStatus() == BasicPlayer.STOPPED ){
				if( m_PlayModeBtn.getText().equals( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_REPEAT.ordinal() ] ) ){
					try{
						m_Player.play();
					}
					catch( BasicPlayerException e ){
						e.printStackTrace();
					}
				}
			}
		}
		public void setController( BasicController controller )
		{
		}
	};

	private AdjustmentListener		m_AdjListener = new AdjustmentListener()
	{
		public void adjustmentValueChanged( AdjustmentEvent event )
		{
			if( event.getSource().equals( m_SeekBar ) ){
				if( !m_SeekBar.getValueIsAdjusting() ){
					try{
						// 音楽全体の長さを取得
						long bytes = Long.parseLong( m_AudioInfo.get( "audio.length.bytes" ).toString() );
						// シークバーの位置から、再生位置を取得
						long seek = bytes * m_SeekBar.getValue() / m_SeekBar.getMaximum();
						// シーク
						m_Player.removeBasicPlayerListener( m_BasicListener );
						m_Player.seek( seek );
						m_Player.addBasicPlayerListener( m_BasicListener );
					}
					catch( BasicPlayerException e ){
						e.printStackTrace();
					}
				}
			}
			else if( event.getSource().equals( m_VolumeAdjBar ) ){
				if( !m_VolumeAdjBar.getValueIsAdjusting() ){
					double volume = 1.0 * m_VolumeAdjBar.getValue() / m_VolumeAdjBar.getMaximum();
					try {
						m_Player.setGain( volume );
					}
					catch( BasicPlayerException e ){
						e.printStackTrace();
					}
				}
			}
			else if( event.getSource().equals( m_PanAdjBar ) ){
				if( !m_PanAdjBar.getValueIsAdjusting() ){
					double pan = -1.0 + 2.0 * m_PanAdjBar.getValue() / m_PanAdjBar.getMaximum();
					try {
						m_Player.setPan( pan );
					}
					catch( BasicPlayerException e ){
						e.printStackTrace();
					}
				}
			}
		}
	};

	// コンストラクタ
	public PlayController( JFrame mainWnd, IMessageMediator mediator )
	{
		super( mediator, ComponentID.COM_ID_PLAY_CONTROLLER );


		m_PrevSec = -1;

		m_PlayCtrl = new JPanel();
		m_PlayCtrl.setBounds( 10, 10, 370, 70 );
		m_PlayCtrl.setBackground( Color.BLACK );
		m_PlayCtrl.setLayout( null );

		final int BUTTON_WIDTH		= 80;
		final int BUTTON_HEIGHT		= 20;
		final int BUTTON_OFFSET_X	= 10;

		int posX = 10;
		int posY = 10;

		// 再生ボタン作成
		m_PlayBtn = new JButton( PLAY_BUTTON_NAME );
		m_PlayBtn.setBounds( posX, posY, BUTTON_WIDTH, BUTTON_HEIGHT );
		m_PlayBtn.addActionListener( this );
		m_PlayBtn.setActionCommand( m_PlayBtn.getText() );
		m_PlayCtrl.add( m_PlayBtn );

		posX += BUTTON_WIDTH + BUTTON_OFFSET_X;

		// 停止ボタン作成
		m_StopBtn = new JButton( STOP_BUTTON_NAME );
		m_StopBtn.setBounds( posX, posY, BUTTON_WIDTH, BUTTON_HEIGHT );
		m_StopBtn.addActionListener( this );
		m_StopBtn.setActionCommand( m_StopBtn.getText() );
		m_PlayCtrl.add( m_StopBtn );

		posX += BUTTON_WIDTH + BUTTON_OFFSET_X;

		// 一時停止ボタン作成
		m_PauseBtn = new JButton( PAUSE_BUTTON_NAME );
		m_PauseBtn.setBounds( posX, posY, BUTTON_WIDTH, BUTTON_HEIGHT );
		m_PauseBtn.addActionListener( this );
		m_PauseBtn.setActionCommand( m_PauseBtn.getText() );
		m_PlayCtrl.add( m_PauseBtn );

		posX += BUTTON_WIDTH + BUTTON_OFFSET_X;

		// 再生モード作成
		m_PlayModeBtn = new JButton( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_ONCE.ordinal() ] );
		m_PlayModeBtn.setBounds( posX, posY, BUTTON_WIDTH, BUTTON_HEIGHT );
		m_PlayModeBtn.addActionListener( this );
		m_PlayModeBtn.setActionCommand( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_TOTAL.ordinal() ] );
		m_PlayCtrl.add( m_PlayModeBtn );

		// 音量調整バー作成
		m_VolumeAdjBar = new JScrollBar( JScrollBar.HORIZONTAL, 500, 0, 0, 1000 );
		m_VolumeAdjBar.setBounds( 10, 38, 110, 10 );
		m_VolumeAdjBar.addAdjustmentListener( m_AdjListener );
		m_PlayCtrl.add( m_VolumeAdjBar );

		// パン調整バー作成
		m_PanAdjBar = new JScrollBar( JScrollBar.HORIZONTAL, 500, 0, 0, 1000 );
		m_PanAdjBar.setBounds( 130, 38, 110, 10 );
		m_PanAdjBar.addAdjustmentListener( m_AdjListener );
		m_PlayCtrl.add( m_PanAdjBar );

		// シークバー作成
		m_SeekBar = new JScrollBar( JScrollBar.HORIZONTAL, 0, 0, 0, 1000 );
		m_SeekBar.setBounds( 10, 50, 270, 10 );
		m_SeekBar.addAdjustmentListener( m_AdjListener );
		m_PlayCtrl.add( m_SeekBar );

		// 再生時間表示ラベル作成
		m_PlayTimeLbl = new JLabel( "00:00 / 00:00" );
		m_PlayTimeLbl.setBounds( 290, 45, 90, 20 );
		m_PlayCtrl.add( m_PlayTimeLbl );

		// 音楽プレイヤーの作成
		m_Player = new BasicPlayer();
		m_Player.addBasicPlayerListener( m_BasicListener );
		try{
			m_Player.setGain( 1.0 * m_VolumeAdjBar.getValue() / m_VolumeAdjBar.getMaximum() );
			m_Player.setPan( -1.0 + 2.0 * m_PanAdjBar.getValue() / m_PanAdjBar.getMaximum() );
		}
		catch( BasicPlayerException e ){
			e.printStackTrace();
		}


		mainWnd.add( m_PlayCtrl );
	}

	public void actionPerformed( ActionEvent event )
	{
		String cmd = event.getActionCommand();

		if( cmd.equals( PLAY_BUTTON_NAME ) ){
			m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_CONTROLLER, "Play Button Pushed" );
		}
		else if( cmd.equals( STOP_BUTTON_NAME ) ){
			try{
				m_Player.stop();
			}
			catch( BasicPlayerException e ){
				e.printStackTrace();
			}
			m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_CONTROLLER, Constant.MsgID.MSG_ID_STOP.ordinal(), null );
		}
		else if( cmd.equals( PAUSE_BUTTON_NAME ) ){
			try{
				// 再生状態 -> 一時停止状態
				if( m_PauseBtn.getText().equals( PAUSE_BUTTON_NAME ) ){
					m_PauseBtn.setText( "Resume" );
					m_Player.pause();
				}
				// 一時停止状態 -> 再生状態
				else{
					m_PauseBtn.setText( PAUSE_BUTTON_NAME );
					m_Player.resume();
				}
			}
			catch( BasicPlayerException e ){
				e.printStackTrace();
			}
		}
		else if( cmd.equals( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_TOTAL.ordinal() ] ) ){
			if( m_PlayModeBtn.getText().equals( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_ONCE.ordinal() ] ) ){
				m_PlayModeBtn.setText( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_REPEAT.ordinal() ] );
			}
			else if( m_PlayModeBtn.getText().equals( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_REPEAT.ordinal() ] ) ){
				m_PlayModeBtn.setText( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_ONCE.ordinal() ] );
			}
		}
	}

	public void procMsg( ComponentID from, String msg )
	{
	}

	public void procMsg( ComponentID from, String msg, String[] options )
	{
	}

	public void procMsg( ComponentID from, int msg, String[] options )
	{
		switch( from ){
			case COM_ID_PLAY_LIST:
				if( msg == Constant.MsgID.MSG_ID_PLAY.ordinal() ){
					if( options.length == 3 ){
						File file = new File( options[ 0 ] );
						try{
							m_Player.open( file );
							m_Player.play();
						}
						catch( BasicPlayerException e ){
							e.printStackTrace();
						}
					}
					m_PrevSec = -1;
				}
				break;
			default:
				break;
		}
	}
}
