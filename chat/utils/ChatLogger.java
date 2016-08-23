package chat.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class ChatLogger {
	private static ChatLogger SERVER_INSTANCE = new ChatLogger(true);
	private static ChatLogger CLIENT_INSTANCE = new ChatLogger(false);
	
	private Severity severity;
	private BufferedWriter writer;
	//Checks if ChatLogger is used by server or clienside
	private boolean isServer = false;
	//Checks if ChatLogger is available, if not the program stop its execution
	private boolean available = true;

	private ChatLogger(boolean isServer) {
		this.isServer = isServer;
		init();
	}

	public static ChatLogger getInstance(boolean isServer) {
		return isServer ? SERVER_INSTANCE : CLIENT_INSTANCE;
	}
	
	public void log(Severity sev,String clientId,String message) {
		if (severity.ordinal() > sev.ordinal()) {
			return;
		}
		synchronized(this) {
			try {
				writer.write(LocalDateTime.now() + "<--->"+ clientId +"<--->" +sev + "<--->" + message);
				writer.write(System.getProperty("line.separator"));
				writer.flush();
			} catch (IOException e) {
				//if writer does not succeed close the current and create new one, else the program stop its execution	
				try {
					writer.close();
					Path path = Paths.get("C:\\Users\\I322730\\Desktop\\default_trace.txt");
					writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
					writer.flush();
				} catch (IOException e1) {
					available = false;
				};
			}
		}
		
	}

	private void init() {
		String envSeverity = System.getProperty("chat.severity");
		if ("debug".equals(envSeverity)) {
			severity = Severity.DEBUG;
		} else if ("error".equals(envSeverity)) {
			severity = Severity.ERROR;
		} else if ("fatal".equals(envSeverity)) {
			severity = Severity.FATAL;
		}
		//TODO: default severity: error
		//TODO: remove isServer

		Path path = null;
		// Creates new path depending on server or client side
		if (isServer) {
			path = Paths.get("C:\\Users\\I322730\\default_trace_server.txt");
		} else {
			path = Paths.get("C:\\Users\\I322730\\default_trace_client.txt");
		}
		try {
			//Creates the file if it has not been created, or append logging informstion to the existing one
			writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			// if the logger is not available, the program stop its execution		
			available = false;
		}
		
	}
    
	public boolean isAvailable() {
		return available;
	}
}
