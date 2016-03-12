import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class ClientQueueExecutionThread implements Runnable {

    private MSocket mSocket  =  null;
    private Hashtable<String, Client> clientTable = null;
    
    //my priority queue
    private PriorityBlockingQueue<MPacket> myPriorityQueue = null;
    
    private int clientEventNumber;
    
    public ClientQueueExecutionThread( MSocket mSocket,
        Hashtable<String, Client> clientTable,PriorityBlockingQueue myPriorityQueue)
    {
        this.mSocket = mSocket;
        this.clientTable = clientTable;
        
        //my queue here initialized
        this.myPriorityQueue = myPriorityQueue;
        
        this.clientEventNumber = 0;
        
        if(Debug.debug) System.out.println("ClientQueueExecutionThread: Instatiating QueueExecutionThread");
    }

    public void run() {
        MPacket received = null;
        Client client = null;
        if(Debug.debug) System.out.println("ClientQueueExecutionThread: Starting Instatiating QueueExecutionThread");
        
        while(true){
        	//System.out.println("ClientQueueExecutionThread: Checking if queue is empty or not");
        	if(!myPriorityQueue.isEmpty())
        	{
        		//System.out.println("ClientQueueExecutionThread: Priority queue is NOT empty");
                //if(myPriorityQueue.peek().sequenceNumber == clientEventNumber)
                {
                	System.out.println("YES! clientEventNumber: " + clientEventNumber);
                	System.out.println("QueueExecutionThread  myProrityQueue's event dequing is " + myPriorityQueue.peek() + "\n");
                	received = myPriorityQueue.poll();
                	client = clientTable.get(received.name);
                	
                	//Debugging
                	//System.out.println("QueueExecutionThread: client is: " + received.name);
                	
                   	//execute client actions. Here the client class refers to the client who actually caused the movement, fire or projectile movement
                    if(received.event == MPacket.UP){
                        client.forward();
                    }else if(received.event == MPacket.DOWN){
                        client.backup();
                    }else if(received.event == MPacket.LEFT){
                        client.turnLeft();
                    }else if(received.event == MPacket.RIGHT){
                        client.turnRight();
                    }else if(received.event == MPacket.FIRE){
                        client.fire();
                    }else if(received.event == MPacket.PROJECTILE_MOVEMENT){
                        client.myMoveProjectile();
                    }
                    else{
                        throw new UnsupportedOperationException();
                    }
                  //then wait for next packet
                    clientEventNumber++;
                }
        	} 
                
        	
        }
    }
}
