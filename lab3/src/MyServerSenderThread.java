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
            	
                toBroadcast = (MPacket)eventQueue.take();
                // Send only head packet of queue, need vector clock mechanism
                if(Debug.debug) System.out.println("MyServerSenderThread: Sending " + toBroadcast);
                //Send it to all clients
                for(MSocket mSocket: mSocketList){
                    mSocket.writeObject(toBroadcast);
                }
            }catch(InterruptedException e){
                System.out.println("Throwing Interrupt");
                Thread.currentThread().interrupt();    
            }
            
        }
    }
    
}
