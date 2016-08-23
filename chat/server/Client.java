package chat.server;

import java.net.Socket;
import java.net.SocketAddress;

class Client {
	private Socket socket;
	private String name;
	private ClientReceiver receiver;
	private ClientSender sender;
	
	public Client(Socket socket) {
		this.socket = socket;
		name = "" + socket.getPort();
	}
	
	public String getName() {
		return name;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public SocketAddress getIP() {
		return socket.getLocalSocketAddress();
	}
	
	public ClientReceiver getReciever() {
		return receiver;
	}
	
	public ClientSender getSender() {
		return sender;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setReciever(ClientReceiver receiver) {
		this.receiver = receiver; 
	}
	
	public void setSender(ClientSender sender) {
		this.sender = sender; 
	}
	
	@Override
	public String toString() {
		return name;
	}
}