# Send Deltas Between WAN Sites
## Description

This project provides a way to send Deltas between WAN sites.

It configures a PartitionedRegion co-located with the data region that is attached to the GatewaySender.

It also provides several CacheCallbacks that facilitate Deltas being sent between WAN sites, including:

- A **GatewaySenderDeltaCacheWriter** attached to the data Region
- A **GatewaySenderDeltaCacheListener** attached to the data Region
- A **GatewaySenderProxyCacheWriter** attached to the proxy Region

The **GatewaySenderDeltaCacheWriter**:

- initializes the tail key in the EntryEvent
- sets the tail key as the callback argument

The **GatewaySenderDeltaCacheListener**:

- gets the co-located proxy Region
- creates an EntryEvent using the proxy Region and input EntryEvent including:
	- operation
	- full or delta bytes
	- boolean callback argument whether delta or not
- retrieves the proxy Region's GatewaySenders
- distributes the EntryEvent to each GatewaySender

The **GatewaySenderProxyCacheWriter**:

- gets the co-located data Region
- invokes either basicBridgeUpdate or basicBridgeDestroy depending on the EntryEvent's operation, bytes and callback argument

## Initialization
Modify the **GEODE** environment variable in the *setenv.sh* script to point to a Geode installation directory.
## Build
Build the Spring Boot Client Application and Geode Server Function and sizer classes using gradle like:

```
./gradlew clean jar bootJar
```
## Run Example
### Start and Configure Locator and Servers
Start and configure the locator and 2 servers in each WAN site using the *startandconfigure.sh* script like:

```
./startandconfigure.sh
```
### Create Entries
Run the client to create Session instances using the *runclient.sh* script like below.

```
./runclient.sh create 50
```
The parameters are:

- operation (e.g. create)
- number of entries (e.g. 50)

### Update Entries (Add Attributes)
Run the client to add attributes to the Sessions using the *runclient.sh* script like below.

```
./runclient.sh add-attributes 50
```
The parameters are:

- operation (e.g. add-attributes)
- number of entries (e.g. 50)

### Update Entries (Remove Attributes)
Run the client to remove attributes from the Sessions using the *runclient.sh* script like below.

```
./runclient.sh remove-attributes 50
```
The parameters are:

- operation (e.g. a remove dd-attributes)
- number of entries (e.g. 50)

### Destroy Entries
Run the client to destroy Session instances using the *runclient.sh* script like below.

```
./runclient.sh destroy 50
```
The parameters are:

- operation (e.g. destroy)
- number of entries (e.g. 50)

### Shutdown Locator and Servers
Execute the * shutdownsites.sh* script to shutdown the servers and locators in both WAN sites like:

```
./shutdownsites.sh
```
### Remove Locator and Server Files
Execute the *cleanupfiles.sh* script to remove the server and locator files like:

```
./cleanupfiles.sh
```
## Example Output
### Start and Configure Locator and Servers
Sample output from the *startandconfigure.sh* script is:

