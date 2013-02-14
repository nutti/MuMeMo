package nutti.lib;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Util
{
	public static void saveStringUTF8( FileOutputStream stream, String str ) throws UnsupportedEncodingException, IOException
	{
		int len = str.getBytes( "UTF-8" ).length;
		stream.write( str.getBytes( "UTF-8" ), 0, len );
	}

	public static void saveInt( FileOutputStream stream, int val ) throws IOException
	{
		stream.write( val );
	}

	public static String loadStringUTF8( FileInputStream stream, int len ) throws UnsupportedEncodingException, IOException, LibException
	{
		if( ( len = stream.read() ) == -1 ){
			throw new LibException( "Failed to read." );
		}
		byte[] buf;
		buf = new byte [ len ];
		if( stream.read( buf, 0, len ) == -1 ){
			throw new LibException( "Failed to read." );
		}
		return new String( buf, "UTF-8" );
	}

	public static int loadInt( FileInputStream stream ) throws IOException
	{
		return stream.read();
	}
}
