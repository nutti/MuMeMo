package nutti.mumemo;

import java.awt.Rectangle;

import javax.swing.JFrame;


public class Application
{
	private static final String APP_TITLE = "Music Memo";			// アプリケーションのタイトル
	private static final Rectangle WIN_BOUND = new Rectangle( 100, 100, 400, 400 );			// ウィンドウサイズ

	private JFrame		m_MainWnd;		// メインウィンドウ

	private PlayController			m_PlayCtrl;			// 音楽再生制御
	private CommentWriter			m_CommWriter;		// コメントライター
	private MessageMediator			m_MsgMediator;		// 全体制御
	private MetaDataHandler			m_MetaDataHandler;	// メタデータハンドラ
	private CommentFileHandler		m_CommFileHandler;	// コメントファイルハンドラ
	//private MusicPlayerController	m_MusicPlayerCtrl;		// 音楽再生スレッド

	public Application()
	{
		m_MainWnd = new JFrame( APP_TITLE );	// タイトルの設定
		m_MainWnd.setBounds( WIN_BOUND );		// ウィンドウサイズの設定
		m_MainWnd.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );		// ×ボタンで閉じる
		m_MainWnd.setLayout( null );			// レイアウトマネージャを停止

		m_MsgMediator = new MessageMediator();

		m_PlayCtrl = new PlayController( m_MainWnd, m_MsgMediator );
		m_MsgMediator.addComponent( m_PlayCtrl );

		m_MetaDataHandler = new MetaDataHandler();
		m_CommFileHandler = new CommentFileHandler();

		m_CommWriter = new CommentWriter( m_MainWnd, m_MsgMediator, m_MetaDataHandler, m_CommFileHandler );
		m_MsgMediator.addComponent( m_CommWriter );



		m_MsgMediator.postMsg( "App Init" );

		//m_MetaDataHandler.loadMetaDataFile( "meta.dat" );
		//String path = m_MetaDataHandler.getCommentFilePath( "touho" );
		//if( path == null ){
		//	m_MetaDataHandler.addMetaData( "touhou", "touhou.com" );
		//}

		//m_MusicPlayerCtrl = new MusicPlayerController( m_MsgMediator );
		//m_MsgMediator.addComponent( m_MusicPlayerCtrl );

		m_MainWnd.setVisible( true );

		// 終了処理を追加
		Runtime.getRuntime().addShutdownHook( new Shutdown() );
	}

	public void run()
	{
	}

	// 終了処理
	private class Shutdown extends Thread
	{
		public void run()
		{
			// アプリケーション全体に終了通知
			m_MsgMediator.postMsg( "App Exit" );
		}
	}
}
