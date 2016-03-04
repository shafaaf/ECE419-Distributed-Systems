import java.io.IOException;

public class MyServerThread implements Runnable{
	
	private MServerSocket myServerSocket;
	private int portNumber;
	private int maxClients;
	private int clientCount;
	private MSocket[] mSocketList;
	
	MyServerThread(MServerSocket mServerSocket, int portNumber, int maxClients, MSocket[] mSocketList) {
		this.myServerSocket = mServerSocket;
		this.portNumber = portNumber;
		this.maxClients = maxClients;
		this.clientCount = 0;
		this.mSocketList = mSocketList;
		
	}
	
	public void run() {
		while(clientCount < maxClients){
            //Start a new handler thread for each new client connection
            try {
				MSocket mSocket = myServerSocket.accept();
				//thread
				
				mSocketList[clientCount] = mSocket;
				clientCount++;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            //new Thread(new ServerListenerThread(mSocket, eventQueue)).start();
            
            //mSocketList[clientCount] = mSocket;                            
            
            clientCount++;
        }
	
	}

}