```
./startandconfigure.sh

1. Executing - set variable --name=APP_RESULT_VIEWER --value=any

Value for variable APP_RESULT_VIEWER is now: any.

2. Executing - start locator --name=locator-ln --port=10331 --locators=localhost[10331] --mcast-port=0 --J=-Dgemfire.remote-locators=localhost[10332] --J=-Dgemfire.distributed-system-id=1 --J=-Dgemfire.jmx-manager-start=true --J=-Dgemfire.jmx-manager-http-port=8081 --J=-Dgemfire.jmx-manager-port=1091

.....................
Locator in <working-directory>/locator-ln on xxx.xxx.x.x[10331] as locator-ln is currently online.
Process ID: 66374
Uptime: 27 seconds
Geode Version: 1.13.1-build.0
Java Version: 1.8.0_151
Log File: <working-directory>/locator-ln/locator-ln.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

Successfully connected to: JMX Manager [host=xxx.xxx.x.x, port=1091]

Cluster configuration service is up and running.

3. Executing - start server --name=server-ln-1 --locators=localhost[10331] --server-port=0 --initial-heap=2g --max-heap=2g --statistic-archive-file=cacheserver.gfs --classpath=../server/build/libs/server-0.0.1-SNAPSHOT.jar --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

..........
Server in <working-directory>/server-ln-1 on xxx.xxx.x.x[65149] as server-ln-1 is currently online.
Process ID: 66461
Uptime: 8 seconds
Geode Version: 1.13.1-build.0
Java Version: 1.8.0_151
Log File: <working-directory>/server-ln-1/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

4. Executing - start server --name=server-ln-2 --locators=localhost[10331] --server-port=0 --initial-heap=2g --max-heap=2g --statistic-archive-file=cacheserver.gfs --classpath=../server/build/libs/server-0.0.1-SNAPSHOT.jar --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

..............
Server in <working-directory>/server-ln-2 on xxx.xxx.x.x[65176] as server-ln-2 is currently online.
Process ID: 66462
Uptime: 10 seconds
Geode Version: 1.13.1-build.0
Java Version: 1.8.0_151
Log File: <working-directory>/server-ln-2/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

5. Executing - create gateway-sender --id=ny --remote-distributed-system-id=2 --parallel=true

  Member    | Status | Message
----------- | ------ | -------------------------------------------
server-ln-1 | OK     | GatewaySender "ny" created on "server-ln-1"
server-ln-2 | OK     | GatewaySender "ny" created on "server-ln-2"

Cluster configuration for group 'cluster' is updated.

6. Executing - sleep --time=5


7. Executing - create region --name=session --type=PARTITION_REDUNDANT --cache-writer=example.server.callback.GatewaySenderDeltaCacheWriter --cache-listener=example.server.callback.GatewaySenderDeltaCacheListener

  Member    | Status | Message
----------- | ------ | ------------------------------------------
server-ln-1 | OK     | Region "/session" created on "server-ln-1"
server-ln-2 | OK     | Region "/session" created on "server-ln-2"

Cluster configuration for group 'cluster' is updated.

8. Executing - create region --name=session_gateway_sender_delta_proxy --type=PARTITION_REDUNDANT --gateway-sender-id=ny --colocated-with=session --redundant-copies=1 --cache-writer=example.server.callback.GatewaySenderProxyCacheWriter

  Member    | Status | Message
----------- | ------ | ---------------------------------------------------------------------
server-ln-1 | OK     | Region "/session_gateway_sender_delta_proxy" created on "server-ln-1"
server-ln-2 | OK     | Region "/session_gateway_sender_delta_proxy" created on "server-ln-2"

Cluster configuration for group 'cluster' is updated.

9. Executing - create gateway-receiver

  Member    | Status | Message
----------- | ------ | ----------------------------------------------------------------------------------
server-ln-1 | OK     | GatewayReceiver created on member "server-ln-1" and will listen on the port "5496"
server-ln-2 | OK     | GatewayReceiver created on member "server-ln-2" and will listen on the port "5321"

Cluster configuration for group 'cluster' is updated.

10. Executing - disconnect

Disconnecting from: xxx.xxx.x.x[1091]
Disconnected from : xxx.xxx.x.x[1091]

11. Executing - start locator --name=locator-ny --port=10332 --locators=localhost[10332] --mcast-port=0 --J=-Dgemfire.remote-locators=localhost[10331] --J=-Dgemfire.distributed-system-id=2 --J=-Dgemfire.jmx-manager-start=true --J=-Dgemfire.jmx-manager-http-port=8082 --J=-Dgemfire.jmx-manager-port=1092

......................
Locator in <working-directory>/locator-ny on xxx.xxx.x.x[10332] as locator-ny is currently online.
Process ID: 66464
Uptime: 26 seconds
Geode Version: 1.13.1-build.0
Java Version: 1.8.0_151
Log File: <working-directory>/locator-ny/locator-ny.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

Successfully connected to: JMX Manager [host=xxx.xxx.x.x, port=1092]

Cluster configuration service is up and running.

12. Executing - start server --name=server-ny-1 --locators=localhost[10332] --server-port=0 --statistic-archive-file=cacheserver.gfs --classpath=../server/build/libs/server-0.0.1-SNAPSHOT.jar --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

...........
Server in <working-directory>/server-ny-1 on xxx.xxx.x.x[65268] as server-ny-1 is currently online.
Process ID: 66541
Uptime: 8 seconds
Geode Version: 1.13.1-build.0
Java Version: 1.8.0_151
Log File: <working-directory>/server-ny-1/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

13. Executing - start server --name=server-ny-2 --locators=localhost[10332] --server-port=0 --statistic-archive-file=cacheserver.gfs --classpath=../server/build/libs/server-0.0.1-SNAPSHOT.jar --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

.........
Server in <working-directory>/server-ny-2 on xxx.xxx.x.x[65300] as server-ny-2 is currently online.
Process ID: 66564
Uptime: 7 seconds
Geode Version: 1.13.1-build.0
Java Version: 1.8.0_151
Log File: <working-directory>/server-ny-2/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

14. Executing - create gateway-sender --id=ln --remote-distributed-system-id=1 --parallel=true

  Member    | Status | Message
----------- | ------ | -------------------------------------------
server-ny-1 | OK     | GatewaySender "ln" created on "server-ny-1"
server-ny-2 | OK     | GatewaySender "ln" created on "server-ny-2"

Cluster configuration for group 'cluster' is updated.

15. Executing - sleep --time=5


16. Executing - create region --name=session --type=PARTITION_REDUNDANT --cache-writer=example.server.callback.GatewaySenderDeltaCacheWriter --cache-listener=example.server.callback.GatewaySenderDeltaCacheListener

  Member    | Status | Message
----------- | ------ | ------------------------------------------
server-ny-1 | OK     | Region "/session" created on "server-ny-1"
server-ny-2 | OK     | Region "/session" created on "server-ny-2"

Cluster configuration for group 'cluster' is updated.

17. Executing - create region --name=session_gateway_sender_delta_proxy --type=PARTITION_REDUNDANT --gateway-sender-id=ln --colocated-with=session --redundant-copies=1 --cache-writer=example.server.callback.GatewaySenderProxyCacheWriter

  Member    | Status | Message
----------- | ------ | ---------------------------------------------------------------------
server-ny-1 | OK     | Region "/session_gateway_sender_delta_proxy" created on "server-ny-1"
server-ny-2 | OK     | Region "/session_gateway_sender_delta_proxy" created on "server-ny-2"

Cluster configuration for group 'cluster' is updated.

18. Executing - create gateway-receiver

  Member    | Status | Message
----------- | ------ | ----------------------------------------------------------------------------------
server-ny-1 | OK     | GatewayReceiver created on member "server-ny-1" and will listen on the port "5166"
server-ny-2 | OK     | GatewayReceiver created on member "server-ny-2" and will listen on the port "5176"

Cluster configuration for group 'cluster' is updated.

19. Executing - disconnect

Disconnecting from: xxx.xxx.x.x[1092]
Disconnected from : xxx.xxx.x.x[1092]

************************* Execution Summary ***********************
Script file: startandconfigure.gfsh

Command-1 : set variable --name=APP_RESULT_VIEWER --value=any
Status    : PASSED

Command-2 : start locator --name=locator-ln --port=10331 --locators=localhost[10331] --mcast-port=0 --J=-Dgemfire.remote-locators=localhost[10332] --J=-Dgemfire.distributed-system-id=1 --J=-Dgemfire.jmx-manager-start=true --J=-Dgemfire.jmx-manager-http-port=8081 --J=-Dgemfire.jmx-manager-port=1091
Status    : PASSED

Command-3 : start server --name=server-ln-1 --locators=localhost[10331] --server-port=0 --initial-heap=2g --max-heap=2g --statistic-archive-file=cacheserver.gfs --classpath=../server/build/libs/server-0.0.1-SNAPSHOT.jar --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false
Status    : PASSED

Command-4 : start server --name=server-ln-2 --locators=localhost[10331] --server-port=0 --initial-heap=2g --max-heap=2g --statistic-archive-file=cacheserver.gfs --classpath=../server/build/libs/server-0.0.1-SNAPSHOT.jar --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false
Status    : PASSED

Command-5 : create gateway-sender --id=ny --remote-distributed-system-id=2 --parallel=true
Status    : PASSED

Command-6 : sleep --time=5
Status    : PASSED

Command-7 : create region --name=session --type=PARTITION_REDUNDANT --cache-writer=example.server.callback.GatewaySenderDeltaCacheWriter --cache-listener=example.server.callback.GatewaySenderDeltaCacheListener
Status    : PASSED

Command-8 : create region --name=session_gateway_sender_delta_proxy --type=PARTITION_REDUNDANT --gateway-sender-id=ny --colocated-with=session --redundant-copies=1 --cache-writer=example.server.callback.GatewaySenderProxyCacheWriter
Status    : PASSED

Command-9 : create gateway-receiver
Status    : PASSED

Command-10 : disconnect
Status     : PASSED

Command-11 : start locator --name=locator-ny --port=10332 --locators=localhost[10332] --mcast-port=0 --J=-Dgemfire.remote-locators=localhost[10331] --J=-Dgemfire.distributed-system-id=2 --J=-Dgemfire.jmx-manager-start=true --J=-Dgemfire.jmx-manager-http-port=8082 --J=-Dgemfire.jmx-manager-port=1092
Status     : PASSED

Command-12 : start server --name=server-ny-1 --locators=localhost[10332] --server-port=0 --statistic-archive-file=cacheserver.gfs --classpath=../server/build/libs/server-0.0.1-SNAPSHOT.jar --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false
Status     : PASSED

Command-13 : start server --name=server-ny-2 --locators=localhost[10332] --server-port=0 --statistic-archive-file=cacheserver.gfs --classpath=../server/build/libs/server-0.0.1-SNAPSHOT.jar --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false
Status     : PASSED

Command-14 : create gateway-sender --id=ln --remote-distributed-system-id=1 --parallel=true
Status     : PASSED

Command-15 : sleep --time=5
Status     : PASSED

Command-16 : create region --name=session --type=PARTITION_REDUNDANT --cache-writer=example.server.callback.GatewaySenderDeltaCacheWriter --cache-listener=example.server.callback.GatewaySenderDeltaCacheListener
Status     : PASSED

Command-17 : create region --name=session_gateway_sender_delta_proxy --type=PARTITION_REDUNDANT --gateway-sender-id=ln --colocated-with=session --redundant-copies=1 --cache-writer=example.server.callback.GatewaySenderProxyCacheWriter
Status     : PASSED

Command-18 : create gateway-receiver
Status     : PASSED

Command-19 : disconnect
Status     : PASSED
```
### Create Entries
Sample output from the *runclient.sh* script is:

