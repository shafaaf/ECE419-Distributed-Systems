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
//-----------------------------------------------------Ack sending-------------------------------------------------------------
        		
	           headOfPriorityQueue = myPriorityQueue.peek();
	           /*
	           System.out.println("ClientQueueExecutionThread: client with pid " + pid + ", has headOfPriorityQueue as lamport clock " + 
	        		   headOfPriorityQueue.lamportClock + " and number of acks it received is "+
	        		   	lamportAcks.get(headOfPriorityQueue.lamportClock) + " and acks sent is "+
	        		   	headOfPriorityQueue.acks_sent);
	        	*/	   	
	           
	           if(headOfPriorityQueue!= null)
	           {
	        	   
	        	   if(headOfPriorityQueue.acks_sent == 0)
	        	   { 
		        		
	        		   	packetAck = new MPacket(1, headOfPriorityQueue.lamportClock);
		        		System.out.println("ClientQueueExecutionThread: Made ack packet with category " +
		        				packetAck.category + " and lamportClock " + packetAck.lamportClock);
		        		
		        		System.out.println("ClientQueueExecutionThread: Now send out acks also to myself "
		        			+ "for headOfPriorityQueue which has lamport clock " + headOfPriorityQueue.lamportClock);
		        		
		        		//Ack myself
		        		System.out.println("ClientQueueExecutionThread: Acking myself for headOfPriorityQueue with lamport clock " +
		        				headOfPriorityQueue.lamportClock);
		        		
		        		synchronized (lamportAcks)
		        		{
		                	if(lamportAcks.get(packetAck.lamportClock) == null)
		                	{
		                		lamportAcks.put(new Double(packetAck.lamportClock), new Integer(1));
		                		System.out.println("ClientQueueExecutionThread: FIRST Ack from myself for " + packetAck.lamportClock 
		                			+  " so total number of acks is " + (double)lamportAcks.get(packetAck.lamportClock));
		                	}
		                	
		                	else
		                	{	//else increment acks for that entry by 1
		                		lamportAcks.put(packetAck.lamportClock, (lamportAcks.get(packetAck.lamportClock)) + 1);
		                		System.out.println("ClientQueueExecutionThread: ANOTHER Ack for " + packetAck.lamportClock 
		                    			+  " so total number for acks is now " + (double)lamportAcks.get(packetAck.lamportClock));
		                	}
			        	}
		        		
		        		headOfPriorityQueue.acks_sent = 1;
		        		System.out.println("ClientQueueExecutionThread: Finished acking myself. Now put ACK packet with lamport clock " +
		        				packetAck.lamportClock + " in event queue to be sent to everyone else");
		        		
		        		try {
							eventQueue.put(packetAck);
							System.out.println("ClientQueueExecutionThread: Finished putting it in event queue");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	
	                	
	               }
	        	   
	        	   else if(headOfPriorityQueue.acks_sent == 1)
	        	   {
	        		    
	        		   System.out.println("MyServerListenerThread: Acks sent already is 1 for event with lamport clock " + 
	        				   headOfPriorityQueue.lamportClock);
	        	   }
	        	   else
	        	   {
	        		   System.out.println("ClientQueueExecutionThread: Weird shouldnt come here!");
	        	   }
	        	}
	           
 //--------------------------------------------------------------------------------------------------------------

	           //Now do actual execution. Peek queue again for head
	           headOfPriorityQueue = myPriorityQueue.peek();
	           
	           //have at least 1 event
	           if(lamportAcks.get(headOfPriorityQueue.lamportClock) != null)
	        	{
		        	if((lamportAcks.get(headOfPriorityQueue.lamportClock).intValue() == maxClients)	//unsure of intvalue()
	        				&& (headOfPriorityQueue.acks_sent == 1))
	        		{
			        	System.out.println("ClientQueueExecutionThread: CAN execute! -  headOfPriorityQueue has " +
			        			"lamport clock " + headOfPriorityQueue.lamportClock +" and it has " + 
	        						lamportAcks.get(headOfPriorityQueue.lamportClock) + " acks!");
	        			
			        	received = myPriorityQueue.poll();
			        	System.out.println("ClientQueueExecutionThread: received.lamportClock is " + received.lamportClock);
			        	System.out.println("ClientQueueExecutionThread: headOfPriorityQueue.lamportClock is " + headOfPriorityQueue.lamportClock);
			        	
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
			        	else
			        	{	//put it back in as its something else now
			        		System.out.println("ClientQueueExecutionThread: Putting it back as its something else");
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
