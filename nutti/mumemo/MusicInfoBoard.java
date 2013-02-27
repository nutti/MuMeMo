package nutti.mumemo;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import nutti.mumemo.Constant.ComponentID;

public class MusicInfoBoard extends IComponent
{
	private JPanel			m_MusicInfoBoard;		// 音楽情報表示板
	private JTextArea		m_InfoTextArea;			// 情報表示用テキストエリア

	// コンストラクタ
	public MusicInfoBoard( JFrame mainWnd, IMessageMediator mediator )
	{
		super( mediator, ComponentID.COM_ID_MUSIC_INFO_BOARD );

		m_MusicInfoBoard = new JPanel();
		m_MusicInfoBoard.setBounds( 390, 10, 200, 70 );
		m_MusicInfoBoard.setBackground( Color.BLACK );
		m_MusicInfoBoard.setLayout( null );

		m_InfoTextArea = new JTextArea();
		m_InfoTextArea.setEditable( false );
		m_InfoTextArea.setBounds( 10, 10, 180, 50 );
		m_MusicInfoBoard.add( m_InfoTextArea );

		mainWnd.add( m_MusicInfoBoard );
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
					String str;
					str = options[ 4 ] + " Hz  ";
					str += options[ 6 ] + " bps\n";
					str += options[ 7 ] + "  ";
					if( options[ 8 ].equals( "2" ) ){
						str += "Stereo  ";
					}
					else{
						str += "Mono  ";
					}
					if( options[ 10 ].equals( "1" ) ){
						str += "CBR  ";
					}
					else{
						str += "VBR  ";
					}
					str += "\n" + options[ 9 ];

					m_InfoTextArea.setText( str );
				}
				break;
			case COM_ID_PLAY_CONTROLLER:
				if( msg == Constant.MsgID.MSG_ID_STOP.ordinal() ){
					m_InfoTextArea.setText( "" );
				}
				break;
			default:
				break;
		}
	}

}
