package nutti.mumemo;

public abstract class IMessageMediator
{
	public abstract void addComponent( IComponent component );
	public abstract void postMsg( String msg );
	public abstract void postMsg( String msg, String[] options );
}
