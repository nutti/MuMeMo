package nutti.mumemo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;

import nutti.mumemo.Constant.ComponentID;

public class Menu extends IComponent
{
	private JMenuBar							m_MenuBar;			// メニューバー

	private JMenu								m_ConfigMenu;		// 「設定メニュー」

	private JMenu								m_SkinConfigRoot;	// 「スキンの設定」
	private ArrayList < JCheckBoxMenuItem >		m_SkinList;			// スキンリスト

	private JMenuItem							m_CommNameConfig;	// 「コメント名設定」

	private ActionListener						m_MenuActionListener = new ActionListener()
	{
		public void actionPerformed( ActionEvent event )
		{
			for( JCheckBoxMenuItem skin : m_SkinList ){
				if( event.getActionCommand().equals( skin.getActionCommand() ) ){
					skin.setSelected( true );
					Config.getInst().setSkinName( skin.getActionCommand() );
				}
				else{
					skin.setSelected( false );
				}
			}
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
		m_ConfigMenu.add( m_CommNameConfig );
		m_MenuBar.add( m_ConfigMenu );

		mainWnd.setJMenuBar( m_MenuBar );
	}

	public void procMsg( ComponentID from, String msg )
	{
	}

	public void procMsg( ComponentID from, String msg, String[] options )
	{
	}
}
