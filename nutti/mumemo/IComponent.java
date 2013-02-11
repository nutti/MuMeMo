package nutti.mumemo;

public abstract class IComponent
{
	protected IMessageMediator		m_MsgMediator;
	private String					m_Name;

	public IComponent( IMessageMediator mediator, String name )
	{
		m_Name = name;
		m_MsgMediator = mediator;
	}

	public String getName()
	{
		return m_Name;
	}

	public abstract void procMsg( String msg );

	public abstract void procMsg( String msg, String[] options );
}
