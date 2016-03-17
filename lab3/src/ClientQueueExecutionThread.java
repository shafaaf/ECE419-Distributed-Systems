import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class ClientQueueExecutionThread implements Runnable {

    private Hashtable<String, Client> clientTable;
    private PriorityBlockingQueue<MPacket> myPriorityQueue;
    private int clientEventNumber;
    private HashMap<Double, Integer> lamportAcks;
    private int maxClients;
    private int pid;
    public ArrayList<MSocket> socketList;
    public BlockingQueue eventQueue;
    
    public ClientQueueExecutionThread( Hashtable<String, Client> clientTable,
    		PriorityBlockingQueue myPriorityQueue, HashMap<Double, Integer> lamportAcks, int maxClients, 
    			int pid, ArrayList<MSocket> socketList, BlockingQueue eventQueue)
    {
        this.clientTable = clientTable;
        this.myPriorityQueue = myPriorityQueue;
        this.clientEventNumber = 0;
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
        
        int a = 0;
        
        while(true){
        	
        	//if(a < 5)
        	/*
        	{
        		if(!myPriorityQueue.isEmpty())
        		{
        			System.out.println("DEBUG: ClientQueueExecutionThread: " + pid +": priority queue is not empty");
        		}
        		else if(myPriorityQueue.isEmpty())
        		{
        			System.out.println("DEBUG: ClientQueueExecutionThread: " + pid +": priority queue IS empty");
        		}
        		a++;
        	}
        	*/
        	
        	if(!myPriorityQueue.isEmpty())
        	{
        		headOfPriorityQueue = (MPacket)myPriorityQueue.peek();
        		
        		
	        	System.out.println("ClientQueueExecutionThread: headOfPriorityQueue has lamport clock " + 
	        			headOfPriorityQueue.lamportClock + " and number of acks received for it is " +
	        				lamportAcks.get(headOfPriorityQueue.lamportClock));
        		/*
	        	System.out.println("QueueExecutionThread: PID "+ pid + " has head queue event with " + headOfPriorityQueue.lamportClock +
	        			" and its category is " + headOfPriorityQueue.category + ", acks_sent is  " + headOfPriorityQueue.acks_sent +
	        			", its owner is " + headOfPriorityQueue.owner + ", number of acks received for it is " + 
	        				lamportAcks.get(headOfPriorityQueue.lamportClock).intValue());
	        	*/
	        	
	        	//Makes sure at least has 1 ack for that event
            	if(lamportAcks.get(headOfPriorityQueue.lamportClock) != null)
	        	{
		        	//If Im owner, check if didnt send acks and received n-1 acks from other people
            		if((headOfPriorityQueue.owner == pid) && (headOfPriorityQueue.acks_sent == 0) && 
		        			(lamportAcks.get(headOfPriorityQueue.lamportClock).intValue() == maxClients-1)) 
            		{
            			//ALL OK. EXECUTE EVENT
            			System.out.println("QueueExecutionThread: Im owner! CAN execute! - headOfPriorityQueue has "
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
            		
            		
            		//Im not owner, so check if sent acks and received all acks
            		else if ((headOfPriorityQueue.owner != pid) && (headOfPriorityQueue.acks_sent == 1) && 
            			(lamportAcks.get(headOfPriorityQueue.lamportClock).intValue() == maxClients-1))
	            		
            		{	//ALL OK. EXECUTE EVENT
            			System.out.println("QueueExecutionThread: Im NOT owner! CAN execute! -  headOfPriorityQueue has "
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
            	
        	} // if(!myPriorityQueue.isEmpty()) end bracket
        	
        	else
        	{
        		//System.out.println("QueueExecutionThread: Empty queue");
        	}
                
        	
        }
    }
}
