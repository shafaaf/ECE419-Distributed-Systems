import java.io.Serializable;

public class Clientinfo implements Serializable {
	public String hostName;
	public int port;
	public int pid;
	public static int pid_i = 0;
	
	public Clientinfo(String hostName, int port){
		this.hostName = hostName;
		this.port = port;
		this.pid = pid_i;
		Clientinfo.pid_i += 1;
	}
	
	
}

