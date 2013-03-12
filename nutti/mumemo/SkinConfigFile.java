package nutti.mumemo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import nutti.lib.IniFileLoader;

public class SkinConfigFile
{

	public enum SkinID
	{
		SKIN_ID_PLAY_BUTTON,				// 再生ボタン
		SKIN_ID_PLAYING_BUTTON,				// 生成中ボタン
		SKIN_ID_STOP_BUTTON,				// 停止ボタン
		SKIN_ID_PAUSE_BUTTON,				// 一時停止ボタン
		SKIN_ID_PAUSING_BUTTON,				// 一時停止中ボタン
		SKIN_ID_PLAY_MODE_ONCE_BUTTON,		// 一度再生ボタン
		SKIN_ID_PLAY_MODE_REPEAT_BUTTON,	// 連続再生ボタン

		SKIN_ID_PLAY_LIST_BG,				// プレイリストの背景
		SKIN_ID_COMM_WRITER_BG,				// コメントライターの背景
	}

	private static final String PLAY_BUTTON_TAG					= "PLAY_BUTTON";
	private static final String PLAYING_BUTTON_TAG				= "PLAYING_BUTTON";
	private static final String STOP_BUTTON_TAG					= "STOP_BUTTON";
	private static final String PAUSE_BUTTON_TAG				= "PAUSE_BUTTON";
	private static final String PAUSING_BUTTON_TAG				= "PAUSING_BUTTON";
	private static final String PLAY_MODE_ONCE_BUTTON_TAG		= "PLAY_MODE_ONCE_BUTTON";
	private static final String PLAY_MODE_REPEAT_BUTTON_TAG		= "PLAY_MODE_REPEAT_BUTTON";
	private static final String PLAY_LIST_BG_TAG				= "PLAY_LIST_BG";
	private static final String COMM_WRITER_BG_TAG				= "COMM_WRITER_BG";

	private static final SkinConfigFile		m_Inst = new SkinConfigFile();

	private Map < SkinID, String >			m_SkinFilePath;		// スキンファイルのパス一覧


	private SkinConfigFile()
	{
	}

	public void load( String filePath )
	{
		m_SkinFilePath = new HashMap < SkinID, String > ();

		IniFileLoader loader = new IniFileLoader();
		loader.load( filePath, "=" );

		Map < String, String > map = loader.getValues();
		Iterator < String > it = map.keySet().iterator();
		while( it.hasNext() ){
			String key = it.next();
			String value = map.get( key );
			if( key.equals( PLAY_BUTTON_TAG ) ){
				m_SkinFilePath.put( SkinID.SKIN_ID_PLAY_BUTTON, value );
			}
			else if( key.equals( PLAYING_BUTTON_TAG ) ){
				m_SkinFilePath.put( SkinID.SKIN_ID_PLAYING_BUTTON, value );
			}
			else if( key.equals( STOP_BUTTON_TAG ) ){
				m_SkinFilePath.put( SkinID.SKIN_ID_STOP_BUTTON, value );
			}
			else if( key.equals( PAUSE_BUTTON_TAG ) ){
				m_SkinFilePath.put( SkinID.SKIN_ID_PAUSE_BUTTON, value );
			}
			else if( key.equals( PAUSING_BUTTON_TAG ) ){
				m_SkinFilePath.put( SkinID.SKIN_ID_PAUSING_BUTTON, value );
			}
			else if( key.equals( PLAY_MODE_ONCE_BUTTON_TAG ) ){
				m_SkinFilePath.put( SkinID.SKIN_ID_PLAY_MODE_ONCE_BUTTON, value );
			}
			else if( key.equals( PLAY_MODE_REPEAT_BUTTON_TAG ) ){
				m_SkinFilePath.put( SkinID.SKIN_ID_PLAY_MODE_REPEAT_BUTTON, value );
			}
			else if( key.equals( PLAY_LIST_BG_TAG ) ){
				m_SkinFilePath.put( SkinID.SKIN_ID_PLAY_LIST_BG, value );
			}
			else if( key.equals( COMM_WRITER_BG_TAG ) ){
				m_SkinFilePath.put( SkinID.SKIN_ID_COMM_WRITER_BG, value );
			}
		}
	}

	public String getSkinFileName( SkinID id )
	{
		return m_SkinFilePath.get( id );
	}

	public static SkinConfigFile getInst()
	{
		return m_Inst;
	}
}
