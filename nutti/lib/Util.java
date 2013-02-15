package nutti.lib;

import java.io.File;
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

	public static void saveLong( FileOutputStream stream, long val ) throws IOException
	{
		int hi = (int) ( ( ( val ) >> 32 ) & 0xFFFFFFFF );
		int lo = (int) ( val & 0xFFFFFFFF );

		stream.write( hi );
		stream.write( lo );
	}

	public static String loadStringUTF8( FileInputStream stream, int len ) throws UnsupportedEncodingException, IOException, LibException
	{
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

	public static long loadLong( FileInputStream stream ) throws IOException
	{
		int hi = stream.read();
		int lo = stream.read();

		return ( (long)(hi) << 32 | (long)lo );
	}

	public static int getStringUTF8Byte( String str ) throws UnsupportedEncodingException
	{
		return str.getBytes( "UTF-8" ).length;
	}

	public static boolean fileExist( String fileName )
	{
		File file = new File( fileName );

		return file.exists();
	}


}
