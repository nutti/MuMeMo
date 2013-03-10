package nutti.lib.sound;

import nutti.lib.sound.MusicPlayer.AudioInfo;
import nutti.lib.sound.MusicPlayer.StatusFlag;

public interface IMusicPlayerListener
{
	public void opened( AudioInfo info );

	public void progress( long readBytes, long microSec,  byte[] pcmData, AudioInfo info );

	public void statusUpdated( StatusFlag status );
}
