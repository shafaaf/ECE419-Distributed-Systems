#Distributed Systems Lab 3

Distributed P2P implementation of the Mazewar multiplayer game using Lamport clocks. Total Order Multicast is used to achieve this.

To Run
- go to src
- run commands: make clean, followed by make (make sure the path to Java is fine) 
- run the server, by using command: server.sh (port) 
- run Mazewar client instance, by using command:run.sh (serverHost) (serverPort)
- to change number of players, go to Server.java and set MAX_CLIENTS to whatever number you want. Do the same for MAX_CLIENTS in Mazewar.java. Then repeat the above to run.

