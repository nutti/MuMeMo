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
		for( int i = 0; i < 4; ++i ){
			stream.write( ( int ) ( ( val >> ( i * 8 ) ) & 0xFF ) );
		}
	}

	public static void saveLong( FileOutputStream stream, long val ) throws IOException
	{
		for( int i = 0; i < 8; ++i ){
			stream.write( ( int ) ( ( val >> ( i * 8 ) ) & 0xFF ) );
		}
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

	public static int loadInt( FileInputStream stream ) throws IOException, LibException
	{
		int value = 0;

		for( int i = 0; i < 4; ++i ){
			int val = stream.read();
			if( val == -1 ){
				throw new LibException( "Failed to read.");
			}
			value |= ( val ) << ( i * 8 );
		}

		return value;
	}

	public static long loadLong( FileInputStream stream ) throws IOException, LibException
	{
		long value = 0;

		for( int i = 0; i < 8; ++i ){
			int val = stream.read();
			if( val == -1 ){
				throw new LibException( "Failed to read.");
			}
			value |= (long)( ( (long) ( val ) ) << ( i * 8 ) );
		}

		return value;
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

	public static boolean isDirectory( String fileName )
	{
		File file = new File( fileName );

		return file.isDirectory();
	}

	public static void mkdir( String fileName )
	{
		File file = new File( fileName );

		file.mkdir();
	}

}
