import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class MyServerThread implements Runnable{
	
	private MServerSocket mServerSocket;
	private int portNumber;
	private int maxClients;
	private int clientCount;
	private MSocket[] mSocketList;
	private BlockingQueue eventQueue;
	
	MyServerThread(MServerSocket mServerSocket, int portNumber, int maxClients, int clientCount, MSocket[] mSocketList, BlockingQueue eventQueue) {
		this.mServerSocket = mServerSocket;
		this.portNumber = portNumber;
		this.maxClients = maxClients;
		this.clientCount = clientCount;
		this.mSocketList = mSocketList;
		this.eventQueue = eventQueue;
		
	}
	
	public void run() {
		while(clientCount < maxClients){
           
            try {
            	System.out.println("MyServerThread: clientCount before connection is " + clientCount);
            	System.out.println("MyServerThread: Waiting for connection.");
				MSocket mSocket = mServerSocket.accept();
				
				System.out.println("MyServerThread: Got a connection here");
				
				//Start a new handler thread for each new client connection for each Mazewar client
				new Thread(new MyServerListenerThread(mSocket,eventQueue)).start();
				mSocketList[clientCount] = mSocket;
				clientCount++;
				System.out.println("MyServerThread: clientCount after connection is " + clientCount);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
		}
            //1 Thread to send to all clients for each Mazewar client
            new Thread(new MyServerSenderThread(mSocketList, eventQueue)).start();
            
	}

}
