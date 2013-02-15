package nutti.mumemo;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import nutti.mumemo.Constant.ComponentID;


public class MessageMediator extends IMessageMediator
{
	Map < String, IComponent >		m_ComponentList = new HashMap < String, IComponent > ();	// コンポーネントリスト

	public MessageMediator()
	{
		m_ComponentList.clear();
	}

	// コンポーネント追加
	public void addComponent( IComponent component )
	{
		m_ComponentList.put( component.getName(), component );

	}

	// 全体へメッセージ送信
	public void postMsg( String msg )
	{
		Iterator < String > it = m_ComponentList.keySet().iterator();
		while( it.hasNext() ){
			IComponent comp = m_ComponentList.get( it.next() );
			comp.procMsg( msg );
		}
	}

	// 全体へメッセージ送信（オプション付）
	public void postMsg( String msg, String[] options )
	{
		Iterator < String > it = m_ComponentList.keySet().iterator();
		while( it.hasNext() ){
			IComponent comp = m_ComponentList.get( it.next() );
			comp.procMsg( msg, options );
		}
	}

	// 指定コンポーネントへメッセージ送信
	public void postMsg( String name, String msg )
	{
		IComponent comp = m_ComponentList.get( name );
		if( comp != null ){
			comp.procMsg( msg );
		}
	}

	public void postMsg( ComponentID from, String msg )
	{
		Iterator < String > it = m_ComponentList.keySet().iterator();
		while( it.hasNext() ){
			IComponent comp = m_ComponentList.get( it.next() );
			comp.procMsg( from, msg );
		}
	}

	public void postMsg( ComponentID from, String msg, String[] options )
	{
		Iterator < String > it = m_ComponentList.keySet().iterator();
		while( it.hasNext() ){
			IComponent comp = m_ComponentList.get( it.next() );
			comp.procMsg( from, msg, options );
		}
	}
}
