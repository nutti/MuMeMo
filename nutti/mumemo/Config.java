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
	private double		m_Volume;			// 音量
	private double		m_Pan;				// パン

	private static final String COMMENT_NAME_TAG	= "COMMENT_NAME";
	private static final String SKIN_NAME_TAG		= "SKIN_NAME";
	private static final String VOLUME_TAG			= "VOLUME";
	private static final String PAN_TAG				= "PAN";


	private Config()
	{
		m_Volume = 0.5;
		m_Pan = 0.5;
		m_CommName = "";
		m_SkinName = "";
	}

	public void load()
	{
		IniFileLoader loader = new IniFileLoader();
		loader.load( Constant.CONFIG_FILE_NAME, "=" );
		m_CommName = loader.getValue( COMMENT_NAME_TAG );
		m_SkinName = loader.getValue( SKIN_NAME_TAG );
		m_Volume = Double.parseDouble( loader.getValue( VOLUME_TAG ) );
		m_Pan = Double.parseDouble( loader.getValue( PAN_TAG ) );
	}

	public void save()
	{
		try{
			File file = new File( Constant.CONFIG_FILE_NAME );
			FileOutputStream stream = new FileOutputStream( file );
			BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( stream ) );

			writer.write( COMMENT_NAME_TAG + "=" + m_CommName + "\n" );
			writer.write( SKIN_NAME_TAG + "=" + m_SkinName + "\n" );
			writer.write( VOLUME_TAG + "=" + Double.toString( m_Volume ) + "\n" );
			writer.write( PAN_TAG + "=" + Double.toString( m_Pan ) + "\n" );

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

	public double getVolume()
	{
		return m_Volume;
	}

	public double getPan()
	{
		return m_Pan;
	}

	public void setCommName( String name )
	{
		m_CommName = name;
	}

	public void setSkinName( String name )
	{
		m_SkinName = name;
	}

	public void setVolume( double volume )
	{
	}

	public void setPan( double pan )
	{

	}

	public static Config getInst()
	{
		return m_Inst;
	}
}
