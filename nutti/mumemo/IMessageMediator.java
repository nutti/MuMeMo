package nutti.mumemo;

import nutti.mumemo.Constant.ComponentID;

public abstract class IMessageMediator
{
	public abstract void addComponent( IComponent component );
	public abstract void postMsg( String msg );
	public abstract void postMsg( String msg, String[] options );
	public abstract void postMsg( ComponentID from, String msg );
	public abstract void postMsg( ComponentID from, String msg, String[] options );

	public abstract void postMsg( ComponentID from, ComponentID to, String msg );
	public abstract void postMsg( ComponentID from, ComponentID to, String msg, String[] options );
}
