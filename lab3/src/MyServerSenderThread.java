import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

//made in MyServerthread.java


public class MyServerSenderThread implements Runnable {

    //private ObjectOutputStream[] outputStreamList = null;
    private MSocket[] client_mSocket = null;
    private BlockingQueue eventQueue = null;
    public LamportClock myLamportClock = null;
    public PriorityBlockingQueue myPriorityQueue;
    public int pid;
    public ArrayList<MSocket> socketList;
    
    
    public MyServerSenderThread(MSocket[] client_mSocket, BlockingQueue eventQueue, 
    		LamportClock myLamportClock, PriorityBlockingQueue myPriorityQueue, int pid, ArrayList<MSocket> socketList)
    {
		this.client_mSocket = client_mSocket;
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

            	//Take packet from queue to broadcast to all clients
            	/*if(eventQueue.isEmpty()){
            		System.out.println("MyServerSenderThread: Event queue is empty");
            	}
            	else{
            		System.out.println("MyServerSenderThread: Event queue is not empty");
            	}*/
            	
            	System.out.println("MyServerSenderThread: Going to take from event queue");
            	toBroadcast = (MPacket)eventQueue.take();
                // Send only head packet of queue, need vector clock mechanism somewhere around here
                System.out.println("MyServerSenderThread: Taken from eventqueue.");
                myLamportClock.value = myLamportClock.value + 1;
                
                /*Setup broadcast packet*/
                toBroadcast.lamportClock = myLamportClock.value;
                //0 to show its an event and NOT an ack
                toBroadcast.category = 0;
                //0 to show that no acks for it have been sent
                toBroadcast.acks_sent = 0;
                
                
                /*Setup My packet which I will put in my queue*/
                //from gui client - eventQueue.put(new MPacket(getName(), MPacket.ACTION, MPacket.DOWN));
                //MPacket constructor - public MPacket(String name, int type, int event){
                toMe = new MPacket(toBroadcast.name, toBroadcast.type, toBroadcast.event);
                toMe.lamportClock = toBroadcast.lamportClock;
                toMe.category = toBroadcast.category;
                toMe.acks_sent = toBroadcast.acks_sent;
                
                /*I add to my queue first and broadcast to everyone except for me*/
                //System.out.println("Putting event in MY QUEUE with Lamport clock: " + toMe.lamportClock);
                //myPriorityQueue.put(toMe);
                
                //To me
                //client_mSocket[pid].writeObject(toBroadcast);
                
                /*
                try {
                    Thread.sleep(400);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }*/
                
                
                //Send it to all clients except me
                System.out.println("BROADCAST event to ALl clients with Lamport clock: " 
                		+ toBroadcast.lamportClock);
                i = 0;
                for(MSocket mSocket: client_mSocket)
                {
                	// if(i != pid)
                	{
                		System.out.println("MyServerSenderThread: Writing EVENT with lamport clock " + 
                				toBroadcast.lamportClock + " to socket " + i);
                		mSocket.writeObject(toBroadcast);
                	}
                	
                	/*else
                	{
                		System.out.println("MyServerSenderThread: NOT Writing to socket " + i + " as thats me!");
                	}*/
                	i++;
                	
                }
                i = 0;
                
                
            }catch(InterruptedException e){
                System.out.println("Throwing Interrupt");
                Thread.currentThread().interrupt();    
            }
            
        }
    }
    
}
