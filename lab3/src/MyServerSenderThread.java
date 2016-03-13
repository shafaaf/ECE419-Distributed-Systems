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
    
    
    public MyServerSenderThread(MSocket[] client_mSocket, BlockingQueue eventQueue, LamportClock myLamportClock, PriorityBlockingQueue myPriorityQueue)
    {
		this.client_mSocket = client_mSocket;
		this.eventQueue = eventQueue;
		this.myLamportClock =  myLamportClock;
		this.myPriorityQueue = myPriorityQueue;		
    }

    public void run() {
        MPacket toBroadcast = null;
        
        while(true){
            try{	//dequeue packet, add global sequence number, and broadcast
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
                System.out.println("MyServerSenderThread: Taken from eventqueue. Now broadcast by writing to sockets");
                myLamportClock.value = myLamportClock.value + 1;
                System.out.println("MyServerSenderThread: Sending EVENT with new lamport clock value " + myLamportClock.value);
                
                toBroadcast.lamportClock = myLamportClock.value;
                //0 to show its an event and NOT an ack
                toBroadcast.category = 0;
                //0 to show that no acks for it have been sent
                toBroadcast.acks_sent = 0;
                
                /*NEED EDIT HERE, WHERE I add to my queue first and broadcast to everyone except for me*/
                
                //Send it to all clients
                for(MSocket mSocket: client_mSocket){
                	System.out.println("MyServerSenderThread: Writing to sockets");
                    mSocket.writeObject(toBroadcast);
                }
            }catch(InterruptedException e){
                System.out.println("Throwing Interrupt");
                Thread.currentThread().interrupt();    
            }
            
        }
    }
    
}
