package nutti.mumemo;

import nutti.mumemo.Constant.ComponentID;

public abstract class IComponent
{
	protected IMessageMediator		m_MsgMediator;
	private ComponentID						m_ID;			// コンポーネントID

	public IComponent( IMessageMediator mediator, ComponentID id )
	{
		m_MsgMediator = mediator;
		m_ID = id;
	}

	public ComponentID getID()
	{
		return m_ID;
	}

	public abstract void procMsg( ComponentID from, String msg );

	public abstract void procMsg( ComponentID from, String msg, String[] options );

	public void procMsg( ComponentID from, int msg, String[] options )
	{
	}

}
