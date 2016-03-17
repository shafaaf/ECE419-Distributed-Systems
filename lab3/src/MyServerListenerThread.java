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
                
                if(received.category == 0) //if 0 its an event from someone, so put in execution queue
                {
                	System.out.println("MyServerListenerThread: Got an EVENT from the socket! Its from " + received.owner);
	                
                	//As guy who sent event has it, so ack him as he WONT send the ack if hes owner
                	System.out.println("MyServerListenerThread: Event from client " + received.owner +
                			" , so he has to have ack, so increment acks received");
                	
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

	               //HACK - ack myself - DONT as waiting for n-1 acks
	               /*
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
	               */
	        		
	        		
	        		//Put ONLY events in priority queue
		            System.out.println("MyServerListenerThread: Putting EVENT from client " +  received.owner + 
		            		" with lamport clock " + received.lamportClock + " in myPriorityQueue");
		            myPriorityQueue.put(received);
	               
                }	//end of if its an event
                
                
                else if (received.category == 1) //its an ACK from the socket, DONT put in any queue and just update hashmap
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
            
                
                /* Need to constantly do this. ONLY EVENTS in myProrityQueue. So now check if acks sent 
                	and if Im owner, THEN ONLY put ack in the event queue to be broadcasted
                */
                
               headOfPriorityQueue = myPriorityQueue.peek();
               if(headOfPriorityQueue!= null)
	        	{
	        		System.out.println("MyServerListenerThread: Peeked event has lamport clock " +
	        			headOfPriorityQueue.lamportClock + " and acks_sent as " +
	        				headOfPriorityQueue.acks_sent);
	        		
	        		if(headOfPriorityQueue.owner == pid)
	        		{
	        			System.out.println("MyServerListenerThread: Im owner of peeked event so DONT send acks!");
	        		}
	        		
		        	if((headOfPriorityQueue.acks_sent == 0) && (headOfPriorityQueue.owner!= pid))
		        	{
		        		System.out.println("MyServerListenerThread: Acks NOT sent for this this and Im NOT the owner "
		        				+ "of this event, so MAKE new ACK packet to send to everyone else");
		        		
		        		synchronized(myLamportClock) 
		        		{
		        			packetAck = new MPacket(1, headOfPriorityQueue.lamportClock);
		        		}
		        		
		        		//Dont ack myself as waiting for n-1 acks and them from everyone else
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
			        	}
			        	*/
		        		
	                	System.out.println("MyServerListenerThread: Putting newly made ACK with lamport clock " +
	                			packetAck.lamportClock + " in event queue");
	                	try {
							eventQueue.put(packetAck);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                	headOfPriorityQueue.acks_sent = 1;
	                	System.out.println("MyServerListenerThread: Event with lamport clock " + 
	                			headOfPriorityQueue.lamportClock + " now has acks sent to " + 
	                				headOfPriorityQueue.acks_sent);
	                }
		        	
		        	else if((headOfPriorityQueue.acks_sent == 0) && (headOfPriorityQueue.owner== pid))
		        	{
		        		System.out.println("MyServerListenerThread: All good. I DIDNT send acks as Im OWNER of event with lamportclock " + 
		        				headOfPriorityQueue.lamportClock);
		        	}
		        	else if((headOfPriorityQueue.acks_sent == 1) && (headOfPriorityQueue.owner != pid))
		        	{
		        		System.out.println("MyServerListenerThread: All good. Already sent acks and Im not owner.");
		        	}	
		        	else if((headOfPriorityQueue.acks_sent == 1) && (headOfPriorityQueue.owner == pid))
		        	{
		        		System.out.println("MyServerListenerThread: Weird! Shouldnt happen with acks.");
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


