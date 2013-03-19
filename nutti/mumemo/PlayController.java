package nutti.mumemo;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nutti.lib.sound.IMusicPlayerListener;
import nutti.lib.sound.MusicPlayer;
import nutti.lib.sound.MusicPlayer.AudioInfo;
import nutti.lib.sound.MusicPlayer.StatusFlag;
import nutti.mumemo.Constant.ComponentID;
import nutti.mumemo.SkinConfigFile.SkinID;


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

	private JSlider			m_SeekBar;				// シークバー
	private JSlider			m_VolumeAdjBar;			// 音量調整バー
	private JSlider			m_PanAdjBar;			// パン調整バー
	private JLabel			m_PlayTimeLbl;			// 再生時間


	private int				m_PrevSec;				// 前回更新時の時間
	private boolean			m_Paused;				// ポーズされていたらtrue

	private JTextArea		m_MusicTitleBoard;		// 再生中の音楽名表示板

	private MusicPlayer		m_Player;

	private AudioInfo		m_AudioInfo;

	public enum EventID
	{
		EVENT_ID_EOM,		// End of music.
		EVENT_ID_TOTAL,
	}

	// イベントオブザーバー
	private class EventObserver
	{
		public void eventReceived( EventID event )
		{
			m_Player.stop();
			playMusic();
		}
	}

	// イベントハンドラスレッド
	private EventHandlerThread		m_EventHandler = new EventHandlerThread();
	private class EventHandlerThread implements Runnable
	{
		private Queue < EventID >			m_EventQueue;
		private boolean						m_HasTermSig;
		private EventObserver				m_EventObserver;

		public void run()
		{
			m_EventObserver = new EventObserver();
			m_EventQueue = new LinkedList < EventID > ();
			m_EventQueue.clear();
			m_HasTermSig = false;
			while( !m_HasTermSig ){
				if( m_EventQueue.isEmpty() ){
					try{
						Thread.sleep( 1 );
					}
					catch( InterruptedException e ){
						e.printStackTrace();
					}
					continue;
				}
				EventID event = m_EventQueue.poll();
				if( m_EventObserver != null ){
					m_EventObserver.eventReceived( event );
				}
			}
		}

		public void terminate()
		{
			m_HasTermSig = true;
		}

		public void notifyEvent( EventID event )
		{
			m_EventQueue.add( event );
		}

	}


	private IMusicPlayerListener		m_MusicPlayerListener = new IMusicPlayerListener()
	{
		public void opened( AudioInfo info )
		{
			m_AudioInfo = info;
		}

		public void progress( long readBytes, long microSec, byte[] pcmData, AudioInfo info )
		{
			// 現在の音楽再生位置を取得
			long length = info.m_Length;		// 音楽のバイト総数
			int curSeekPos = ( int ) ( ( double )readBytes / length * m_SeekBar.getMaximum() );			// 現在のシークバーの位置

			// 音楽の長さを秒単位で取得
			// 全バイト数 / 1秒あたりの転送バイト数（ビットレート）
			String type = "MP3";
			long bitrate = 0;
			if( type.equals( "MP3" ) ){
				bitrate = info.m_BitRate;	// ビットレート
			}
			else{
				System.exit( -1 );
			}

			long totalSecond = length / ( bitrate / 8 );						// 音楽総再生時間

			// 再生時間を秒単位で取得
			// 読み込んだバイト数 * 音楽の長さ / 音楽のバイト総数
			int curSecond = (int) ( readBytes * totalSecond / length );

			// シークバーの位置を更新しなくてはならない場合
			if( curSeekPos != m_SeekBar.getValue() && !m_SeekBar.getValueIsAdjusting() ){
				m_SeekBar.removeChangeListener( m_ChangeListener );

				// 音楽の長さを取得
				int dispTotMin = (int) ( totalSecond / 60 );
				int dispTotSec = (int) ( totalSecond % 60 );
				int dispCurMin = curSecond / 60;
				int dispCurSec = curSecond % 60;

				m_SeekBar.setValue( curSeekPos );
				m_PlayTimeLbl.setText( String.format( "%1$02d:%2$02d / %3$02d:%4$02d", dispCurMin, dispCurSec, dispTotMin, dispTotSec ) );
				m_SeekBar.addChangeListener( m_ChangeListener );

			}

			if( Math.abs( m_PrevSec - curSecond ) > 0 ){
				String[] options = new String[ 1 ];
				options[ 0 ] = Integer.toString( curSecond );
				m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_CONTROLLER, "Update Time", options );
				m_PrevSec = curSecond;
			}
		}

		public void statusUpdated( StatusFlag status )
		{
			if( status == StatusFlag.EOF ){
				if( m_PlayModeBtn.getActionCommand().equals( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_REPEAT.ordinal() ] ) ){
					//m_Player.stop();
					//playMusic();
					m_EventHandler.notifyEvent( EventID.EVENT_ID_EOM );
				}
			}
		}
	};

	private ChangeListener		m_ChangeListener = new ChangeListener()
	{
		public void stateChanged( ChangeEvent event )
		{
			if( event.getSource().equals( m_SeekBar ) ){
				if( !m_SeekBar.getValueIsAdjusting() ){
					// 音楽全体の長さを取得
					long bytes = m_AudioInfo.m_Length;
					// シークバーの位置から、再生位置を取得
					long seek = bytes * m_SeekBar.getValue() / m_SeekBar.getMaximum();
					// シーク
					m_Player.removeMusicPlayerListener();
					m_Player.seek( seek );
					m_Player.setMusicPlayerListener( m_MusicPlayerListener );
				}
			}
			else if( event.getSource().equals( m_VolumeAdjBar ) ){
				if( !m_VolumeAdjBar.getValueIsAdjusting() ){
					double volume = 1.0 * m_VolumeAdjBar.getValue() / m_VolumeAdjBar.getMaximum();
					Config.getInst().setVolume( m_VolumeAdjBar.getValue() );
					m_Player.setVolume( volume );
				}
			}
			else if( event.getSource().equals( m_PanAdjBar ) ){
				if( !m_PanAdjBar.getValueIsAdjusting() ){
					double pan = -1.0 + 2.0 * m_PanAdjBar.getValue() / m_PanAdjBar.getMaximum();
					Config.getInst().setPan( m_PanAdjBar.getValue() );
					m_Player.setPan( pan );
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

		final int BUTTON_WIDTH		= 20;
		final int BUTTON_HEIGHT		= 20;
		final int BUTTON_OFFSET_X	= 0;

		int posX = 285;
		int posY = 10;

		// 再生ボタン作成
		m_PlayBtn = new JButton();
		m_PlayBtn.setBounds( posX, posY, BUTTON_WIDTH, BUTTON_HEIGHT );
		m_PlayBtn.addActionListener( this );
		m_PlayBtn.setActionCommand( PLAY_BUTTON_NAME );
		m_PlayBtn.setContentAreaFilled( false );
		m_PlayBtn.setBorderPainted( false );
		m_PlayCtrl.add( m_PlayBtn );

		posX += BUTTON_WIDTH + BUTTON_OFFSET_X;

		// 停止ボタン作成
		m_StopBtn = new JButton();
		m_StopBtn.setBounds( posX, posY, BUTTON_WIDTH, BUTTON_HEIGHT );
		m_StopBtn.addActionListener( this );
		m_StopBtn.setActionCommand( STOP_BUTTON_NAME );
		m_StopBtn.setContentAreaFilled( false );
		m_StopBtn.setBorderPainted( false );
		m_PlayCtrl.add( m_StopBtn );

		posX += BUTTON_WIDTH + BUTTON_OFFSET_X;

		// 一時停止ボタン作成
		m_PauseBtn = new JButton();
		m_PauseBtn.setBounds( posX, posY, BUTTON_WIDTH, BUTTON_HEIGHT );
		m_PauseBtn.addActionListener( this );
		m_PauseBtn.setActionCommand( PAUSE_BUTTON_NAME );
		m_PauseBtn.setContentAreaFilled( false );
		m_PauseBtn.setBorderPainted( false );
		m_PlayCtrl.add( m_PauseBtn );

		posX += BUTTON_WIDTH + BUTTON_OFFSET_X;

		// 再生モード作成
		m_PlayModeBtn = new JButton();
		m_PlayModeBtn.setBounds( posX, posY, BUTTON_WIDTH, BUTTON_HEIGHT );
		m_PlayModeBtn.addActionListener( this );
		m_PlayModeBtn.setActionCommand( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_ONCE.ordinal() ] );
		m_PlayModeBtn.setContentAreaFilled( false );
		m_PlayModeBtn.setBorderPainted( false );
		m_PlayCtrl.add( m_PlayModeBtn );

		// 音量調整バー作成
		m_VolumeAdjBar = new JSlider( JSlider.HORIZONTAL, 0, 1000, Config.getInst().getVolume() );
		m_VolumeAdjBar.setBounds( 10, 38, 125, 10 );
		m_VolumeAdjBar.addChangeListener( m_ChangeListener );
		m_VolumeAdjBar.setBackground( Color.BLACK );
		m_PlayCtrl.add( m_VolumeAdjBar );

		// パン調整バー作成
		m_PanAdjBar = new JSlider( JSlider.HORIZONTAL, 0, 1000, Config.getInst().getPan() );
		m_PanAdjBar.setBounds( 155, 38, 125, 10 );
		m_PanAdjBar.addChangeListener( m_ChangeListener );
		m_PanAdjBar.setBackground( Color.BLACK );
		m_PlayCtrl.add( m_PanAdjBar );

		// シークバー作成
		m_SeekBar = new JSlider( JSlider.HORIZONTAL, 0, 1000, 0 );
		m_SeekBar.setBounds( 10, 50, 270, 10 );
		m_SeekBar.addChangeListener( m_ChangeListener );
		m_SeekBar.setBackground( Color.BLACK );
		m_PlayCtrl.add( m_SeekBar );

		// 再生時間表示ラベル作成
		m_PlayTimeLbl = new JLabel( "00:00 / 00:00" );
		m_PlayTimeLbl.setBounds( 290, 45, 90, 20 );
		m_PlayTimeLbl.setForeground( Color.WHITE );
		m_PlayCtrl.add( m_PlayTimeLbl );

		// 音楽プレイヤーの作成
		m_Player = new MusicPlayer();
		m_Player.setMusicPlayerListener( m_MusicPlayerListener );
		m_Player.setVolume( 1.0 * m_VolumeAdjBar.getValue() / m_VolumeAdjBar.getMaximum() );
		m_Player.setPan( -1.0 + 2.0 * m_PanAdjBar.getValue() / m_PanAdjBar.getMaximum() );

		m_MusicTitleBoard = new JTextArea();
		m_MusicTitleBoard.setEditable( false );
		m_MusicTitleBoard.setBounds( 15, 10, 260, 20 );
		m_MusicTitleBoard.setBackground( Color.BLACK );
		m_MusicTitleBoard.setForeground( Color.WHITE );
		m_MusicTitleBoard.setBorder( new EtchedBorder( EtchedBorder.RAISED, Color.WHITE, Color.GRAY ) );
		m_MusicTitleBoard.setFont( m_MusicTitleBoard.getFont().deriveFont( 11.0f ) );
		m_PlayCtrl.add( m_MusicTitleBoard );

		m_Paused = false;

		Thread th = new Thread( m_EventHandler );
		th.start();

		setupSkins();

		mainWnd.add( m_PlayCtrl );
	}

	public void actionPerformed( ActionEvent event )
	{
		String cmd = event.getActionCommand();

		if( cmd.equals( PLAY_BUTTON_NAME ) ){
			m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_CONTROLLER, "Play Button Pushed" );
		}
		else if( cmd.equals( STOP_BUTTON_NAME ) ){
			m_Player.stop();
			ImageIcon icon = new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PLAY_BUTTON ) );
			m_PlayBtn.setIcon( icon );
			if( m_Paused ){
				icon = new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PAUSE_BUTTON ) );
				m_PauseBtn.setIcon( icon );
				m_Paused = false;
			}
			m_MusicTitleBoard.setText( "" );
			m_MsgMediator.postMsg( ComponentID.COM_ID_PLAY_CONTROLLER, Constant.MsgID.MSG_ID_STOP.ordinal(), null );
		}
		else if( cmd.equals( PAUSE_BUTTON_NAME ) ){
			// 再生状態 -> 一時停止状態
			if( !m_Paused && m_Player.getStatus() == StatusFlag.PLAY ){
				m_Player.pause();
				ImageIcon icon = new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PLAY_BUTTON ) );
				m_PlayBtn.setIcon( icon );
				icon = new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PAUSING_BUTTON ) );
				m_PauseBtn.setIcon( icon );
				m_Paused = true;
			}
			// 一時停止状態 -> 再生状態
			else if( m_Paused ){
				m_Player.resume();
				ImageIcon icon = new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PLAYING_BUTTON ) );
				m_PlayBtn.setIcon( icon );
				icon = new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PAUSE_BUTTON ) );
				m_PauseBtn.setIcon( icon );
				m_Paused = false;
			}
		}
		else if( cmd.equals( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_ONCE.ordinal() ] ) ){
			ImageIcon icon = new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PLAY_MODE_REPEAT_BUTTON ) );
			m_PlayModeBtn.setIcon( icon );
			m_PlayModeBtn.setActionCommand( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_REPEAT.ordinal() ] );
		}
		else if( cmd.equals( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_REPEAT.ordinal() ] ) ){
			ImageIcon icon = new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PLAY_MODE_ONCE_BUTTON ) );
			m_PlayModeBtn.setIcon( icon );
			m_PlayModeBtn.setActionCommand( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_ONCE.ordinal() ] );
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
					m_Player.stop();
					m_Player.open( options[ 0 ] );
					playMusic();
					ImageIcon icon = new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PLAYING_BUTTON ) );
					m_PlayBtn.setIcon( icon );
					m_Paused = false;
					m_PrevSec = -1;
					m_MusicTitleBoard.setText( options[ 2 ] );
				}
				break;
			case COM_ID_MENU:
				if( msg == Constant.MsgID.MSG_ID_SKIN_CHANGED.ordinal() ){
					setupSkins();
				}
				break;
			case COM_ID_APP_MAIN:
				if( msg == Constant.MsgID.MSG_ID_APP_TERM.ordinal() ){
					m_EventHandler.terminate();
				}
				break;
			default:
				break;
		}
	}

	private void playMusic()
	{
		double volume = 1.0 * m_VolumeAdjBar.getValue() / m_VolumeAdjBar.getMaximum();
		double pan = -1.0 + 2.0 * m_PanAdjBar.getValue() / m_PanAdjBar.getMaximum();
		m_Player.play();
		m_Player.setVolume( volume );
		m_Player.setPan( pan );
	}

	private String getSkinFilePath( SkinID id )
	{
		return Constant.SKIN_FILES_DIR + "/" + Config.getInst().getSkinName() + "/" + SkinConfigFile.getInst().getSkinFileName( id );
	}

	private void setupSkins()
	{
		m_PlayBtn.setIcon( new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PLAY_BUTTON ) ) );
		m_StopBtn.setIcon( new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_STOP_BUTTON ) ) );
		if( m_Paused ){
			m_PauseBtn.setIcon( new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PAUSING_BUTTON ) ) );
		}
		else{
			m_PauseBtn.setIcon( new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PAUSE_BUTTON ) ) );
		}
		if( m_PlayModeBtn == null || m_PlayModeBtn.getActionCommand().equals( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_ONCE.ordinal() ] ) ){
			m_PlayModeBtn.setIcon( new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PLAY_MODE_ONCE_BUTTON ) ) );
		}
		else if( m_PlayModeBtn.getActionCommand().equals( PLAY_MODE_BUTTON_NAME[ PlayMode.PLAY_MODE_REPEAT.ordinal() ] ) ){
			m_PlayModeBtn.setIcon( new ImageIcon( getSkinFilePath( SkinID.SKIN_ID_PLAY_MODE_REPEAT_BUTTON ) ) );
		}
	}
}
