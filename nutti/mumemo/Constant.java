package nutti.mumemo;

public class Constant
{
	public static final long		MAJOR_VERSION	= 0;
	public static final long		MINOR_VERSION	= 10 << 8 | 0;

	public static final long		MUMEMO_VERSION	= MAJOR_VERSION << 16 | MINOR_VERSION;

	public enum ComponentID
	{
		COM_ID_PLAY_CONTROLLER,
		COM_ID_MUSIC_INFO_BOARD,
		COM_ID_COMMENT_WRITER,
		COM_ID_COMMENT_PLAYER,
		COM_ID_APP_MAIN,
		COM_ID_PLAY_LIST,
		COM_ID_MENU,
		COM_ID_TOTAL
	}

	public enum MsgID
	{
		MSG_ID_PLAY,
		MSG_ID_STOP,
		MSG_ID_APP_INIT,
		MSG_ID_APP_TERM,
		MSG_ID_SKIN_CHANGED,
	}

	public static final String COMMENT_FILE_SUFFIX		= ".comm";
	public static final String DATA_FILE_DIR			= "dat";
	public static final String COMMENT_FILE_DIR			= DATA_FILE_DIR  + "/comment";
	public static final String META_FILE_NAME			= DATA_FILE_DIR + "/meta.dat";
	public static final String PLAY_LIST_FILE_NAME		= DATA_FILE_DIR + "/playlist.lst";
	public static final String MUSIC_FILE_DIR			= "music";
	public static final String SKIN_FILES_DIR			= "skin";
	public static final String CONFIG_FILE_DIR			= "config";
	public static final String CONFIG_FILE_NAME			= CONFIG_FILE_DIR + "/mumemo.ini";
}
