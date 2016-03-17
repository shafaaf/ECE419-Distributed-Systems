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
    
    public MyServerListenerThread( MSocket mSocket, PriorityBlockingQueue myPriorityQueue, LamportClock myLamportClock, 
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
                
                
                /*if 0 an event, dont ack myself and put in my priority queue where acks 
            	for others will be sent, and i will ack MYSELF*/
                if(received.category == 0) 
                {
                	System.out.println("MyServerListenerThread: Got an EVENT! with lamport clock " + received.lamportClock);
	                
                	synchronized (myLamportClock)
                	{
		                if(received.lamportClock > myLamportClock.value)  //Updating lamport clock
		                {
		            	    System.out.println("MyServerListenerThread: Updating lamport clock value as new one is HIGHER");
		            	    System.out.println("MyServerListenerThread: My current one is " + myLamportClock.value + 
		            	    		" and new one FROM EVENT received is " + received.lamportClock);
	
		                	int a = (int) Math.floor(received.lamportClock);
		                	Double localLamportClock = new Double(a + "." + pid).doubleValue();
		            		myLamportClock.value = (double) localLamportClock;
		            	}
		                
		               else
		               {
		            	   System.out.println("MyServerListenerThread: NOT updating lamport clock value - My Lamport clock value is " + 
		            			   myLamportClock.value + " and one received is " + received.lamportClock);
		               }
                	}
	               
	               System.out.println("MyServerListenerThread: My Lamport clock value FINALLY is " + myLamportClock.value +
	            		   " and received one has lamport clock " + received.lamportClock);

	              
	               //Ack myself as I read an event from the socket. And event from someone else
	               /*
	               System.out.println("MyServerListenerThread: Now ack myself as got event with lamport clock " + received.lamportClock +
	            		   " from socket and therefore from someone else.");

	        		synchronized (lamportAcks)
	        		{
	                	if(lamportAcks.get(received.lamportClock) == null)
	                	{
	                		lamportAcks.put(new Double(received.lamportClock), new Integer(1));
	                		System.out.println("MyServerListenerThread: FIRST Ack for " + received.lamportClock 
	                			+  " so total number of acks is now " + (double)lamportAcks.get(received.lamportClock));
	                	}
	                	
	                	else
	                	{	
	                		lamportAcks.put(received.lamportClock, (lamportAcks.get(received.lamportClock)) + 1);
	                		System.out.println("MyServerListenerThread: ANOTHER Ack for " + received.lamportClock 
	                    			+  " so total number for acks is now " + (double)lamportAcks.get(received.lamportClock));
	                	}
		        	}
	        		*/
	        		
	        		 //Put only events in priority queue
		             System.out.println("MyServerListenerThread: Putting EVENT from someone else with lamport clock " + 
		            		 received.lamportClock + " in myPriorityQueue");
		             received.acks_sent = 0;
	        		 myPriorityQueue.put(received);
	           
                }	//end bracket of if for received.category = 0
                
                
                else if(received.category == 1)	//its an ACK from someone, JUST update hashmap
                {
                	System.out.println("MyServerListenerThread: Got an Ack from socket! Its lamport clock value is " + 
                			received.lamportClock);
                	
                	System.out.println("MyServerListenerThread: Therefore update number of acks received in "
                			+ "hashmap for event with lamport clock " + received.lamportClock);
                	synchronized (lamportAcks)
                	{
	                	if(lamportAcks.get(received.lamportClock) == null)
	                	{
	                		lamportAcks.put(new Double(received.lamportClock), new Integer(1));
	                		System.out.println("MyServerListenerThread: FIRST Ack for " + received.lamportClock 
	                			+  " so total number of acks is " + lamportAcks.get(received.lamportClock));
	                	}
	                	
	                	else
	                	{	
	                		lamportAcks.put((Double)received.lamportClock, (lamportAcks.get(received.lamportClock)) + 1);
	                		System.out.println("MyServerListenerThread: ANOTHER Ack for " + received.lamportClock 
	                    			+  " so total number for acks is now " + lamportAcks.get(received.lamportClock));
	                	}
                	}         	
                }
                else{
                	System.out.println("MyServerListenerThread: weird! shouldnt come here");
                }
                
                  
            } //end backet for try
            
            catch(IOException e){
                e.printStackTrace();
            }catch(ClassNotFoundException e){
                e.printStackTrace();
            }
            
        }
    }
    
}


