import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

//made in MyServerthread.java


public class MyServerSenderThread implements Runnable {

    //private ObjectOutputStream[] outputStreamList = null;
    private MSocket[] client_mSocket = null;
    private BlockingQueue eventQueue = null;
    public LamportClock myLamportClock = null;
    public int pid;
    public PriorityBlockingQueue myPriorityQueue;
    
    public MyServerSenderThread(MSocket[] client_mSocket, BlockingQueue eventQueue, 
    		LamportClock myLamportClock, int pid, PriorityBlockingQueue myPriorityQueue){
		this.client_mSocket = client_mSocket;
		this.eventQueue = eventQueue;
		this.myLamportClock =  myLamportClock;
		this.pid = pid;
		this.myPriorityQueue = myPriorityQueue;
    }

    public void run() {
        MPacket toBroadcast = null;
        int z = 0;
        while(true){
            try{	
                //Take packet from queue to broadcast to all clients
            	/*if(eventQueue.isEmpty()){
            		System.out.println("MyServerSenderThread: Event queue is empty");
            	}
            	else{
            		System.out.println("MyServerSenderThread: Event queue is not empty");
            	}*/
            	
            	//These are only EVENTS!
            	System.out.println("MyServerSenderThread: Going to take from event queue");
            	toBroadcast = (MPacket)eventQueue.take();
            	System.out.println("MyServerSenderThread: Taken from eventqueue. Now broadcast by writing to sockets");
                
            	myLamportClock.value = myLamportClock.value + 1;
                System.out.println("MyServerSenderThread: Sending EVENT with incremented lamport clock value " + myLamportClock.value);
                toBroadcast.lamportClock = myLamportClock.value;
                //0 to show its an event and NOT an ack
                toBroadcast.category = 0;
                toBroadcast.acks_sent = 0;
                
                //Put in my queue to myself only IMP
                System.out.println("MyServerSenderThread: I have pid- " + pid + ". Putting EVENT with incremented lamport clock value " 
                		+ myLamportClock.value + " in only my queue");
                
                MPacket putInMine = toBroadcast;
                myPriorityQueue.put(putInMine);
                
                
                //Send it to all clients except yourself
                z = 0;
                System.out.println("MyServerSenderThread: Writing EVENT to sockets");
                for(MSocket mSocket: client_mSocket)
            	{	
                	if(z != pid)
                	{
                		System.out.println("MyServerSenderThread: WRITING event to " + z + " socket in array");
                		mSocket.writeObject(toBroadcast);
                	}
                	System.out.println("MyServerSenderThread: NOT WRITING to " + z + " socket in array");
                	z++;
                }
                z = 0;
                
            	
                	
                
            }catch(InterruptedException e){
                System.out.println("Throwing Interrupt");
                Thread.currentThread().interrupt();    
            }
            
        }
    }
    
}
