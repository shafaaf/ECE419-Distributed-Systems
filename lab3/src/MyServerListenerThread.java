//Listens for actions from specific client (depending on MSocket) in P2P
//Made in MyServerthread.java
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.lang.*;


public class MyServerListenerThread implements Runnable{
	private MSocket mSocket;
    //private BlockingQueue eventQueue;
    private PriorityBlockingQueue <MPacket>myPriorityQueue;
    public LamportClock myLamportClock;
    public HashMap<Double, Integer> lamportAcks;
    public int pid;
    public BlockingQueue eventQueue;
    
    public MyServerListenerThread(MSocket mSocket, PriorityBlockingQueue myPriorityQueue, LamportClock myLamportClock, 
    		HashMap<Double, Integer> lamportAcks, int pid,  BlockingQueue eventQueue){
        this.mSocket = mSocket;
        this.myPriorityQueue = myPriorityQueue;
        this.myLamportClock = myLamportClock;
        this.lamportAcks = lamportAcks;
        this.pid = pid;
        this.eventQueue =  eventQueue;
    }
    
    public void run() {	//read, process and enqueue packet
        MPacket received = null;
        MPacket  packetAck = null;
        MPacket headOfPriorityQueue = null;
        
        if(Debug.debug) System.out.println("MyServerListenerThread: Starting a listener");
        while(true){
            try{
            	System.out.println("MyServerListenerThread: Going to read from socket");
                received = (MPacket) mSocket.readObject();
                if(Debug.debug) System.out.println("MyServerListenerThread: Read: " + received);
                
                if(received.category == 0) //if 0 its an event, so put in execution queue
                {
                	System.out.println("MyServerListenerThread: Got an EVENT from the socket!");
	                
                	//As guy who sent event has ack
                	synchronized (myLamportClock)
                	{
		                if(received.lamportClock > myLamportClock.value)  //Updating Lamport clock
		                {
		            	    System.out.println("MyServerListenerThread: Updating lamport clock value as new one is HIGHER");
		            	    System.out.println("MyServerListenerThread: Current lamport clock is " + myLamportClock.value + 
		            	    		" and new one FROM EVENT received is " + received.lamportClock);
	
		                	int a = (int) Math.round(received.lamportClock);
		                	Double localLamportClock = new Double(a + "." + pid).doubleValue();
		            		myLamportClock.value = (double) localLamportClock;
		            	}
		                
		               else
		               {
		            	   System.out.println("MyServerListenerThread: NOT updating lamport clock value - My Local Lamport clock value is " + 
		            			   myLamportClock.value + " and one received is " + received.lamportClock);
		               }
                	}
                	
	               System.out.println("MyServerListenerThread: My latest Lamport clock value is now " + myLamportClock.value);

	               //HACK - ack myself
	               
	               System.out.println("MyServerListenerThread: If event from someone, he has to have ack, so increment acks received");
	               
	        		synchronized (lamportAcks)
	        		{
	                	if(lamportAcks.get(received.lamportClock) == null)
	                	{
	                		lamportAcks.put(new Double(received.lamportClock), new Integer(1));
	                		System.out.println("MyServerListenerThread: FIRST Ack for " + received.lamportClock 
	                			+  " so total number of acks is " + (double)lamportAcks.get(received.lamportClock));
	                	}
	                	
	                	else
	                	{	//else increment acks for that entry by 1
	                		lamportAcks.put(received.lamportClock, (lamportAcks.get(received.lamportClock)) + 1);
	                		System.out.println("MyServerListenerThread: ANOTHER Ack for " + received.lamportClock 
	                    			+  " so total number for acks is now " + (double)lamportAcks.get(received.lamportClock));
	                	}
		        	}
	        		
	        		
	        		//Put only events in priority queue
		            System.out.println("MyServerListenerThread: Putting EVENT from someone else with " + received.lamportClock + " in myPriorityQueue");
		            myPriorityQueue.put(received);
	               
                }	//end of if event
                
                else if (received.category == 1) //its an ACK from the socket, dont put in any queue and just update hashmap
                {
                	System.out.println("MyServerListenerThread: Got an Ack from socket! Its lamport clock value is " 
                			+ received.lamportClock);
                	
                	synchronized (lamportAcks)
                	{
	                	//If not present in hashmap, make new entry with acks received as 1
	                	if(lamportAcks.get(received.lamportClock) == null)
	                	{
	                		lamportAcks.put(new Double(received.lamportClock), new Integer(1));
	                		System.out.println("MyServerListenerThread: FIRST Ack for " + received.lamportClock 
	                			+  " so total number of acks is " + (double)lamportAcks.get(received.lamportClock));
	                	}
	                	
	                	else
	                	{	//else increment acks for that entry by 1
	                		lamportAcks.put((Double)received.lamportClock, (lamportAcks.get(received.lamportClock)) + 1);
	                		System.out.println("MyServerListenerThread: ANOTHER Ack for " + received.lamportClock 
	                    			+  " so total number for acks is now " + (double)lamportAcks.get(received.lamportClock));
	                	}
                	}         	
                }
                
                else
                {
                	System.out.println("MyServerListenerThread: WEIRD!! Should not come here for category!");
                }
            
                
             //Need to constantly do this, only events in myProrityQueue, so now put acks for every event in the event queue
               headOfPriorityQueue = myPriorityQueue.peek();
               if(headOfPriorityQueue!= null)
	        	{
	        		//Still writing this
	        		System.out.println("MyServerListenerThread: Peeked event with lamport clock " +
	        			headOfPriorityQueue.lamportClock + " has acks_sent " +  
	        				headOfPriorityQueue.acks_sent);
	        		
		        	if(headOfPriorityQueue.acks_sent == 0)
		        	{
		        		System.out.println("MyServerListenerThread: Acks NOT sent for this this, so create new packet");
		        		synchronized(myLamportClock) 
		        		{
		        			packetAck = new MPacket(1, headOfPriorityQueue.lamportClock);
		        		}
		        		//If not present in hashmap, make new entry with acks received as 1
		        		/*synchronized (lamportAcks)
		        		{
		                	if(lamportAcks.get(packetAck.lamportClock) == null)
		                	{
		                		lamportAcks.put(new Double(packetAck.lamportClock), new Integer(1));
		                		System.out.println("ClientQueueExecutionThread: FIRST Ack for " + packetAck.lamportClock 
		                			+  " so total number of acks is " + (double)lamportAcks.get(packetAck.lamportClock));
		                	}
		                	
		                	else
		                	{	//else increment acks for that entry by 1
		                		lamportAcks.put(packetAck.lamportClock, (lamportAcks.get(packetAck.lamportClock)) + 1);
		                		System.out.println("ClientQueueExecutionThread: ANOTHER Ack for " + packetAck.lamportClock 
		                    			+  " so total number for acks is now " + (double)lamportAcks.get(packetAck.lamportClock));
		                	}
			        	}*/
		        		
	                	System.out.println("MyServerListenerThread: Putting newly made ACK with lamport clock " +
	    	                	headOfPriorityQueue.lamportClock + " in event queue");
	                	try {
							eventQueue.put(packetAck);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                	//System.out.println("MyServerListenerThread: Setting acks sent for event with lamport "
	                		//	+ "clock " + headOfPriorityQueue.lamportClock + "");
	                	
	                	headOfPriorityQueue.acks_sent = 1;
		        	}
		        	
		        	else if(headOfPriorityQueue.acks_sent == 1)
		        	{
		        		System.out.println("MyServerListenerThread: Already sent acks SENT for " + 
		        				headOfPriorityQueue.lamportClock);
		        	}
		        	else
		        	{
		        		System.out.println("MyServerListenerThread: WEIRD! Should not come here for acks!!");
		        	}	
	        	}
                
                
            }
            
            catch(IOException e){
                e.printStackTrace();
            }catch(ClassNotFoundException e){
                e.printStackTrace();
            }
            
        }
    }
    
}


