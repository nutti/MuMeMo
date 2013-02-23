package nutti.mumemo;

public class Constant
{
	public enum ComponentID
	{
		COM_ID_PLAY_CONTROLLER,
		COM_ID_COMMENT_WRITER,
		COM_ID_COMMENT_PLAYER,
		COM_ID_APP_MAIN,
		COM_ID_PLAY_LIST,
		COM_ID_TOTAL
	}

	public enum MsgID
	{
		MSG_ID_PLAY,
		MSG_ID_STOP,
		MSG_ID_APP_INIT,
		MSG_ID_APP_TERM,
	}

	public static final String COMMENT_FILE_SUFFIX		= ".comm";
	public static final String DATA_FILE_DIR			= "dat";
	public static final String COMMENT_FILE_DIR			= DATA_FILE_DIR  + "/comment";
	public static final String META_FILE_NAME			= DATA_FILE_DIR + "/meta.dat";
	public static final String PLAY_LIST_FILE_NAME		= DATA_FILE_DIR + "/playlist.lst";
}
