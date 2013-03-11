package nutti.mumemo;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import nutti.lib.MultipleRunChecker;
import nutti.lib.Util;
import nutti.mumemo.Constant.ComponentID;


public class Application
{
	private static final String APP_TITLE = "Music Memo";			// アプリケーションのタイトル
	private static final Rectangle WIN_BOUND = new Rectangle( 100, 100, 620, 500 );			// ウィンドウサイズ

	private JFrame		m_MainWnd;		// メインウィンドウ

	private PlayController			m_PlayCtrl;			// 音楽再生制御
	private MusicInfoBoard			m_MusicInfoBoard;	// 音楽情報板
	private CommentWriter			m_CommWriter;		// コメントライター
	private MessageMediator			m_MsgMediator;		// 全体制御
	private MetaDataHandler			m_MetaDataHandler;	// メタデータハンドラ
	private CommentFileHandler		m_CommFileHandler;	// コメントファイルハンドラ
	private CommentPlayer			m_CommPlayer;		// コメント表示
	private Menu					m_Menu;				// メニュー
	private PlayList				m_PlayList;			// プレイリスト

	private MultipleRunChecker		m_Checker;			// 多重起動チェッカ

	public Application( MultipleRunChecker checker )
	{
		m_Checker = checker;

		setupDirectories();
		if( !validateFileStructure() ){
			try{
				m_Checker.terminate();
			}catch( IOException e ){
				e.printStackTrace();
			}
			return;
		}

		Config.getInst().load();

		/*try{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		catch(ClassNotFoundException e ){
			e.printStackTrace();
		}
		catch( InstantiationException e ){
			e.printStackTrace();
		}
		catch( IllegalAccessException e ){
			e.printStackTrace();
		}
		catch( UnsupportedLookAndFeelException e ){
			e.printStackTrace();
		}*/

		m_MainWnd = new JFrame( APP_TITLE );	// タイトルの設定
		m_MainWnd.setBounds( WIN_BOUND );		// ウィンドウサイズの設定
		m_MainWnd.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );		// ×ボタンで閉じる
		m_MainWnd.setLayout( null );			// レイアウトマネージャを停止


		m_MsgMediator = new MessageMediator();

		m_PlayCtrl = new PlayController( m_MainWnd, m_MsgMediator );
		m_MsgMediator.addComponent( m_PlayCtrl );

		m_MusicInfoBoard = new MusicInfoBoard( m_MainWnd, m_MsgMediator );
		m_MsgMediator.addComponent( m_MusicInfoBoard );

		m_MetaDataHandler = new MetaDataHandler();
		m_MetaDataHandler.loadMetaDataFile( Constant.META_FILE_NAME );

		m_CommFileHandler = new CommentFileHandler();

		m_CommWriter = new CommentWriter( m_MainWnd, m_MsgMediator, m_MetaDataHandler, m_CommFileHandler );
		m_MsgMediator.addComponent( m_CommWriter );

		m_CommPlayer = new CommentPlayer( m_MainWnd, m_MsgMediator, m_CommFileHandler );
		m_MsgMediator.addComponent( m_CommPlayer );

		m_PlayList = new PlayList( m_MainWnd, m_MsgMediator, m_MetaDataHandler );
		m_MsgMediator.addComponent( m_PlayList );

		m_Menu = new Menu( m_MainWnd, m_MsgMediator );
		m_MsgMediator.addComponent( m_Menu );

		m_MainWnd.getContentPane().setBackground( Color.BLACK );
		m_MainWnd.setVisible( true );

		m_MsgMediator.postMsg( ComponentID.COM_ID_APP_MAIN,  Constant.MsgID.MSG_ID_APP_INIT.ordinal(), null );

		// 終了処理を追加
		Runtime.getRuntime().addShutdownHook( new Shutdown() );
	}

	public void run()
	{
	}

	// ディレクトリのセットアップ
	private void setupDirectories()
	{
		// musicディレクトリ
		if( !Util.fileExist( Constant.MUSIC_FILE_DIR ) ){
			Util.mkdir( Constant.MUSIC_FILE_DIR );
		}
		// datディレクトリ
		if( !Util.fileExist( Constant.DATA_FILE_DIR ) ){
			Util.mkdir( Constant.DATA_FILE_DIR );
		}
		// dat/commentディレクトリ
		if( !Util.fileExist( Constant.COMMENT_FILE_DIR ) ){
			Util.mkdir( Constant.COMMENT_FILE_DIR );
		}
	}

	// ファイル構成の正当性を確認
	private boolean validateFileStructure()
	{
		// musicディレクトリ
		if( !Util.isDirectory( Constant.MUSIC_FILE_DIR ) ){
			return false;
		}
		// datディレクトリ
		if( !Util.isDirectory( Constant.DATA_FILE_DIR ) ){
			return false;
		}
		// dat/commentディレクトリ
		if( !Util.isDirectory( Constant.COMMENT_FILE_DIR ) ){
			return false;
		}

		return true;
	}

	// 終了処理
	private class Shutdown extends Thread
	{
		public void run()
		{
			// アプリケーション全体に終了通知
			m_CommFileHandler.closeFile();
			m_MetaDataHandler.closeFile();
			m_MsgMediator.postMsg( ComponentID.COM_ID_APP_MAIN,  Constant.MsgID.MSG_ID_APP_TERM.ordinal(), null );
			try{
				m_Checker.terminate();
			}
			catch( IOException e ){
				e.printStackTrace();
			}
			Config.getInst().save();
		}
	}
}
