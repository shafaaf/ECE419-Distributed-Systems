import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class MyServerThread implements Runnable{
	
	private MServerSocket mServerSocket;
	private int portNumber;
	private int maxClients;
	private int clientCount;
	private BlockingQueue eventQueue;
	private PriorityBlockingQueue myPriorityQueue;
	public LamportClock myLamportClock;
	public HashMap<Double, Integer> lamportAcks;
	public int pid;
	public ArrayList<MSocket> socketList;
    
	
	//Constructor
	MyServerThread(MServerSocket mServerSocket, int portNumber, int maxClients, int clientCount, 
			 	BlockingQueue eventQueue, PriorityBlockingQueue myPriorityQueue, LamportClock myLamportClock, 
					HashMap<Double, Integer> lamportAcks, int pid, ArrayList<MSocket> socketList) 
	{
		this.mServerSocket = mServerSocket;
		this.portNumber = portNumber;
		this.maxClients = maxClients;
		this.clientCount = clientCount;
		this.eventQueue = eventQueue;
		this.myPriorityQueue = myPriorityQueue;
		this.myLamportClock = myLamportClock;
		this.lamportAcks = lamportAcks;
		this.pid = pid;
		this.socketList = socketList;
	}
	
	
	public void run() {
		while(true){
           try {
        	   if(Debug.debug) System.out.println("MyServerThread: Waiting for connection.");
				MSocket mSocket = mServerSocket.accept();
				socketList.add(mSocket);
				if(Debug.debug) System.out.println("MyServerThread: Got a connection here");
				
				//Start a new handler thread for each new Mazewar client connection
				new Thread(new MyServerListenerThread(mSocket, myPriorityQueue, myLamportClock, lamportAcks, pid, eventQueue)).start();
				if(Debug.debug) System.out.println("MyServerThread: Made listener for  a client");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
		}
            
	}

}
