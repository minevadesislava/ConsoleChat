package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import chat.utils.Constants;
import chat.utils.Printer;
import chat.utils.Severity;

class Sender implements Runnable {
	// -------Client related ------
	
	//The client for which this receiver is created
	private Client client;
	// We use print writer in order to print received messages from the server on the console. 
	private PrintWriter consoleWriter;
	
    private boolean isStarted;

	
	//------Server related------
	
	//Socket to server 
	private Socket socket;
	//Reader over the socket that allows to read lines
	private BufferedReader socketReader;
	

	public Sender(Client client) throws IOException {
		this.client = client;
		this.socket = client.getSocket();
		socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		consoleWriter = new PrintWriter(System.out);		
	}
	
	public void closeRecieverResources() throws IOException {
		consoleWriter.close();
		socketReader.close();
	}

	@Override
	public void run() {		
		synchronized (client) {
			client.notify();
			isStarted = true;
		}
		consoleWriter.println("Successfully connected to server :)");
		consoleWriter.flush();
		// if client is running sender thread will continue to read from the socket, otherwise it will stop 
		while (client.isRunning()) {
			String line;
			try {
				line = socketReader.readLine();
				//TODO: remove quit check
				//Line can be null when the connection with server is broken 
				if ((line == null) || Constants.QUIT_COMMAND.equals(line)) {
					client.setRunning(false);
				}
				if (line != null) {
					consoleWriter.println(line);
					consoleWriter.flush();
				}
				
				
//				if (line == null) {
//					client.setRunning(false);
//				} else {
//					if (Constants.QUIT_COMMAND.equals(line)) {
//						client.setRunning(false);
//					}
//			
//					consoleWriter.println(line);
//					consoleWriter.flush();
//				}

			} catch (IOException ioEx) {
				client.getClientLogger().log(Severity.FATAL, client.getId(), "An error occurred while reading from socket: " + Printer.getStackTrace(ioEx));
				client.setRunning(false);
			}
		}	
		consoleWriter.println("Connection with server was lost!");
		consoleWriter.flush();
	}
	
	public boolean isStarted() {
		return isStarted;
	}
}