```
./runclient.sh create 50   

2021-03-20 07:44:26.968  INFO 64597 --- [           main] example.client.Client                    : Starting Client ...
...
2021-03-20 07:44:36.827  INFO 64597 --- [           main] example.client.Client                    : Started Client in 10.688 seconds (JVM running for 11.389)
2021-03-20 07:44:36.832  INFO 64597 --- [           main] example.client.service.SessionService    : Creating 50 sessions
2021-03-20 07:44:37.240  INFO 64597 --- [           main] example.client.service.SessionService    : Saved Session[id=0; attributes={}]
2021-03-20 07:44:37.359  INFO 64597 --- [           main] example.client.service.SessionService    : Saved Session[id=1; attributes={}]
2021-03-20 07:44:37.436  INFO 64597 --- [           main] example.client.service.SessionService    : Saved Session[id=2; attributes={}]
2021-03-20 07:44:37.507  INFO 64597 --- [           main] example.client.service.SessionService    : Saved Session[id=3; attributes={}]
...
2021-03-20 07:44:42.684  INFO 64597 --- [           main] example.client.service.SessionService    : Saved Session[id=45; attributes={}]
2021-03-20 07:44:42.742  INFO 64597 --- [           main] example.client.service.SessionService    : Saved Session[id=46; attributes={}]
2021-03-20 07:44:42.850  INFO 64597 --- [           main] example.client.service.SessionService    : Saved Session[id=47; attributes={}]
2021-03-20 07:44:42.940  INFO 64597 --- [           main] example.client.service.SessionService    : Saved Session[id=48; attributes={}]
2021-03-20 07:44:43.013  INFO 64597 --- [           main] example.client.service.SessionService    : Saved Session[id=49; attributes={}]
```
### Update Entries (Add Attributes)
Sample output from the *runclient.sh* script is:

