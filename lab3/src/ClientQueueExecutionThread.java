import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.PriorityQueue;
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
    
    public ClientQueueExecutionThread( MSocket mSocket,
        Hashtable<String, Client> clientTable,PriorityBlockingQueue myPriorityQueue, 
        	MSocket[] client_mSocket, HashMap<Double, Integer> lamportAcks, int maxClients, int pid)
    {
        this.mSocket = mSocket;
        this.clientTable = clientTable;
        this.myPriorityQueue = myPriorityQueue;
        this.clientEventNumber = 0;
        this.client_mSocket = client_mSocket;
        this.lamportAcks = lamportAcks;
        this.maxClients = maxClients;
        this.pid = pid;
        
        if(Debug.debug) System.out.println("ClientQueueExecutionThread: Instatiating QueueExecutionThread");
    }

    public void run() {
        MPacket received = null;
        MPacket headOfPriorityQueue = null;
        MPacket headOfPriorityQueue2 = null;
        MPacket toBroadcast = null;
        MPacket packetAck = null;
        Client client = null;
        int i = 0;
        int j = 0;
        if(Debug.debug) System.out.println("ClientQueueExecutionThread: Starting While loop");
        
        while(true){
        	//MyPriorityQueue will only have EVENTS, not acks!
        	if(!myPriorityQueue.isEmpty())
        	{
        		//Send acks for only head of queue, which MAY BE CHANGING
	        	headOfPriorityQueue = (MPacket)myPriorityQueue.peek();
	        	if(headOfPriorityQueue!= null)
	        	{
		        	if(headOfPriorityQueue.acks_sent == 0)
		        	{
		        		//Send it to all clients
		        		/*try {
		        		    Thread.sleep(100);              //1000 milliseconds is one second.
		        		} catch(InterruptedException ex) {
		        		    Thread.currentThread().interrupt();
		        		}*/
		        		
		        		j = 0;
		        		for(MSocket mSocket: client_mSocket)	//send to all clients
		        		{
		                	packetAck = new MPacket(1, headOfPriorityQueue.lamportClock);
		                	System.out.println("ClientQueueExecutionThread: Writing ACK with lamport clock " +
		                			headOfPriorityQueue.lamportClock + " to socket " + j);
		                    mSocket.writeObject(packetAck);
		                    j++;
		                }
		        		headOfPriorityQueue.acks_sent = 1;
		        		j = 0;
		        	}
	        	}
             
	        	
	        	headOfPriorityQueue2 = myPriorityQueue.peek();
	        	if(lamportAcks.get(headOfPriorityQueue2.lamportClock) != null)
	        	{
		        	if((lamportAcks.get(headOfPriorityQueue2.lamportClock) != null))
		        	{	
	        			//Makes sure head has all acks AND has sent out all its acks 
		        		if((lamportAcks.get(headOfPriorityQueue2.lamportClock) == maxClients)
		        				&& (headOfPriorityQueue2.acks_sent == 1))
		        		{
				        	System.out.println("QueueExecutionThread: CAN execute! -  headOfPriorityQueue2 has "
				        			+ "lamport clock " + headOfPriorityQueue2.lamportClock +" and it has " + 
		        					lamportAcks.get(headOfPriorityQueue2.lamportClock) + " acks!");
		        			
				        	received = myPriorityQueue.poll();
				        	if(received.lamportClock == headOfPriorityQueue2.lamportClock)	//same guy I was looking at
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
				        		myPriorityQueue.put(received);
				        	}
		        		}
		        		
		        		else
		        		{
		        			System.out.println("QueueExecutionThread: CANT execute for a reason! Real Head "
			        				+ "of queue has lamport clock " + myPriorityQueue.peek().lamportClock + 
			        					" and number of acks peek has is " + lamportAcks.get(myPriorityQueue.peek().lamportClock));
		        			
			        	}
	        		}
        		}
        	} 
                
        	
        }
    }
}
