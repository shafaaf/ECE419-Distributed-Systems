//this is Mpacket.javazzzzz
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MPacket implements Serializable, Comparable<MPacket> {
	
    /*The following are the type of events*/
    public static final int HELLO = 100;
    public static final int ACTION = 200;
   

    /*The following are the specific action 
    for each type*/
    
    /*Initial Hello*/
    public static final int HELLO_INIT = 101;
    /*Response to Hello*/
    public static final int HELLO_RESP = 102;

    /*Action*/
    public static final int UP = 201;
    public static final int DOWN = 202;
    public static final int LEFT = 203;
    public static final int RIGHT = 204;
    public static final int FIRE = 205;
    //my code to include projectile movement
    public static final int PROJECTILE_MOVEMENT = 207;
    
    
    //These fields characterize the event  
    public int type;
    public int event; 

    //The name determines the client that initiated the event
    public String name;
    
    //The sequence number of the event
    public int sequenceNumber;
    
    //The Lamport clock number of the event
    public double lamportClock;
    
   //Says event or ack, 0 means event, 1 means ack
    public int category;
   
   //Flag to know whether acks sent or not
    public int acks_sent;
    
    //These are used to initialize the board
    public int mazeSeed;
    public int mazeHeight;
    public int mazeWidth;
    public Player[] players;
    
    //Host and port Number for registration with hello packet
    public String hostName;
    public int portNumber;
    
    public ArrayList<Clientinfo> clientInfo;
    
    public MPacket(int type, int event){
        this.type = type;
        this.event = event;
        this.clientInfo =  new ArrayList<Clientinfo>();
    }
    
    public MPacket(String name, int type, int event){
        this.name = name;
        this.type = type;
        this.event = event;
        this.clientInfo =  new ArrayList<Clientinfo>();
    }
    
    //Use this constructor when sending lamport clock
    public MPacket(String name, int type, int event, double lamportClock, int category){
        this.name = name;
        this.type = type;
        this.event = event;
        this.clientInfo =  new ArrayList<Clientinfo>();
        this.lamportClock = lamportClock;
        this.category = category;
    }
    
    //Use this constructor when sending acks
    public MPacket(int category, double lamportClock){
    	this.category = category;
    	this.lamportClock = lamportClock;
    }

    
    
    public String toString(){
        String typeStr;
        String eventStr;
        
        switch(type){
            case 100:
                typeStr = "HELLO";
                break;
            case 200:
                typeStr = "ACTION";
                break;
            default:
                typeStr = "ERROR";
                break;        
        }
        switch(event){
            case 101:
                eventStr = "HELLO_INIT";
                break;
            case 102:
                eventStr = "HELLO_RESP";
                break;
            case 201:
                eventStr = "UP";
                break;
            case 202:
                eventStr = "DOWN";
                break;
            case 203:
                eventStr = "LEFT";
                break;
            case 204:
                eventStr = "RIGHT";
                break;
            case 205:
                eventStr = "FIRE";
                break;
            case 207:
            	eventStr = "PROJECTILE_MOVEMENT";
            	break;
            default:
                eventStr = "ERROR";
                break;        
        }
        
        //MPACKET(NAME: name, <typestr: eventStr>, SEQNUM: sequenceNumber)
        String retString = String.format("MPACKET(NAME: %s, <%s: %s>, LAMPORTCLOCK: %s)", name, 
            typeStr, eventStr, lamportClock);	//last parameter was sequence number
        
        return retString;
    }
    
    public int compareTo(MPacket o){	//if positive this bigger, else other guy
    	/* Our old code which was WRONG: return (int)(this.lamportClock - o.lamportClock);*/
    	if (this.lamportClock - o.lamportClock < 0){return -1;}
    	if (this.lamportClock - o.lamportClock > 0){return 1;}
    	return 0;
    	
    }

}
