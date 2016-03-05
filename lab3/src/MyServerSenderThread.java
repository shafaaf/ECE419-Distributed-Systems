import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

//made in MyServerthread.java


public class MyServerSenderThread implements Runnable {

    //private ObjectOutputStream[] outputStreamList = null;
    private MSocket[] mSocketList = null;
    private BlockingQueue eventQueue = null;
    
    
    public MyServerSenderThread(MSocket[] mSocketList, BlockingQueue eventQueue){
		this.mSocketList = mSocketList;
		this.eventQueue = eventQueue;
		
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
                // Send only head packet of queue, need vector clock mechanism
                System.out.println("MyServerSenderThread: Taken from eventqueue. Now broadcast by writing to sockets");
                //Send it to all clients
                for(MSocket mSocket: mSocketList){
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
