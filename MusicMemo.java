import java.io.IOException;

import nutti.lib.MultipleRunChecker;
import nutti.mumemo.*;

public class MusicMemo
{
	public static void main( String[] args )
	{
		MultipleRunChecker checker = new MultipleRunChecker( "__mumemo_multirun_chk.lock" );
		try {
			if( checker.detectMultipleRun() ){
				System.out.println( "Detect multiple run!!" );
				return;
			}
			Application app = new Application( checker );
			app.run();
		}
		catch( IOException e ){
			e.printStackTrace();
		}
	}
}
