import java.util.ArrayList;
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
    
    
    public MyServerSenderThread(BlockingQueue eventQueue, 
    		LamportClock myLamportClock, PriorityBlockingQueue myPriorityQueue, int pid, ArrayList<MSocket> socketList)
    {
		this.eventQueue = eventQueue;
		this.myLamportClock =  myLamportClock;
		this.myPriorityQueue = myPriorityQueue;		
		this.pid = pid;
		this.socketList = socketList;
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
            	if(Debug.debug) System.out.println("MyServerSenderThread: Going to take from event queue");
            	toBroadcast = (MPacket)eventQueue.take();
            	
            	if (toBroadcast.category == 0)
	            {
	                //Send only head packet of queue, need vector clock mechanism somewhere around here
            		if(Debug.debug) System.out.println("MyServerSenderThread: Taken EVENT from eventqueue.");
	                
	                synchronized(myLamportClock) {
	                	myLamportClock.value = myLamportClock.value + 1;
	                	toBroadcast.lamportClock = myLamportClock.value;
	                }
	                
	                toBroadcast.acks_sent = 0;
	             
	                
	                //First add to my queue, where I will ack myself
	                if(Debug.debug) System.out.println("MyServerSenderThread: Putting event in MY QUEUE with Lamport clock: " + toBroadcast.lamportClock);
	                myPriorityQueue.put(toBroadcast);
	                
	                if(Debug.debug){
	                	System.out.println("MyServerSenderThread: Broadcasting EVENT to all clients except me with Lamport clock: " 
                    		+ toBroadcast.lamportClock + " after putting in MY own queue");
	                }
            	}
            	
            	else if(toBroadcast.category == 1)	//its an ack, which I had put in my event queue to broadcast to others
            	{
            		if(Debug.debug){
            			System.out.println("MyServerSenderThread: Broadcasting ACK to all clients except me with Lamport clock: " 
                    		+ toBroadcast.lamportClock);
            		}
            	}
            	
            	else
            	{
            		if(Debug.debug) System.out.println("MyServerSenderThread: Shouldnt come here! Weird!!");
            	}
                
            	//Send to all clients except me
            	i = 0; 
            	for(MSocket mSocket: socketList)
                {
            		if(Debug.debug){
            			System.out.println("MyServerSenderThread: Writing the THING with category " + 
            				toBroadcast.category + " with lamport clock " + toBroadcast.lamportClock + 
            					" to socket " + i);
            		}
                		mSocket.writeObject(toBroadcast);
                		i++;
                }
                
            }catch(InterruptedException e){
                System.out.println("Throwing Interrupt");
                Thread.currentThread().interrupt();    
            }
            
        }
    }//end bracket for run
    
}
