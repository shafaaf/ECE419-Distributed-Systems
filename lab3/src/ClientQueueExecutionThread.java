import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class ClientQueueExecutionThread implements Runnable {

    private MSocket mSocket;
    private Hashtable<String, Client> clientTable;
    private PriorityBlockingQueue<MPacket> myPriorityQueue;
    private int clientEventNumber;
    private MSocket[] client_mSocket;
    private HashMap<Double, Integer> lamportAcks;
    private int maxClients;
    private int pid;
    public ArrayList<MSocket> socketList;
    public BlockingQueue eventQueue;
    
    public ClientQueueExecutionThread( MSocket mSocket,
        Hashtable<String, Client> clientTable,PriorityBlockingQueue myPriorityQueue, 
        	MSocket[] client_mSocket, HashMap<Double, Integer> lamportAcks, int maxClients, int pid, 
        		ArrayList<MSocket> socketList, BlockingQueue eventQueue)
    {
        this.mSocket = mSocket;
        this.clientTable = clientTable;
        this.myPriorityQueue = myPriorityQueue;
        this.clientEventNumber = 0;
        this.client_mSocket = client_mSocket;
        this.lamportAcks = lamportAcks;
        this.maxClients = maxClients;
        this.pid = pid;
        this.socketList = socketList;
        this.eventQueue = eventQueue;
        
        if(Debug.debug) System.out.println("ClientQueueExecutionThread: Instatiating QueueExecutionThread");
    }

    public void run() {
        MPacket received = null;
        MPacket headOfPriorityQueue = null;
        MPacket headOfPriorityQueue2 = null;
        MPacket toBroadcast = null;
        MPacket packetAck = null;
        Client client = null;
        
        if(Debug.debug) System.out.println("ClientQueueExecutionThread: Starting While loop");
        
        while(true){
        	
        	if(!myPriorityQueue.isEmpty())
        	{
        		//System.out.println("ClientQueueExecutionThread: Its NOT EMPTY");
            	
        		//Send acks for only head of queue, which MAY BE CHANGING
	        	headOfPriorityQueue = (MPacket)myPriorityQueue.peek();
	        	//System.out.println("ClientQueueExecutionThread: headOfPriorityQueue.acks_sent is " + headOfPriorityQueue.acks_sent);
            	
	        	             
	        	
	        	//headOfPriorityQueue2 = myPriorityQueue.peek();
	        	if(lamportAcks.get(headOfPriorityQueue.lamportClock) != null)
	        	{
		        	//Makes sure head has all acks AND has sent out all its acks
	        		//System.out.println("QueueExecutionThread: Acks sent for headOfPriorityQueue2 is " + headOfPriorityQueue2.acks_sent);
				    
	        		if((lamportAcks.get(headOfPriorityQueue.lamportClock).intValue() >= (maxClients - 1)))
	        				//&& (headOfPriorityQueue.acks_sent == 1))
	        		{
			        	System.out.println("QueueExecutionThread: CAN execute! -  headOfPriorityQueue has "
			        			+ "lamport clock " + headOfPriorityQueue.lamportClock +" and it has " + 
	        					lamportAcks.get(headOfPriorityQueue.lamportClock) + " acks!");
	        			
			        	received = myPriorityQueue.poll();
			        	System.out.println("QueueExecutionThread: received.lamportClock is " + received.lamportClock);
			        	System.out.println("QueueExecutionThread: headOfPriorityQueue.lamportClock is " + headOfPriorityQueue.lamportClock);
			        	
			        	if(received.lamportClock == headOfPriorityQueue.lamportClock) //check if same guy I was looking at
			        	{
				        	client = clientTable.get(received.name);
				        	
				        	//Debugging
				        	//System.out.println("QueueExecutionThread: client is: " + received.name);
				        	
				           	//execute client actions. Here the client class refers to the client who actually caused the movement, 
				        	//fire or projectile movement
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
	        			}
			        	else{	//put it back in as its something else now
			        		System.out.println("QueueExecutionThread: Putting it back as its something else");
			        		myPriorityQueue.put(received);
			        	}
	        		}
	        		
	        		else
	        		{
	        			/*System.out.println("QueueExecutionThread: CANT execute for a reason! Real Head "
		        				+ "of queue has lamport clock " + myPriorityQueue.peek().lamportClock + 
		        					" and number of acks peek has is " + lamportAcks.get(myPriorityQueue.peek().lamportClock));
		        					*/
	        			
		        	}
	        		
        		}
        	}
        	else
        	{
        		//System.out.println("QueueExecutionThread: empty queue");
        	}
                
        	
        }
    }
}
