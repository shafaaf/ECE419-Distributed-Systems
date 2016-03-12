import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class ClientQueueExecutionThread implements Runnable {

    private MSocket mSocket  =  null;
    private Hashtable<String, Client> clientTable = null;
    //my priority queue
    private PriorityBlockingQueue<MPacket> myPriorityQueue = null;
    private int clientEventNumber;
    private MSocket[] client_mSocket;
    
    public ClientQueueExecutionThread( MSocket mSocket,
        Hashtable<String, Client> clientTable,PriorityBlockingQueue myPriorityQueue, MSocket[] client_mSocket)
    {
        this.mSocket = mSocket;
        this.clientTable = clientTable;
        this.myPriorityQueue = myPriorityQueue;
        this.clientEventNumber = 0;
        this.client_mSocket = client_mSocket;
        
        if(Debug.debug) System.out.println("ClientQueueExecutionThread: Instatiating QueueExecutionThread");
    }

    public void run() {
        MPacket received = null;
        MPacket headOfPriorityQueue = null;
        MPacket toBroadcast = null;
        Client client = null;
        if(Debug.debug) System.out.println("ClientQueueExecutionThread: Starting Instatiating QueueExecutionThread");
        
        while(true){
        	//System.out.println("ClientQueueExecutionThread: Checking if queue is empty or not");
        	if(!myPriorityQueue.isEmpty())
        	{
        		//MyPriorityQueue will only have EVENTS, not acks!
                
                //Send acks for only head of queue, which may be changing
	        	headOfPriorityQueue = (MPacket)myPriorityQueue.peek();
	        	if(headOfPriorityQueue.acks_sent == 0){
	        		//Send it to all clients
	        		for(MSocket mSocket: client_mSocket){
	                	System.out.println("ClientQueueExecutionThread: Writing ACKS to sockets");
	                	toBroadcast = new MPacket(1, headOfPriorityQueue.lamportClock);
	                    mSocket.writeObject(toBroadcast);
	                    headOfPriorityQueue.acks_sent = 1;
	                }
	        	}
	        	
	        	
	        	//later need check to see if have all acks
	        	System.out.println("QueueExecutionThread  myProrityQueue's event dequing is " + myPriorityQueue.peek() + "\n");
	        	received = myPriorityQueue.poll();
	        	client = clientTable.get(received.name);
	        	
	        	//Debugging
	        	//System.out.println("QueueExecutionThread: client is: " + received.name);
	        	
	           	//execute client actions. Here the client class refers to the client who actually caused the movement, fire or projectile movement
	            if(received.event == MPacket.UP){
	                client.forward();
	            }else if(received.event == MPacket.DOWN){
	                client.backup();
	            }else if(received.event == MPacket.LEFT){
	                client.turnLeft();
	            }else if(received.event == MPacket.RIGHT){
	                client.turnRight();
	            }else if(received.event == MPacket.FIRE){
	                client.fire();
	            }else if(received.event == MPacket.PROJECTILE_MOVEMENT){
	                client.myMoveProjectile();
	            }
	            else{
	                throw new UnsupportedOperationException();
	            }
	          //then wait for next packet
	            clientEventNumber++;
                
        	} 
                
        	
        }
    }
}
