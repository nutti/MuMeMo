package nutti.mumemo;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;

import nutti.mumemo.Constant.ComponentID;
import nutti.mumemo.Constant.MsgID;
import nutti.mumemo.SkinConfigFile.SkinID;

public class Menu extends IComponent
{
	private JMenuBar							m_MenuBar;			// メニューバー

	private JMenu								m_ConfigMenu;		// 「設定メニュー」

	private JMenu								m_SkinConfigRoot;	// 「スキンの設定」
	private ArrayList < JCheckBoxMenuItem >		m_SkinList;			// スキンリスト

	private JMenuItem							m_CommNameConfig;	// 「コメント名設定」

	private JFrame								m_MainWnd;			// メインウィンドウ

	private ActionListener						m_MenuActionListener = new ActionListener()
	{
		public void actionPerformed( ActionEvent event )
		{
			// コメント名変更
			if( event.getActionCommand().equals( "Change Comment Name" ) ){
				m_CommNameCfgWin = new CommentNameConfigWindow( m_MainWnd, true );
				m_CommNameCfgWin.setVisible( true );
			}
			// スキン変更
			else{
				for( JCheckBoxMenuItem skin : m_SkinList ){
					if( event.getActionCommand().equals( skin.getActionCommand() ) ){
						skin.setSelected( true );
						Config.getInst().setSkinName( skin.getActionCommand() );
						SkinConfigFile.getInst().load(	Constant.SKIN_FILES_DIR + "/" +
														Config.getInst().getSkinName() + "/"  +
														Config.getInst().getSkinName() + ".ini" );
						String s = SkinConfigFile.getInst().getSkinFileName( SkinID.SKIN_ID_PLAY_BUTTON );
						m_MsgMediator.postMsg( ComponentID.COM_ID_MENU, MsgID.MSG_ID_SKIN_CHANGED.ordinal(), null );
					}
					else{
						skin.setSelected( false );
					}
				}
			}
		}
	};

	// コメント名設定ダイアログ
	private CommentNameConfigWindow		m_CommNameCfgWin;
	private class CommentNameConfigWindow extends JDialog
	{
		private static final long serialVersionUID = 1L;
		
		private JLabel			m_CurNameLbl;		// 現在のラベル名
		private JTextField		m_CurName;			// 現在のコメント者名
		private JLabel			m_NewNameLbl;		// 変更後のラベル名
		private JTextField		m_NewName;			// 変更後のコメント者名
		private JButton			m_OKBtn;			// OKボタン
		private JButton			m_CancelBtn;		// キャンセルボタン

		private ActionListener		m_AL = new ActionListener()
		{
			public void actionPerformed( ActionEvent event )
			{
				String cmd = event.getActionCommand();

				if( cmd.equals( "OK" ) ){
					Config.getInst().setCommName( m_NewName.getText() );
					setVisible( false );
				}
				else if( cmd.equals( "Cancel" ) ){
					setVisible( false );
				}
			}
		};

		public CommentNameConfigWindow( Frame owner, boolean modal )
		{
			super( owner, modal );

			setLayout( null );
			setBounds( new Rectangle( 100, 100, 220, 200 ) );
			getContentPane().setBackground( Color.BLACK );
			setTitle( "Config Comment Name" );

			m_CurNameLbl = new JLabel( "Current Comment Name" );
			m_CurNameLbl.setBounds( 20, 20, 160, 17 );
			m_CurNameLbl.setBackground( Color.BLACK );
			m_CurNameLbl.setForeground( Color.WHITE );
			add( m_CurNameLbl );

			m_CurName = new JTextField( Config.getInst().getCommName() );
			m_CurName.setEditable( false );
			m_CurName.setBounds( 20, 40, 160, 17 );
			m_CurName.setBackground( Color.BLACK );
			m_CurName.setForeground( Color.WHITE );
			add( m_CurName );

			m_NewNameLbl = new JLabel( "New Comment Name" );
			m_NewNameLbl.setBounds( 20, 70, 160, 17 );
			m_NewNameLbl.setBackground( Color.BLACK );
			m_NewNameLbl.setForeground( Color.WHITE );
			add( m_NewNameLbl );

			m_NewName = new JTextField();
			m_NewName.setBounds( 20, 90, 160, 17 );
			m_NewName.setBackground( Color.BLACK );
			m_NewName.setForeground( Color.WHITE );
			add( m_NewName );

			m_OKBtn = new JButton( "OK" );
			m_OKBtn.setBounds( 20, 130, 60, 20 );
			m_OKBtn.setActionCommand( "OK" );
			m_OKBtn.addActionListener( m_AL );
			add( m_OKBtn );

			m_CancelBtn = new JButton( "Cancel" );
			m_CancelBtn.setBounds( 100, 130, 80, 20 );
			m_CancelBtn.setActionCommand( "Cancel" );
			m_CancelBtn.addActionListener( m_AL );
			add( m_CancelBtn );
		}
	};

	public Menu( JFrame mainWnd, IMessageMediator mediator )
	{
		super( mediator, ComponentID.COM_ID_MENU );

		m_MenuBar = new JMenuBar();

		// Configメニューの作成
		m_ConfigMenu = new JMenu( "Config" );


		// 利用可能なSkinの一覧を作成
		m_SkinList = new ArrayList < JCheckBoxMenuItem > ();
		File skinDir = new File( Constant.SKIN_FILES_DIR );
		File[] files = skinDir.listFiles();
		for( File f : files ){
			if( f.isDirectory() ){
				JCheckBoxMenuItem newItem = new JCheckBoxMenuItem( f.getName() );
				newItem.addActionListener( m_MenuActionListener );
				if( f.getName().equals( Config.getInst().getSkinName() ) ){
					newItem.setSelected( true );
				}
				m_SkinList.add( newItem );
			}
		}

		// Skinメニューの作成
		m_SkinConfigRoot = new JMenu( "Skin" );
		for( JMenuItem item : m_SkinList ){
			m_SkinConfigRoot.add( item );
		}
		m_ConfigMenu.add( m_SkinConfigRoot );
		// コメント名変更
		m_CommNameConfig = new JMenuItem( "Change Comment Name" );
		m_CommNameConfig.addActionListener( m_MenuActionListener );
		m_ConfigMenu.add( m_CommNameConfig );
		m_MenuBar.add( m_ConfigMenu );

		mainWnd.setJMenuBar( m_MenuBar );

		m_MainWnd = mainWnd;
	}

	public void procMsg( ComponentID from, String msg )
	{
	}

	public void procMsg( ComponentID from, String msg, String[] options )
	{
	}
}
