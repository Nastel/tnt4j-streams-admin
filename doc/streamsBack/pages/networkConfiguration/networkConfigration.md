# Network configuration

A simplified view of how StreamsAdminUI communicates with other components

![img](../../screenshots/networkConfiguration/sequence.png)

There are 3 components

- streamsAdminUi
- Zookeeper server
- StreamsAdminClient

Zookeeper must have an open port (by default it is 2181) to listen for incoming connections from StreamsAdminUI and StreamsAdminClient

StreamAdminClient must have an open port for each instance of StreamsAdminClient

A top down view of our components

![img](../../screenshots/networkConfiguration/network.png)

Explanation:

The components from which the line starts from are the components that send requests and the components on which the arrow ends listen to requests

Listeners must have a open port to listen to requests

How to configure StreamsAdminClient network properties can be found at Configuration

How to configure ZookeeperServer network properties can be found at Zookeeper configuration and deployment