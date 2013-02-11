package nutti.mumemo;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.util.Map;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;


public class PlayController extends IComponent implements ActionListener
{

	private static final String PLAY_BUTTON_NAME = "Play";
	private static final String STOP_BUTTON_NAME = "Stop";
	private static final String PAUSE_BUTTON_NAME = "Pause";
	private static final String OPEN_BUTTON_NAME = "Open";

	private static final long serialVersionUID = 1L;

	private JPanel			m_PlayCtrl;				// プレイヤーコントロール
	private JButton			m_PlayBtn;				// 再生ボタン
	private JButton			m_StopBtn;				// 停止ボタン
	private JButton			m_PauseBtn;				// 一時停止ボタン
	private JButton			m_FileSelectBtn;		// ファイル選択ボタン
	private JTextField		m_MusicFileName;		// 音楽ファイル名

	private JScrollBar		m_SeekBar;				// スクロールバー
	private JLabel			m_MusicLengthLbl;		// 音楽ファイルの長さ
	private JLabel			m_PlayTimeLbl;			// 再生時間

	private BasicPlayer		m_Player;			// Basic Player
	private Map				m_AudioInfo;		// 再生中の曲情報

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
			// シークバーの位置を更新しなくてはならない場合
			if( curSeekPos != m_SeekBar.getValue() && !m_SeekBar.getValueIsAdjusting() ){
				m_SeekBar.removeAdjustmentListener( m_AdjListener );

				// 音楽の長さを秒単位で取得
				// 全バイト数 / 1秒あたりの転送バイト数（ビットレート）
				long bitrate = Long.parseLong( m_AudioInfo.get( "bitrate" ).toString() );	// ビットレート
				int totalSecond = ( int ) ( length / ( bitrate / 8 ) );						// 音楽総再生時間

				// 再生時間を秒単位で取得
				// 読み込んだバイト数 * 音楽の長さ / 音楽のバイト総数
				int curSecond = ( int ) ( bytesread * totalSecond / length );

				// 音楽の長さを取得
				int dispTotMin = totalSecond / 60;
				int dispTotSec = totalSecond % 60;
				int dispCurMin = curSecond / 60;
				int dispCurSec = curSecond % 60;

				m_SeekBar.setValue( curSeekPos );
				m_MusicLengthLbl.setText( String.format( "%1$d:%2$02d", dispTotMin, dispTotSec ) );
				m_PlayTimeLbl.setText( String.format( "%1$d:%2$02d", dispCurMin, dispCurSec ) );
				m_SeekBar.addAdjustmentListener( m_AdjListener );
			}
		}
		public void setController( BasicController controller )
		{
		}
	};

	private AdjustmentListener		m_AdjListener = new AdjustmentListener()
	{
		public void adjustmentValueChanged( AdjustmentEvent event ){
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
	};

	// コンストラクタ
	public PlayController( JFrame mainWnd, IMessageMediator mediator )
	{
		super( mediator, "PlayerController" );

		m_PlayCtrl = new JPanel();
		m_PlayCtrl.setBounds( 10, 10, 300, 200 );
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

		// シークバー作成
		m_SeekBar = new JScrollBar( JScrollBar.HORIZONTAL, 0, 0, 0, 1000 );
		m_SeekBar.setBounds( 10, 40, 150, 20 );
		m_SeekBar.addAdjustmentListener( m_AdjListener );
		m_PlayCtrl.add( m_SeekBar );

		// 再生時間表示ラベル作成
		m_PlayTimeLbl = new JLabel( "00:00" );
		m_PlayTimeLbl.setBounds( 170, 40, 40, 20 );
		m_PlayCtrl.add( m_PlayTimeLbl );

		// 音楽の長さ表示ラベル作成
		m_MusicLengthLbl = new JLabel( "00:00" );
		m_MusicLengthLbl.setBounds( 210, 40, 50, 20 );
		m_PlayCtrl.add( m_MusicLengthLbl );

		// ファイル選択ボタン作成
		m_FileSelectBtn = new JButton( OPEN_BUTTON_NAME );
		m_FileSelectBtn.setBounds( 200, 60, BUTTON_WIDTH, BUTTON_HEIGHT );
		m_FileSelectBtn.addActionListener( this );
		m_FileSelectBtn.setActionCommand( m_FileSelectBtn.getText() );
		m_PlayCtrl.add( m_FileSelectBtn );

		// 音楽ファイル名入力欄作成
		m_MusicFileName = new JTextField();
		m_MusicFileName.setBounds( 10, 60, 180, 20 );
		m_PlayCtrl.add( m_MusicFileName );

		// 音楽プレイヤーの作成
		m_Player = new BasicPlayer();
		m_Player.addBasicPlayerListener( m_BasicListener );

		mainWnd.add( m_PlayCtrl );
	}

	public void actionPerformed( ActionEvent event )
	{
		String cmd = event.getActionCommand();

		if( cmd.equals( PLAY_BUTTON_NAME ) ){
			String[] options = new String[ 1 ];
			options[ 0 ] = m_MusicFileName.getText();
			m_MsgMediator.postMsg( "Play", options );
		}
		else if( cmd.equals( STOP_BUTTON_NAME ) ){
			m_MsgMediator.postMsg( "Stop" );
		}
		else if( cmd.equals( OPEN_BUTTON_NAME ) ){
			m_MsgMediator.postMsg( "Open" );
		}
		else if( cmd.equals( PAUSE_BUTTON_NAME ) ){
			m_MsgMediator.postMsg( "Pause" );
		}
	}

	public void procMsg( String msg )
	{
		if( msg.equals( "Open" ) ){
			JFileChooser chooser = new JFileChooser();

			int selected = chooser.showOpenDialog( m_PlayCtrl );
			if( selected == JFileChooser.APPROVE_OPTION ){
				File file = chooser.getSelectedFile();
				String filePath = chooser.getCurrentDirectory() + "/" + file.getName();
				m_MusicFileName.setText( filePath.replaceAll( "\\\\", "/" ) );
			}
		}
		else if( msg.equals( "Stop" ) ){
			try{
				m_Player.stop();
			}
			catch( BasicPlayerException e ){
				e.printStackTrace();
			}
		}
		else if( msg.equals( "Pause" ) ){
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
			}
		}
	}
}
