import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

//made in MyServerthread.java


public class MyServerSenderThread implements Runnable {

    //private ObjectOutputStream[] outputStreamList = null;
    private BlockingQueue eventQueue = null;
    public LamportClock myLamportClock = null;
    public PriorityBlockingQueue myPriorityQueue;
    public int pid;
    public ArrayList<MSocket> socketList;
    public HashMap<Double, Integer> lamportAcks;
    
    
    public MyServerSenderThread(BlockingQueue eventQueue, 
    		LamportClock myLamportClock, PriorityBlockingQueue myPriorityQueue, 
    		int pid, ArrayList<MSocket> socketList, HashMap<Double, Integer> lamportAcks)
    {
		this.eventQueue = eventQueue;
		this.myLamportClock =  myLamportClock;
		this.myPriorityQueue = myPriorityQueue;		
		this.pid = pid;
		this.socketList = socketList;
		this.lamportAcks = lamportAcks;
    }

    public void run() {
        MPacket toBroadcast = null;
        MPacket packetAck = null;
        MPacket toMe = null;
        int i = 0;
        int j = 0;
        MPacket headOfPriorityQueue = null;
        
        while(true){
            try{
            		System.out.println("MyServerSenderThread: Going to take from event queue");
	            	toBroadcast = (MPacket)eventQueue.take();
	            	
	            	if (toBroadcast.category == 0)	//Its an event
		            {
		                System.out.println("MyServerSenderThread: Taken an EVENT from eventqueue.");
		                System.out.println("MyServerSenderThread: Will now increment lamport clock on send."
		                		+ " Current Lamport clock before is " + myLamportClock.value);
		                
		                //Increment on send
		                synchronized(myLamportClock) 
		                {
		                	myLamportClock.value = myLamportClock.value + 1;
		                	toBroadcast.lamportClock = myLamportClock.value;
		                }
		                
		                System.out.println("MyServerSenderThread: New lamport clock is " + 
		                		myLamportClock.value + " and event has Lamport clock " 
		                			+ toBroadcast.lamportClock);
		                
		                toBroadcast.owner = pid;
		                toBroadcast.acks_sent = 0;
		                
		                /*Setup My packet which I will put in my queue*/
		                //from gui client - eventQueue.put(new MPacket(getName(), MPacket.ACTION, MPacket.DOWN));
		                //MPacket constructor - public MPacket(String name, int type, int event){
		                /*toMe = new MPacket(toBroadcast.name, toBroadcast.type, toBroadcast.event);
		                toMe.lamportClock = toBroadcast.lamportClock;
		                toMe.category = toBroadcast.category;
		                toMe.acks_sent = toBroadcast.acks_sent;
		                */
		                
		               System.out.println("MyServerSenderThread: Putting EVENT in MY QUEUE with Lamport clock: " + 
		                		toBroadcast.lamportClock + " with owner me as client" + toBroadcast.owner);
		                
		                //Putting in MY execution queue first before broadcasting to others
		                //Unsure here whether need to make a new type of packet
		                myPriorityQueue.put(toBroadcast);
		                
		                System.out.println("MyServerSenderThread: Broadcasting that EVENT to all clients except me with Lamport clock: " 
	                    		+ toBroadcast.lamportClock + " AFTER putting in my queue");
		                
		                /* Dont act myself as waiting for n-1 acks
		                synchronized (lamportAcks)
		        		{
		                	if(lamportAcks.get(toBroadcast.lamportClock) == null)
		                	{
		                		lamportAcks.put(new Double(toBroadcast.lamportClock), new Integer(1));
		                		System.out.println("ClientQueueExecutionThread: FIRST Ack for " + toBroadcast.lamportClock 
		                			+  " so total number of acks is " + (double)lamportAcks.get(toBroadcast.lamportClock));
		                	}
		                	
		                	else
		                	{	//else increment acks for that entry by 1
		                		lamportAcks.put(toBroadcast.lamportClock, (lamportAcks.get(toBroadcast.lamportClock)) + 1);
		                		System.out.println("ClientQueueExecutionThread: ANOTHER Ack for " + toBroadcast.lamportClock 
		                    			+  " so total number for acks is now " + (double)lamportAcks.get(toBroadcast.lamportClock));
		                	}
			        	}
		                */
		                
	            	}
	            	
	            	/*	Its a ack.These are put in event queue by me when I try to send acks for events when
	            	 	when Im not the owner and acks sent is 0.
	            	*/	
	            	else if(toBroadcast.category == 1)
	            	{
	            		//Unsure here. REMEMBER to include this if needed
	            		//toBroadcast.acks_sent = 0;
	            		
	            		System.out.println("MyServerSenderThread: Broadcasting ACK to all clients except me with Lamport clock: " 
	                    		+ toBroadcast.lamportClock);
	            		
	            		//the ack should have a lamport clock value from before
	            		//toBroadcast.lamportClock = myLamportClock.value;
	            	}
	            	
	            	else
	            	{
	            		System.out.println("MyServerSenderThread: WEIRD!! Should not come here!");
	            	}
	                
	                //Send to all clients except me
	            	i = 0;
	            	for(MSocket mSocket: socketList)
	                {
	            		System.out.println("MyServerSenderThread: Actual writing the event or ack with category " + 
	                			toBroadcast.category + " with lamport clock " + toBroadcast.lamportClock + 
	                				" to socket " + i);
	                	mSocket.writeObject(toBroadcast);
	                	i++;
	                }
                
            }catch(InterruptedException e){
                System.out.println("MyServerSenderThread: Throwing Interrupt");
                Thread.currentThread().interrupt();    
            }
            
        }
    }
    
}
