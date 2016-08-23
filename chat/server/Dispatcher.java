package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import chat.utils.ChatLogger;
import chat.utils.Constants;
import chat.utils.Printer;
import chat.utils.Severity;


// Dispatcher thread holds the list for all conected clients and keep all recieved messages in queue.
// When new client is connected to server socket, it is added to clients map with key-default nickname and value - object Client

class Dispatcher implements Runnable {
	//------Server related------
	//Initilize server logger
	private static final ChatLogger SERVER_LOGER = ChatLogger.getInstance(true);
	//Keeps all connected clients
	private Map<String, Client> clients;
	//Keeps all unsent messages
	private List<String> messages;
	//Listening for new clients
	private ServerSocket serverSocket;
	private boolean running; 
	
	
	public Dispatcher(ServerSocket serverSocket) throws SecurityException, IOException{
		this.serverSocket = serverSocket;
		clients = new  ConcurrentHashMap<String, Client>();
	    messages = new ArrayList<String>();
		running = true;
	}
	
	//While thread is running it get messages from the queue and sends them to ClientSender thread
	@Override
	public void run() {
		while (running) {
			String message;
			message = getNextMessage();
			sendMessage(message);									
		}
	}
	
	public boolean isRunning() {
		return running; 
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}	
	protected ServerSocket getServerSocket() {
		return serverSocket;
	}
	
	protected Map<String, Client> getClients() {
		return clients;
	}
	
	protected Client getClientByName(String name) {
		return clients.get(name);
	}
	
	public ChatLogger getServerLoger() {
		return SERVER_LOGER;
	}
	
	//Adds client in client's map, if there isn't already client with this nickname
	protected void addClient(Client client) {
		if(!clients.containsKey(client.getName())) {
			clients.put(client.getName(), client);
		}
	}
	
	//Removes client from client's map and close the socket
	protected void removeClient(String name) {
		try {
			Client client = clients.get(name);
			if (client != null) {
				client.getReciever().closeResources();
				client.getSender().closeResources();
				client.getSocket().close();
			}
			//clients.get(name).getSocket().close();
		} catch (Exception e) {
			SERVER_LOGER.log(Severity.FATAL, "server", "Error while creating client: " + Printer.getStackTrace(e));
		}		
		clients.remove(name);
	}
	
	//Adds message to Dispatcher queue in order to be broadcasted or sent to a particular client
	protected synchronized void addMessageToQueue(Client client, String message) {
		if(! (messages.size() == 0)) {
			try {
				wait();
			} catch (InterruptedException interruptedEx) {
				SERVER_LOGER.log(Severity.FATAL, "server", "An error occured while adding message: " + Printer.getStackTrace(interruptedEx));
			}
		}
		//Adds client's name and message
		messages.add("<" + client.getName() + "> " + message);
		notify();
	}
	
	//Stops server by disconnecting all clients one by one and setting running to false
	 protected synchronized void stopServer() {
			notifyAll();
			try {
				for (String name : clients.keySet()) {
					clients.get(name).getSender().addMessageToClientsQueue(Constants.QUIT_COMMAND);
					removeClient(name);			
				}
				serverSocket.close();
			} catch (IOException e) {
				running = false;
			}
	}
	private synchronized String getNextMessage() {
		String result = null;
		//Waits if message queue is empty and if server is still running, otherwise gets first message from queue and removes it
		if(messages.size() == 0 && running) {
			try {
				wait();
			} catch (InterruptedException interruptedEx) {
				SERVER_LOGER.log(Severity.FATAL, "server", "Error while getting next message from queue: " + Printer.getStackTrace(interruptedEx));
				this.setRunning(false);
			}
		} else {
			result = messages.remove(0);	
		}
		return result;
	}
	
	// Sends back message to clientSender if there aren't any connected users or
	// invokes sendMessageToAll if it is broadcast message or
	// invokes sendMessageToOneUser
	
	 private synchronized void sendMessage(String message){
		if (message != null) {
			String senderName = message.substring(message.indexOf("<")+ 1,message.indexOf(">"));
			if(clients.size() == 1) {
				clients.get(senderName).getSender().addMessageToClientsQueue("There aren't any connected users at " + LocalDateTime.now());
				return;
			}			
			if (message.contains(Constants.SENDTO_COMMAND)) {
				sendMessageToOneUser(message);
				return;
			}
			sendMessageToAll(message);
							
		}	
	}
		
	//Sends message to all connected clients from client's map except sender
	private synchronized void sendMessageToAll(String message) {
		//Gets sender name from message
		String senderName = message.substring(message.indexOf("<")+ 1,message.indexOf(">"));
		//Counts to how many cliets the message is sent successfully
		int sentTo = 0;
		for (String name : clients.keySet()) {
			if(!name.equals(senderName)){
				clients.get(name).getSender().addMessageToClientsQueue(message);	
				sentTo++;
			}					
		}
		//Information message to how many clients the message is successfully sent
		clients.get(senderName).getSender().addMessageToClientsQueue("Message sent to " + sentTo+ "/" + (clients.size() - 1) + " clients!");	
	}
	
	//Sends message only to one client
	private synchronized void sendMessageToOneUser(String message) {
				//Gets sender name
				String senderName = message.substring(message.indexOf("<")+ 1,message.indexOf(">"));
				//Gets reciever name in order to check if he is connected to server
				String[] strings = message.split(Constants.SENDTO_COMMAND);
				if(!clients.containsKey(strings[1])) {
					clients.get(senderName).getSender().addMessageToClientsQueue("There isn't any user with nickname: " + strings[1] + "!");
				} else {
					clients.get(strings[1]).getSender().addMessageToClientsQueue(strings[0]);
					clients.get(senderName).getSender().addMessageToClientsQueue("Message sent to: " + strings[1]);
				}
	}
}