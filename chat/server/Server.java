package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import chat.utils.Constants;
import chat.utils.Printer;
import chat.utils.Severity;

public class Server {
	private static Dispatcher dispatcher;
	private ServerListener listener;
	private ServerSocket serverSocket;

	public Server() throws IOException {	
		init();	
	}
	
	//Initializes server - open serversocket on default port, starts dispatcher thread and serverlistener thread in order to be able to 
	//read commands from console
	private void init() throws IOException {
		serverSocket = new ServerSocket(Constants.DEFAULT_PORT);
		dispatcher = new Dispatcher(serverSocket);
		listener = new ServerListener(dispatcher);
		new Thread(dispatcher).start();
		new Thread(listener).start();
		dispatcher.getServerLoger().log(Severity.DEBUG, "server ", "Strated and waiting for clients on port: " + Constants.DEFAULT_PORT);
		System.out.println("Server started and waiting for clients on port: " + Constants.DEFAULT_PORT);
	}
	
	// Accepts connections while dispatcher thread is running
	private void listenForClients() {
		while(dispatcher.isRunning() && dispatcher.getServerLoger().isAvailable()) {
			Socket s;
			try {
				//Creates client and initialize it with socket
				s = serverSocket.accept();
				Client client = new Client(s);
				//creates ClientReciever and ClientSender threads in order to be able to sends and receives messages from client
				ClientReceiver receiver = new ClientReceiver(client, dispatcher);
				ClientSender sender = new ClientSender(client, dispatcher);
				client.setReciever(receiver);
				client.setSender(sender);
				new Thread(receiver).start();
				new Thread(sender).start();
				//when the client is created and all threads are started it is added to client's map
				dispatcher.addClient(client);
				
				//when the client is created and all threads are started it is added to client's map
				dispatcher.addClient(client);
			} catch (IOException ioEx) {
				dispatcher.getServerLoger().log(Severity.FATAL, "server", "An error occurred while listening for clients. Shutting down the server: " + Printer.getStackTrace(ioEx));
				dispatcher.setRunning(false);
				return;
			}
		}	
	}
	
	//Creates server object and listens for new connections
	public static void main(String[] args) {	
		Server server = null;
		try {
			server = new Server();
		} catch (IOException ioEx) {
			dispatcher.getServerLoger().log(Severity.FATAL, "server", "An error occurred while starting the server: " + Printer.getStackTrace(ioEx));
			return;
		}
		server.listenForClients();		
	}
}