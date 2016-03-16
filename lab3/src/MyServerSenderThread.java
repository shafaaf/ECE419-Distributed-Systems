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
	            	
	            	if (toBroadcast.category == 0)	//its an event
		            {
		                System.out.println("MyServerSenderThread: Taken an EVENT from eventqueue.");
		                
		                System.out.println("MyServerSenderThread: Now incrementing lamport clock on send. "
		                		+ "Current Lamport clock is " + myLamportClock.value);
		                
		                //Increment on send
		                synchronized(myLamportClock) 
		                {
		                	myLamportClock.value = myLamportClock.value + 1;
		                	toBroadcast.lamportClock = myLamportClock.value;
		                }
		                
		                System.out.println("MyServerSenderThread: New lamport clock is" + 
		                		myLamportClock.value + " and event has Lamport clock value " 
		                			+ toBroadcast.lamportClock);
		                
		                
		                //0 to show its an event and NOT an ack
		                //toBroadcast.category = 0;
		                //0 to show that no acks for it have been sent
		                toBroadcast.acks_sent = 0;
		                
		                
		                /*Setup My packet which I will put in my queue*/
		                //from gui client - eventQueue.put(new MPacket(getName(), MPacket.ACTION, MPacket.DOWN));
		                //MPacket constructor - public MPacket(String name, int type, int event){
		                
		                /*toMe = new MPacket(toBroadcast.name, toBroadcast.type, toBroadcast.event);
		                toMe.lamportClock = toBroadcast.lamportClock;
		                toMe.category = toBroadcast.category;
		                toMe.acks_sent = toBroadcast.acks_sent;
		                */
		                
		                /*I add to my queue first and broadcast to everyone except for me*/
		                System.out.println("MyServerSenderThread: Putting EVENT in MY QUEUE with Lamport clock: " + toBroadcast.lamportClock);
		                
		                //Putting in my execution queue
		                //Unsure here whether need to make a new type of packet
		                myPriorityQueue.put(toBroadcast);
		                
		                System.out.println("MyServerSenderThread: Broadcasting EVENT to all clients except me with Lamport clock: " 
	                    		+ toBroadcast.lamportClock + " AFTER putting in my queue");
		                
		                //should ACK MYSELF I think
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
		                
	            	}
	            	
	            	else if(toBroadcast.category == 1)	//its a ack, acks are put in event queue
	            	{
	            		//Dont need as its an ack for me to come here
	            		//toBroadcast.category = 1;
	            		
	            		//unsure here
	            		toBroadcast.acks_sent = 0;
	            		
	            
	            		
	            		System.out.println("MyServerSenderThread: Broadcasting ACK to all clients except me with Lamport clock: " 
	                    		+ toBroadcast.lamportClock);
	            		
	            		//the ack should have a lamport clock value from before
	            		//toBroadcast.lamportClock = myLamportClock.value;
	            		
	                }
	            	
	            	else
	            	{
	            		System.out.println("MyServerSenderThread: WEIRD!! Should not come here!");
	            	}
	                
	                //Delay
	                /*
	                try {
	                    Thread.sleep(400);                 //1000 milliseconds is one second.
	                } catch(InterruptedException ex) {
	                    Thread.currentThread().interrupt();
	                }*/
	                
	            	
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
