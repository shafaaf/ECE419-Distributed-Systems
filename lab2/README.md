The completed lab code is in src and bin. The other folders are for references and not needed.

Introduction
============
This implements a client-server Mazewar game. 
Additionally, the MSocket class can be used to 
induce errors, such as adding delays and reordering packets.


Architecture
============
The server starts and listens on some port.

When a client contacts the server, the server spawns a 
ServerListenerThread. Server.java defines the MAX_CLIENTS constant 
that determines the maximum number of clients that join.

When the expected number of clients have contacted the 
server, the server spawns a single ServerSenderThreads, that broadcasts 
the name and location of all the clients (this requires that names
must be unique).

When an event happens, it is sent to the server. The server
stamps the event with a global sequence number and broadcasts to all 
hosts.

The client and server communicates using MSockets, and MServerSockets
which are analogous to Sockets and ServerSockets. MSockets are 
constructed by passing a host address and port. MSockets expose two 
main methods, readObject and writeObject to read and write objects, respectively. 

The MSocket can reorder packets and add delays based on 
3 constants defined in MSocket.java, namely DELAY_WEIGHT, DELAY_THRESHOLD, 
UNORDER_FACTOR. Errors are added both on sending and receiveing packets.


Making and Running
==================
To make the project run
make

To run the server:
</path/to/java/>java Server <listening port>

To run the clients: 
</path/to/java/>java Mazewar <server host> <server port>


What we did in this lab
=======================
The server was sending packets with a sequence number. Made sure packets are popped from the queue on the client side in the correct order using a global counter. Therefore all clients would be see the same screen.
Previously, projectile movement was done on client side and therefore not consistent with other clients due to delays in local machine.

Now, each client tells the server to broadcast to everyone to move its projectile by 1 unit if it exists. Keep doing this until projectile dies.


