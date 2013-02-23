package nutti.mumemo;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import nutti.mumemo.Constant.ComponentID;


public class MessageMediator extends IMessageMediator
{
	Map < ComponentID, IComponent >		m_ComponentList = new HashMap < ComponentID, IComponent > ();	// コンポーネントリスト

	public MessageMediator()
	{
		m_ComponentList.clear();
	}

	// コンポーネント追加
	public void addComponent( IComponent component )
	{
		m_ComponentList.put( component.getID(), component );

	}



	public void postMsg( ComponentID from, String msg )
	{
		Iterator < ComponentID > it = m_ComponentList.keySet().iterator();
		while( it.hasNext() ){
			IComponent comp = m_ComponentList.get( it.next() );
			comp.procMsg( from, msg );
		}
	}

	public void postMsg( ComponentID from, String msg, String[] options )
	{
		Iterator < ComponentID > it = m_ComponentList.keySet().iterator();
		while( it.hasNext() ){
			IComponent comp = m_ComponentList.get( it.next() );
			comp.procMsg( from, msg, options );
		}
	}

	public void postMsg( ComponentID from, ComponentID to, String msg )
	{
		IComponent comp = m_ComponentList.get( to );
		if( comp != null ){
			comp.procMsg( from, msg );
		}
	}

	public void postMsg( ComponentID from, ComponentID to, String msg, String[] options )
	{
		IComponent comp = m_ComponentList.get( to );
		if( comp != null ){
			comp.procMsg( from, msg, options );
		}
	}

	public void postMsg( ComponentID from, int msg, String[] options )
	{
		Iterator < ComponentID > it = m_ComponentList.keySet().iterator();
		while( it.hasNext() ){
			IComponent comp = m_ComponentList.get( it.next() );
			comp.procMsg( from, msg, options );
		}
	}
}
