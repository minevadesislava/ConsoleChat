package chat.utils;

public interface Constants {
	public static final int DEFAULT_PORT = 8025;
	public static final String DEFAULT_HOST = "localhost";
	public static final String LIST_ALL_CLIENTS_COMMAND = "list/";
	public static final String STOP_SERVER_COMMAND = "stop/";
	public static final String QUIT_COMMAND = "q/";
	public static final String NICKNAME_COMMAND = "nickname/";
	public static final String SENDTO_COMMAND = ">>";
	public static final String INFO_COMMAND = "info/";
	public static final String INFO_FOR_CLIENT = " 'q/' - stops connection with server, 'nickname/' - change user's nickname, '->' - sends message only to one client ";
	public static final String INFO_FOR_SERVER = " 'stop/' - stops the server, 'list/' - list all connected clients , 'q/<client>'- disconnect client";
}
