To Run
- go to src
- run commands: make clean, followed by make (make sure the path to Java is fine) 
- run the server, by using command: server.sh (port) 
- run Mazewar client instance, by using command:run.sh (serverHost) (serverPort)
- to change number of players, go to Server.java and set MAX_CLIENTS to whatever number you want and repeat the above.

Summary
This is distributed version of the Mazewar game. A p2p system is made and Total Order Multicasts are used to establish a 
global ordering of events. 

