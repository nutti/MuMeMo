package nutti.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import nutti.mumemo.Constant;

public class IniFileLoader
{
	Map < String, String >		m_Values = new HashMap < String, String > ();

	public IniFileLoader()
	{
		m_Values.clear();
	}

	public void load( String filePath, String regex )
	{
		try{
			File file = new File( filePath );
			FileInputStream stream = new FileInputStream( file );
			BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );

			String line;
			while( ( line = reader.readLine() ) != null ){
				String[] tokens = line.split( regex, 2 );
				m_Values.put( tokens[ 0 ], tokens[ 1 ] );
			}

			reader.close();
			stream.close();
		}
		catch( FileNotFoundException e ){
			e.printStackTrace();
		}
		catch( IOException e ){
			e.printStackTrace();
		}
	}

	public String getValue( String key )
	{
		return m_Values.get( key );
	}

	public Map < String, String > getValues()
	{
		return m_Values;
	}
}
