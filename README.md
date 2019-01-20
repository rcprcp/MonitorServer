# MonitorServer

This is the backend server for the Monitor system.  This program runs as a stand-alone JVM
and receives information from the Monitor javaagent [http://github.com/rcprcp/Monitor.git]
which is publishing various information
to this server.  The information is stored in a database and the MonitorServer also
provides a UI to access the provides a UI to view the information. 

##Installation and use
* Ensure you have a MySQL instance running and have created a user who can create databases, SELECT and INSERT.  
* Optionally, create a directory for MonitorServer, and Monitor  `cd bobTest`; cd into that directory.
* download the repo: `git clone http://github.com/MonitorServer.git
* create the monitordb database in MySQL. 
* after starting the mysql command line you can do something like: 
* MySQL>   \\. <path_to_MonitorServer>/src/main/resources/monitor_server.sql
* exit MySQL client.
* then cd into the MonitorServer directory.
* build: `mvn clean package`
* cd into the target directory
* run it:  `java -jar MonitorServer-1.0-SNAPSHOT-jar-with-dependencies.jar`

At this point you should get trace something like the following:

````
Running! Point your browsers to http://Bobs-iMac.local:22277
DBLogger: Connected to database jdbc:mysql://localhost:3306/
````
##TODO:

- [ ]  fix/enhance the UI
- [ ] PostgreSQL support