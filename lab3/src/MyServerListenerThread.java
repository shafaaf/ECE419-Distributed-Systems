//Listens for actions from specific client (depending on MSocket) in P2P
//Made in MyServerthread.java
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.lang.*;


public class MyServerListenerThread implements Runnable{
	private MSocket mSocket;
    //private BlockingQueue eventQueue;
    private PriorityBlockingQueue myPriorityQueue;
    public LamportClock myLamportClock;
    public HashMap<Double, Integer> lamportAcks;
    public int pid;
    
    public MyServerListenerThread( MSocket mSocket, PriorityBlockingQueue myPriorityQueue, LamportClock myLamportClock, 
    		HashMap<Double, Integer> lamportAcks, int pid){
        this.mSocket = mSocket;
        this.myPriorityQueue = myPriorityQueue;
        this.myLamportClock = myLamportClock;
        this.lamportAcks = lamportAcks;
        this.pid = pid;
    }
    
    public void run() {	//read, process and enqueue packet
        MPacket received = null;
        if(Debug.debug) System.out.println("MyServerListenerThread: Starting a listener");
        while(true){
            try{
            	System.out.println("MyServerListenerThread: Going to read from socket");
                received = (MPacket) mSocket.readObject();
                if(Debug.debug) System.out.println("MyServerListenerThread: Read: " + received);
                
                
                if(received.category == 0) //if 0, an event, so put in queue
                {
                	System.out.println("MyServerListenerThread: Got an EVENT!");
	               if(received.lamportClock > myLamportClock.value)  //Updating lamport clock
	                {
	            	    System.out.println("MyServerListenerThread: Updating lamport clock value as new one is HIGHER");
	            	    System.out.println("MyServerListenerThread: Old one is " + myLamportClock.value + 
	            	    		" and new one FROM EVENT received is " + received.lamportClock);

	                	int a = (int) Math.round(received.lamportClock);
	                	Double localLamportClock = new Double(a + "." + pid).doubleValue();
	            		myLamportClock.value = (double) localLamportClock;
	            		
	            	}
	               else
	               {
	            	   System.out.println("MyServerListenerThread: NOT updating lamport clock value - My Lamport clock value is " + 
	            			   myLamportClock.value + " and one received is " + received.lamportClock);
	               }
	               
	               System.out.println("MyServerListenerThread: My Lamport clock value is right now " + myLamportClock.value);

	               //only events in priority queue
	               System.out.println("MyServerListenerThread: Putting EVENT in myPriorityQueue");
	               myPriorityQueue.put(received);
                }
                
                else //its an ACK, dont put in queue and just update hashmap
                {
                	System.out.println("MyServerListenerThread: Got an Ack! Its lamport clock value is " + 
                			received.lamportClock);
                	
                	//If not present in hashmap, make new entry with acks received as 1
                	if(lamportAcks.get(received.lamportClock) == null)
                	{
                		lamportAcks.put(new Double(received.lamportClock), new Integer(1));
                		System.out.println("MyServerListenerThread: FIRST Ack for " + received.lamportClock 
                			+  " so total number of acks is " + (double)lamportAcks.get(received.lamportClock));
                	}
                	
                	else
                	{	//else increment acks for that entry by 1
                		lamportAcks.put(received.lamportClock, (lamportAcks.get(received.lamportClock)) + 1);
                		System.out.println("MyServerListenerThread: ANOTHER Ack for " + received.lamportClock 
                    			+  " so total number for acks is now " + (double)lamportAcks.get(received.lamportClock));
                		
                	}
                	                	
                }
            
            }
            
            catch(IOException e){
                e.printStackTrace();
            }catch(ClassNotFoundException e){
                e.printStackTrace();
            }
            
        }
    }
    
}


