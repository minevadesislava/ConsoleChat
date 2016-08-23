package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import chat.utils.Constants;
import chat.utils.Printer;
import chat.utils.Severity;

class Receiver implements Runnable {
	
	//------Client related------
	
	//The client for which this receiver is created
	private Client client;
    //We use buffered reader in order to be able to read lines 
    private BufferedReader consoleReader;
    private boolean isStarted;
    //------Server related------
    
    //Socket to the server
    private Socket socket;
    // Writer over the socket that allows to write lines 
	private PrintWriter socketWriter;

	public Receiver(Client client) throws IOException {
		this.client = client;
		this.socket = client.getSocket();
		this.consoleReader = new BufferedReader(new InputStreamReader(System.in));
		this.socketWriter = new PrintWriter(socket.getOutputStream());
	}
	
	public boolean isStarted() {
		return isStarted;
	}
	
	public void closeRecieverResources() throws IOException {
		consoleReader.close();
		socketWriter.close();
	}
	
	@Override
	public void run() {
		synchronized (client) {
			isStarted = true;
			client.notify();
		}
		
		while (client.isRunning()) {
			String line = null;
			try {
				line = consoleReader.readLine();
				if(line == null) {
					client.setRunning(false);
					socket.close();
					return;
				} 
				switch(line) {
					case Constants.QUIT_COMMAND:
						socketWriter.println(line);
						socketWriter.flush();
						client.setRunning(false);
						socket.close();
						break;
					case Constants.INFO_COMMAND:
						System.out.println(Constants.INFO_FOR_CLIENT);
					default:
						socketWriter.println(line);
						socketWriter.flush();
				}
			} catch (IOException ioEx) {
				client.getClientLogger().log(Severity.FATAL, client.getId(), "An error occurred while reading from the console:" + Printer.getStackTrace(ioEx));
				client.setRunning(false);
			}
		}
	}
}	