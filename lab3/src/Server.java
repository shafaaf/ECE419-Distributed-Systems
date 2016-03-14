import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.*;


public class Server {	//listen and enqueue, dequeue and broadcast.
						//Attachment of global sequence no. in server sender thread
    
	//The maximum of clients that will join
	//Server waits until the max number of clients to join
    private static final int MAX_CLIENTS = 3;	//CHANGE BACK TO 2
    private MServerSocket mServerSocket = null;
    private int clientCount; //The number of clients before game starts
    private MSocket[] mSocketList = null; //A list of MSockets
    private BlockingQueue eventQueue = null; //A list of events
    
    //private Map <List<String>, Integer> clientConnected = null;
    
    //my arraylist having Clientinfo objects which give host, port and pid
    private ArrayList<Clientinfo> clientInfo = null;
    
    /*
    * Constructor
    */
    public Server(int port) throws IOException{
    	//msockets can connect to specified host and port. It has 2 main methods:
    		//1. void writeObject(Object to) 
    		//2. Object readObject()
    	
    	//mServerSocket listens on specific port and has 1 main method:
    		//1. Msocket accept()
    	
        clientCount = 0;
        
        mServerSocket = new MServerSocket(port);
        if(Debug.debug) System.out.println("Listening on port: " + port);
        mSocketList = new MSocket[MAX_CLIENTS];
        eventQueue = new LinkedBlockingQueue<MPacket>();
        clientInfo = new ArrayList<Clientinfo>();
        
    }
    
    
//---------------------------------------------------------------------------------------------------------------------------------------------    
    /*
    *Starts the listener and sender threads 
    */
    public void startThreads() throws IOException{
        //Listen for new clients
        while(clientCount < MAX_CLIENTS){
            //Start a new listener thread for each new client connection
            MSocket mSocket = mServerSocket.accept();
            
            new Thread(new ServerListenerThread(mSocket, eventQueue)).start();
            
            mSocketList[clientCount] = mSocket;                            
            
            clientCount++;
        }
        
        //Start a new sender thread. Just 1 for all clients
        //Sender thread dequeues events, attaches a global sequence number and broadcasts events
        new Thread(new ServerSenderThread(mSocketList, eventQueue, clientInfo)).start();
        
        //Handle dynamic joins
        while(true){
        	MSocket mSocket_dynamic_join = mServerSocket.accept();
            new Thread(new ServerListenerThread(mSocket_dynamic_join, eventQueue)).start();
            mSocketList[clientCount] = mSocket_dynamic_join;                            
            clientCount++;
        }
    }

    
//---------------------------------------------------------------------------------------------------------------------------------------------

    /*
    * Entry point for server
    */
    public static void main(String args[]) throws IOException {
        if(Debug.debug) System.out.println("Starting the server");
        int port = Integer.parseInt(args[0]);
        Server server = new Server(port);	//calls the server constructor
        
        //need this for naming server since ServerSenderThread will write hello packet and  ServerListenerThread will take it
        server.startThreads();    

    }
}
