package nutti.mumemo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import nutti.lib.IniFileLoader;


public class Config
{
	private static final Config		m_Inst = new Config();

	private String		m_CommName;			// コメント名
	private String		m_SkinName;			// スキン名

	private static final String		COMMENT_NAME_TAG = "COMMENT_NAME";
	private static final String		SKIN_NAME_TAG = "SKIN_NAME";


	private Config()
	{
	}

	public void load()
	{
		IniFileLoader loader = new IniFileLoader();
		loader.load( Constant.CONFIG_FILE_NAME, "=" );
		m_CommName = loader.getValue( COMMENT_NAME_TAG );
		m_SkinName = loader.getValue( SKIN_NAME_TAG );
	}

	public void save()
	{
		try{
			File file = new File( Constant.CONFIG_FILE_NAME );
			FileOutputStream stream = new FileOutputStream( file );
			BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( stream ) );

			writer.write( COMMENT_NAME_TAG + "=" + m_CommName + "\n" );
			writer.write( SKIN_NAME_TAG + "=" + m_SkinName + "\n" );

			writer.close();
			stream.close();
		}
		catch( FileNotFoundException e ){
			e.printStackTrace();
		}
		catch( IOException e ){
			e.printStackTrace();
		}

	}

	public String getCommName()
	{
		return m_CommName;
	}

	public String getSkinName()
	{
		return m_SkinName;
	}

	public void setCommName( String name )
	{
		m_CommName = name;
	}

	public void setSkinName( String name )
	{
		m_SkinName = name;
	}

	public static Config getInst()
	{
		return m_Inst;
	}
}
