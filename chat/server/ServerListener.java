package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;

import chat.utils.Constants;
import chat.utils.Printer;
import chat.utils.Severity;

//thread used for reading commands from console in order to manage the server
class ServerListener implements Runnable {
	private Dispatcher dispatcher; 
	private BufferedReader serverReader = new BufferedReader(new InputStreamReader(System.in));
	private PrintWriter serverWriter =  new PrintWriter(System.out);
	
	public ServerListener(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	//while dispatcher is running reads lines from console and executes commands
	@Override
	public void run() {
		while(dispatcher.isRunning() && dispatcher.getServerLoger().isAvailable()) {
				String line;
				try {
					line = serverReader.readLine();
					
					switch (line) {
					case Constants.LIST_ALL_CLIENTS_COMMAND: 
						listConnectedClients();
						break;
						
					case Constants.STOP_SERVER_COMMAND:
						dispatcher.stopServer();
						dispatcher.setRunning(false);
						break;
					
					case Constants.INFO_COMMAND:
						listInfoCommand();
						break;
					
					default:
						if(line.contains(Constants.QUIT_COMMAND)) {
							String name = line.replace("q/", "");
							dispatcher.removeClient(name);
							dispatcher.getServerLoger().log(Severity.DEBUG, "server", "client: " + name + " was disconnected." );
							serverWriter.println("client: " + name + " was disconnected");
							serverWriter.flush();
						} else {
							serverWriter.println("Unknown command: " + line + ". Please use 'info/' to see all possible commands.");
							serverWriter.flush();
						}					
					}
	
				} catch (IOException ioEx) {
					dispatcher.getServerLoger().log(Severity.FATAL, "server", "An error occurred while reading from the console: " + Printer.getStackTrace(ioEx));
				}			
		}		
	}
	
	//prints list with all connected clients to server console
	private void listConnectedClients() {
		serverWriter.println("Clients connected to server: " + LocalDateTime.now());
		serverWriter.flush();
		if (dispatcher.getClients().keySet().size() == 0) {
			serverWriter.println("There is no connected clients.");
			serverWriter.flush();
			return; 
		}
		for (String name : dispatcher.getClients().keySet()) {
			serverWriter.println("client: " + dispatcher.getClients().get(name) + " | IP: " + dispatcher.getClients().get(name).getIP());
			serverWriter.flush();
		}
	}
	
	//prints information message with all possible commands to the console
	private void listInfoCommand() {
		serverWriter.println(Constants.INFO_FOR_SERVER);
		serverWriter.flush();
	}
}