```
./runclient.sh add-attributes 50

2021-03-20 07:46:26.866  INFO 64770 --- [           main] example.client.Client                    : Starting Client ...
...
2021-03-20 07:46:31.664  INFO 64770 --- [           main] example.client.Client                    : Started Client in 5.512 seconds (JVM running for 6.072)
2021-03-20 07:46:31.669  INFO 64770 --- [           main] example.client.service.SessionService    : Adding attributes to 50 sessions
2021-03-20 07:46:31.777  INFO 64770 --- [           main] example.client.service.SessionService    : Retrieved Session[id=0; attributes={}]
2021-03-20 07:46:31.860  INFO 64770 --- [           main] example.client.service.SessionService    : Added attributes to Session[id=0; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:46:31.881  INFO 64770 --- [           main] example.client.service.SessionService    : Retrieved Session[id=1; attributes={}]
2021-03-20 07:46:31.896  INFO 64770 --- [           main] example.client.service.SessionService    : Added attributes to Session[id=1; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:46:31.899  INFO 64770 --- [           main] example.client.service.SessionService    : Retrieved Session[id=2; attributes={}]
2021-03-20 07:46:31.912  INFO 64770 --- [           main] example.client.service.SessionService    : Added attributes to Session[id=2; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:46:31.914  INFO 64770 --- [           main] example.client.service.SessionService    : Retrieved Session[id=3; attributes={}]
2021-03-20 07:46:31.926  INFO 64770 --- [           main] example.client.service.SessionService    : Added attributes to Session[id=3; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:46:31.928  INFO 64770 --- [           main] example.client.service.SessionService    : Retrieved Session[id=4; attributes={}]
2021-03-20 07:46:31.940  INFO 64770 --- [           main] example.client.service.SessionService    : Added attributes to Session[id=4; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
...
2021-03-20 07:46:32.586  INFO 64770 --- [           main] example.client.service.SessionService    : Retrieved Session[id=45; attributes={}]
2021-03-20 07:46:32.598  INFO 64770 --- [           main] example.client.service.SessionService    : Added attributes to Session[id=45; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:46:32.601  INFO 64770 --- [           main] example.client.service.SessionService    : Retrieved Session[id=46; attributes={}]
2021-03-20 07:46:32.641  INFO 64770 --- [           main] example.client.service.SessionService    : Added attributes to Session[id=46; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:46:32.644  INFO 64770 --- [           main] example.client.service.SessionService    : Retrieved Session[id=47; attributes={}]
2021-03-20 07:46:32.689  INFO 64770 --- [           main] example.client.service.SessionService    : Added attributes to Session[id=47; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:46:32.696  INFO 64770 --- [           main] example.client.service.SessionService    : Retrieved Session[id=48; attributes={}]
2021-03-20 07:46:32.742  INFO 64770 --- [           main] example.client.service.SessionService    : Added attributes to Session[id=48; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:46:32.744  INFO 64770 --- [           main] example.client.service.SessionService    : Retrieved Session[id=49; attributes={}]
2021-03-20 07:46:32.809  INFO 64770 --- [           main] example.client.service.SessionService    : Added attributes to Session[id=49; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
```
### Update Entries (Remove Attributes)
Sample output from the *runclient.sh* script is:

