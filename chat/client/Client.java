package chat.client;

import java.io.IOException;
import java.net.Socket;

import chat.utils.ChatLogger;
import chat.utils.Constants;
import chat.utils.Printer;
import chat.utils.Severity;

public class Client {	
	// lifecycle
	private boolean running;
	
	// client-server communication
	private Receiver receiver;
	private Sender sender;
	private Socket socket;	
	
	// logging
	private static final ChatLogger CLIENT_LOGGER = ChatLogger.getInstance(false); 
	private String clientId; 
	
	public Client(Socket socket)  {
		this.socket = socket;
		clientId = socket.getLocalAddress() + ":" + socket.getLocalPort();
		CLIENT_LOGGER.log(Severity.DEBUG, clientId, "initilized");
	}

//_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_Setter Methods_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_
	public Socket getSocket() {
		return socket;
	}
	
	public String getId() {
		return clientId;
	}
	public ChatLogger getClientLogger() {
		return CLIENT_LOGGER;
	}

	public boolean isRunning() {
		return running;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
		if(!running) {
			try {
				receiver.closeRecieverResources();
				sender.closeRecieverResources();
				socket.close();
			} catch (IOException e) {
				CLIENT_LOGGER.log(Severity.ERROR, clientId, "Error while closing the sources" + Printer.getStackTrace(e));
			}

		}
	}

	private void start() throws IOException, InterruptedException {
			running = true;
			//Creates receiver that will process the messages from server 
			receiver = new Receiver(this);
					
			//Reads user input from console and sends the corresponding commadns and messages to server
			sender = new Sender(this);
			
			Thread receiverThread = new Thread(receiver);
			Thread senderThread = new Thread(sender);

			// Receiver and sender wait() until they are both started, after that running is set to true and they are notified
			receiverThread.start();
			synchronized (this) {
				if (!receiver.isStarted()) {
					wait(10000);
					if (!receiver.isStarted()) {
						running = false;
						throw new RuntimeException("Cannot start client for 10 000.");
					}
				}
				
			}
			senderThread.start();
			synchronized (this) {
				if (!sender.isStarted()) {
					wait(10000);
					if (!sender.isStarted()) {
						running = false;
						throw new RuntimeException("Cannot start client for 10 000.");
					}
				}
			}
	}


	public static void main(String[] args) {
		Client client = null;
		
		try {
	        // Try to creates a client
			if (args.length == 2) {
				String serverHost = args[0];
				try {
					int serverPort = Integer.parseInt(args[1]);
					Socket socket = new Socket(serverHost, serverPort);
					client = new Client(socket);
				} catch(NumberFormatException nfEx) {
					CLIENT_LOGGER.log(Severity.FATAL, "client trying to connect on port" + args[0], Printer.getStackTrace(nfEx));
					throw new NumberFormatException("Incorrect port. DEFAULT_HOST="+ Constants.DEFAULT_HOST + "and" + "DEFAULT_PORT=" + Constants.DEFAULT_PORT);
				}
			}
			if(args.length == 0){
				Socket socket = new Socket(Constants.DEFAULT_HOST, Constants.DEFAULT_PORT);
				client = new Client(socket);
			} else {
				throw new IllegalArgumentException("Two many arguments exception." + Constants.DEFAULT_HOST + "and" + "DEFAULT_PORT=" + Constants.DEFAULT_PORT);
			}
			client.start();
		}  catch(Throwable th) {
			CLIENT_LOGGER.log(Severity.FATAL, "", "Error while creating client: " + Printer.getStackTrace(th));
			if (th instanceof Error) {
				
			    if (th instanceof OutOfMemoryError) {
			    	throw (OutOfMemoryError)th;
			    }
				if (th instanceof ThreadDeath) {
					throw (ThreadDeath)th;
				}
				throw new Error(th);
			} else if(th instanceof RuntimeException) {
				throw new RuntimeException(th);
			} else {
				// This is the case where we have Exception. 
				throw new RuntimeException(th);
			}
		}
	} 
}

