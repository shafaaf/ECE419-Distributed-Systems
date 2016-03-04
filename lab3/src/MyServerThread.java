import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class MyServerThread implements Runnable{
	
	private MServerSocket myServerSocket;
	private int portNumber;
	private int maxClients;
	private int clientCount;
	private MSocket[] mSocketList;
	private BlockingQueue eventQueue;
	
	MyServerThread(MServerSocket mServerSocket, int portNumber, int maxClients, int clientCount, MSocket[] mSocketList, BlockingQueue eventQueue) {
		this.myServerSocket = mServerSocket;
		this.portNumber = portNumber;
		this.maxClients = maxClients;
		this.clientCount = clientCount;
		this.mSocketList = mSocketList;
		this.eventQueue = eventQueue;
		
	}
	
	public void run() {
		while(clientCount < maxClients){
            //Start a new handler thread for each new client connection
            try {
            	System.out.println("MyServerThread: clientCount before connection is " + clientCount);
            	System.out.println("MyServerThread: Waiting for connection.");
				MSocket mSocket = myServerSocket.accept();
				
				System.out.println("MyServerThread: Got a connection here");
				new Thread(new MyServerListenerThread(mSocket,eventQueue)).start();
				mSocketList[clientCount] = mSocket;
				clientCount++;
				System.out.println("MyServerThread: clientCount after connection is " + clientCount);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
		}
            
            new Thread(new MyServerSenderThread(mSocketList, eventQueue)).start();
            
	}

}