```
./runclient.sh remove-attributes 50

2021-03-20 07:48:32.469  INFO 64789 --- [           main] example.client.Client                    : Starting Client ...
...
2021-03-20 07:48:37.172  INFO 64789 --- [           main] example.client.Client                    : Started Client in 5.214 seconds (JVM running for 5.724)
2021-03-20 07:48:37.175  INFO 64789 --- [           main] example.client.service.SessionService    : Removing attributes from 50 sessions
2021-03-20 07:48:37.209  INFO 64789 --- [           main] example.client.service.SessionService    : Retrieved Session[id=0; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:48:37.241  INFO 64789 --- [           main] example.client.service.SessionService    : Removed attributes from Session[id=0; attributes={attr3=attr3_value}]
2021-03-20 07:48:37.254  INFO 64789 --- [           main] example.client.service.SessionService    : Retrieved Session[id=1; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:48:37.261  INFO 64789 --- [           main] example.client.service.SessionService    : Removed attributes from Session[id=1; attributes={attr3=attr3_value}]
2021-03-20 07:48:37.264  INFO 64789 --- [           main] example.client.service.SessionService    : Retrieved Session[id=2; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:48:37.275  INFO 64789 --- [           main] example.client.service.SessionService    : Removed attributes from Session[id=2; attributes={attr3=attr3_value}]
2021-03-20 07:48:37.277  INFO 64789 --- [           main] example.client.service.SessionService    : Retrieved Session[id=3; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:48:37.290  INFO 64789 --- [           main] example.client.service.SessionService    : Removed attributes from Session[id=3; attributes={attr3=attr3_value}]
2021-03-20 07:48:37.298  INFO 64789 --- [           main] example.client.service.SessionService    : Retrieved Session[id=4; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:48:37.309  INFO 64789 --- [           main] example.client.service.SessionService    : Removed attributes from Session[id=4; attributes={attr3=attr3_value}]
...
2021-03-20 07:48:37.713  INFO 64789 --- [           main] example.client.service.SessionService    : Retrieved Session[id=45; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:48:37.720  INFO 64789 --- [           main] example.client.service.SessionService    : Removed attributes from Session[id=45; attributes={attr3=attr3_value}]
2021-03-20 07:48:37.721  INFO 64789 --- [           main] example.client.service.SessionService    : Retrieved Session[id=46; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:48:37.733  INFO 64789 --- [           main] example.client.service.SessionService    : Removed attributes from Session[id=46; attributes={attr3=attr3_value}]
2021-03-20 07:48:37.734  INFO 64789 --- [           main] example.client.service.SessionService    : Retrieved Session[id=47; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:48:37.740  INFO 64789 --- [           main] example.client.service.SessionService    : Removed attributes from Session[id=47; attributes={attr3=attr3_value}]
2021-03-20 07:48:37.742  INFO 64789 --- [           main] example.client.service.SessionService    : Retrieved Session[id=48; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:48:37.750  INFO 64789 --- [           main] example.client.service.SessionService    : Removed attributes from Session[id=48; attributes={attr3=attr3_value}]
2021-03-20 07:48:37.751  INFO 64789 --- [           main] example.client.service.SessionService    : Retrieved Session[id=49; attributes={attr2=attr2_value, attr1=attr1_value, attr3=attr3_value}]
2021-03-20 07:48:37.758  INFO 64789 --- [           main] example.client.service.SessionService    : Removed attributes from Session[id=49; attributes={attr3=attr3_value}]
```
### Destroy Entries
Sample output from the *runclient.sh* script is:

