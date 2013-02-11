package nutti.mumemo;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class CommentWriter extends IComponent implements ActionListener
{
	private JPanel			m_CommWriter;		// コメントライター

	public CommentWriter( JFrame mainWnd, IMessageMediator mediator )
	{
		super( mediator, "CommentWriter" );

		m_CommWriter = new JPanel();
		m_CommWriter.setBounds( 10, 220, 300, 200 );
		m_CommWriter.setBackground( Color.WHITE );
		m_CommWriter.setLayout( null );

		mainWnd.add( m_CommWriter );
	}

	public void actionPerformed( ActionEvent event )
	{
	}

	public void procMsg( String msg )
	{

	}

	public void procMsg( String msg, String[] options )
	{
	}

}
