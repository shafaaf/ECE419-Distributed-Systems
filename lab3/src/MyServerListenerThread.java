//Listens for actions from specific client (depending on MSocket) in P2P
//Made in MyServerthread.java
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.lang.*;


public class MyServerListenerThread implements Runnable{
	private MSocket mSocket;
    //private BlockingQueue eventQueue;
    private PriorityBlockingQueue myPriorityQueue;
    public LamportClock myLamportClock;

    public MyServerListenerThread( MSocket mSocket, PriorityBlockingQueue myPriorityQueue, LamportClock myLamportClock){
        this.mSocket = mSocket;
        //this.eventQueue = eventQueue;
        this.myPriorityQueue = myPriorityQueue;
        this.myLamportClock = myLamportClock;
        
    }
    
    public void run() {	//read, process and enqueue packet
        MPacket received = null;
        if(Debug.debug) System.out.println("MyServerListenerThread: Starting a listener");
        while(true){
            try{
            	System.out.println("MyServerListenerThread: Going to read from socket");
                received = (MPacket) mSocket.readObject();
                if(Debug.debug) System.out.println("MyServerListenerThread: Read: " + received);
                //have vector clock stuff here
                
                System.out.println("MyServerListenerThread: Updating lamport clock value");
                //Updating lamport clock
                //if(received.lamportClock > myLamportClock.value){
                	int a = (int) Math.round(received.lamportClock) + 1;
                	Double localLamportClock = new Double(a + "." + myLamportClock.pid).doubleValue();
            		myLamportClock.setValue(localLamportClock);
            		
            		System.out.println("MyServerListenerThread: New lamport clock value is " + myLamportClock.value);
            		
            	//}
            	System.out.println("MyServerListenerThread: Putting stuff in myPriorityQueue");
                myPriorityQueue.put(received);
            }
            
            catch(IOException e){
                e.printStackTrace();
            }catch(ClassNotFoundException e){
                e.printStackTrace();
            }
            
        }
    }
    
}