```
./runclient.sh destroy 50  

2021-03-20 07:51:54.382  INFO 64836 --- [           main] example.client.Client                    : Starting Client ...
...
2021-03-20 07:51:58.830  INFO 64836 --- [           main] example.client.Client                    : Started Client in 4.969 seconds (JVM running for 5.443)
2021-03-20 07:51:58.834  INFO 64836 --- [           main] example.client.service.SessionService    : Destroying 50 sessions
2021-03-20 07:51:58.916  INFO 64836 --- [           main] example.client.service.SessionService    : Destroyed session key=0
2021-03-20 07:51:58.923  INFO 64836 --- [           main] example.client.service.SessionService    : Destroyed session key=1
2021-03-20 07:51:58.944  INFO 64836 --- [           main] example.client.service.SessionService    : Destroyed session key=2
2021-03-20 07:51:58.947  INFO 64836 --- [           main] example.client.service.SessionService    : Destroyed session key=3
2021-03-20 07:51:58.950  INFO 64836 --- [           main] example.client.service.SessionService    : Destroyed session key=4
...
2021-03-20 07:51:59.090  INFO 64836 --- [           main] example.client.service.SessionService    : Destroyed session key=45
2021-03-20 07:51:59.094  INFO 64836 --- [           main] example.client.service.SessionService    : Destroyed session key=46
2021-03-20 07:51:59.098  INFO 64836 --- [           main] example.client.service.SessionService    : Destroyed session key=47
2021-03-20 07:51:59.106  INFO 64836 --- [           main] example.client.service.SessionService    : Destroyed session key=48
2021-03-20 07:51:59.117  INFO 64836 --- [           main] example.client.service.SessionService    : Destroyed session key=49
```
### Shutdown Locator and Servers
Sample output from the *shutdownall.sh* script is:

```
./shutdownall.sh 

(1) Executing - connect

Connecting to Locator at [host=localhost, port=10334] ..
Connecting to Manager at [host=192.168.1.11, port=1099] ..
Successfully connected to: [host=192.168.1.11, port=1099]


(2) Executing - shutdown --include-locators=true

Shutdown is triggered
```
