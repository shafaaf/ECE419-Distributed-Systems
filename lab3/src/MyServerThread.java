import java.io.IOException;

public class MyServerThread implements Runnable{
	
	private MServerSocket myServerSocket;
	private int portNumber;
	private int maxClients;
	private int clientCount;
	private MSocket[] mSocketList;
	
	MyServerThread(MServerSocket mServerSocket, int portNumber, int maxClients, int clientCount, MSocket[] mSocketList) {
		this.myServerSocket = mServerSocket;
		this.portNumber = portNumber;
		this.maxClients = maxClients;
		this.clientCount = clientCount;
		this.mSocketList = mSocketList;
		
	}
	
	public void run() {
		while(clientCount < maxClients){
            //Start a new handler thread for each new client connection
            try {
            	System.out.println("MyServerThread: Waiting for connection.");
            	System.out.println("MyServerThread: clientCount is " + clientCount);
				MSocket mSocket = myServerSocket.accept();
				//thread
				System.out.println("MyServerThread: Got a connection here");
				
				mSocketList[clientCount] = mSocket;
				clientCount++;
				System.out.println("MyServerThread: clientCount after connection is " + clientCount);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            //new Thread(new ServerListenerThread(mSocket, eventQueue)).start();
            
            //mSocketList[clientCount] = mSocket;                            
            
            //clientCount++;
        }
	
	}

}
