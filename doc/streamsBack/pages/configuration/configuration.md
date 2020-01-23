# Configuration

- [Configuration](#configuration)
  - [Variables](#variables)
  - [Jetty](#jetty)
  - [Zookeeper](#zookeeper)
  - [Connection](#connection)
  - [Agent nodes](#agent-nodes)
    - [Simple node](#simple-node)
    - [Advanced node](#advanced-node)
  - [Stream nodes](#stream-nodes)
    - [Simple node](#simple-node-1)
    - [Advanced stream node](#advanced-stream-node)
  - [Paths](#paths)
  - [Template](#template)

Streams admin configurations mainly consists of 4 JSON objects (sections):

**variables** - variables that can be used in this config to avoid duplicates

**jetty** - define jetty settings

**zookeeper** - define nodes and what to put in them

**paths** - define paths to various files

This is minimalist config that is valid, it's main purpose is to serve as an example that will be broken down and explained later

```json

{
  "variables": {
    "port": 7000,
    "baseUrl": "https://111.11.1.11:7000",
    "baseZk": "/streams/clusters"
  },
  "jetty": {
    "port": 7000,
    "securePort": 7000,
    "keyStorePath": "C:/demo.jks",
    "keyStorePassword": "demo",
    "keyManagerPassword": "demo"
  },
  "zookeeper": {
    "connectString": "127.0.0.1",
    "zklogin": "login",
    "zkpass": "password",
    "agentNodes": [
      {
        "name": "root",
        "path": "/streams"
      },
      {
        "name": "clusters",
        "path": "/streams/clusters",
        "config": {
          "componentLoad": "clusters",
          "streamsIcon": "<svg id='Layer_1' data-name='Layer 1' height='1.8em' xmlns='http://www.w3.org/2000/svg' viewBox='0 0 101 101'><title>log_Artboard 2 copy 6</title><path"
        }
      },
      {
        "name": "agentThreadDump",
        "path": "${baseZk}/streamsAgentEth/threadDump",
        "config": {
          "componentLoad": "thread",
          "streamsIcon": "<svg id='Layer_1' data-name='Layer 1' height='1.8em' xmlns='http://www.w3.org/2000/svg' viewBox='0 0 101 101'><title>log_Artboard 2 copy 6</title><path"

        },
        "data": "${baseUrl}/streamsAgent/threadDump"
      },
      {
        "name": "metrics",
        "path": "${baseZk}/streamsAgentEth/metrics",
        "myField1": "myValue",
        "myField2": "myValue",
        "myField3": "myValue",
        "myObject": {
          "myInnerValue": "myInnerValue"
        }
      }
    ],
    "streamNodes": [
      {
        "name": "stream",
        "path": "${baseZk}/streamsAgentEth/${streamName}",
        "config": {
          "componentLoad": "service",
          "showBottomLog": "pathToBottomLogsNode",
          "streamsIcon": "<svg id='Layer_1' data-name='Layer 1' height='1.8em' xmlns='http://www.w3.org/2000/svg' viewBox='0 0 101 101'><title>log_Artboard 2 copy 6</title><path"
        },
        "data": "${baseUrl}/streamsAgent/${streamName}"
      },
      {
        "name": "streamReplay",
        "path": "/streams/v1/clusters/clusterBlockchainMainnets/streamsAgentEth/${streamName}/_replay",
        "data": "${baseUrl}/streamsAgent/${streamName}/replay?b="
      }
    ]
  },
  "paths": {
    "logsPath": "C:/development/tnt4j/k2/development/k2-index-search/tnt4j-streams-k2/logs/replay",
    "sampleCfgsPath": "C:/development/tnt4j/k2/development/k2-index-search/tnt4j-streams-k2/samples/ethereum",
    "mainConfigPath": "C:/development/tnt4j/k2/development/k2-index-search/tnt4j-streams-k2/samples/ethereum/ethereumInfuraReplay.xml",
    "replayTemplatePath": "C:/development/tnt4j/k2/development/k2-index-search/tnt4j-streams-k2/samples/templates/ethereumInfuraReplay.xml",
    "monitoredPath": "C:/development/tnt4j/k2/development/k2-index-search/tnt4j-streams-k2/samples/templates/userRequests"
  }
}

```

## Variables

Variables section allows us to setup variables that can be used in the config.

The main purpose of this feature is to avoid duplicate fields

To create a **variable** you need to define a field in the variables section

```json

"variables" : {
  "port" : "4444",
  "name" : "zookeeper",
  "number" : 123
}

```

after declaring your field in the variables body you can use it like this **${port}** , **${name}** , **${number}**

| Warning!|
| ------- |
| Variable name **streamName** is reserved and should be never declared |

| Notice!|
| ------ |
| Variables are not mandatory |


## Jetty

Jetty section allows us to setup values to the following fields

**port** - sets port

**securePort** - sets secure port

**keyStorePath** - path to  keystore

**keyStorePassword** - password to the keystore

**keyManagerPassword** - password to the keystores private key

| Notice!|
| ------ |
| Mentioned fields are mandatory and should have valid values |

e.g.

```json

"jetty":  {
     "port": 7000,
     "securePort": 7000,
     "keyStorePath": "C:/demo.jks",
     "keyStorePassword": "demo",
     "keyManagerPassword": "demo"
}

```

## Zookeeper

Zookeeper section is the biggest of all 4. Zookeeper section consists of sub sections: **connection**, **agentNodes**, **streamNodes**

## Connection

**connectString** - IP:PORT pointing to zookeeper server e.g 172.11.55.66:2128

**zklogin** -login that stream will use to authenticate to zookeeper

**zkpass** - password that stream will use to authenticate to zookeeper

| Notice!|
| ------ |
| Mentioned fields are mandatory and should have valid values |

e.g.

```json

"zookeeper": {
    "connectString": "127.0.0.1",
    "zklogin": "login",
    "zkpass": "password",

```

```json

"zookeeper": {
    "connectString": "127.0.0.1:2288",
    "zklogin": "login",
    "zkpass": "password",

```

## Agent nodes

Agent nodes is a JSON array consisting of objects that define a node in zookeeper and what will put into it

| Notice!|
| ------ |
| Stream admin executes agent nodes with the order provided |

### Simple node

This is most simplest node that can be made. 

It consists of 2 **mandatory fields**: **name** and **path** and puts no data into the node

```json

"agentNodes": [
{
   "name": "root",
   "path": "/streams"
}

```

### Advanced node

It consists of 2 **mandatory fields**: **name** and **path**

```json

{
       "name": "metrics",
       "path": "${baseZk}/streamsAgentEth/metrics",
       "myField1": "myValue",
       "myField2": "myValue",
       "myField3": "myValue",
       "myObject": {
            "myInnerValue": "myInnerValue"
       }
}

```

However, everything that is under mandatory field **path** is put into the node

This part will be put into a HashMap and  it's content will be marshalled to Json string and put into the node that was specified earlier with **name** and **path**

```json

"myField1": "myValue",
       "myField2": "myValue",
       "myField3": "myValue",
       "myObject": {
            "myInnerValue": "myInnerValue"
       }

```

This is how it looks inside a node after being marshalled and put

```json

{
      "myField1": "myValue",
       "myField2": "myValue",
       "myField3": "myValue",
       "myObject": {
            "myInnerValue": "myInnerValue"
       }

}

```

## Stream nodes

Stream nodes is a JSON array consisting of objects that define a node in zookeeper and what will put into it

| Notice!|
| ------ |
| Stream nodes contain a variable **${streamName}** it is used internally by streams admin client to dynamically create endpoints and nodes for all streams that are inside main streams config

 
| Notice!|
| ------ |
| Stream admin executes stream nodes with the order provided |


### Simple node

This is most simplest node that can be made. 

It consists of **2 mandatory fields**: **name** and **path**

```json

{
   "name": "streamReplay",
   "path": "/streams/v1/clusters/clusterBlockchainMainnets/streamsAgentEth/${streamName}/_replay",
   "data": "${baseUrl}/streamsAgent/${streamName}/replay?b="
}

```

However, everything that is under mandatory field **path** is put into the node

This part will be put into a HashMap and  it's content will be marshalled to Json string and put into the node that was specified earlier with **name** and **path**

### Advanced stream node

It consists of **2 mandatory fields**: **name** and **path**

```json

 "streamNodes": [ 
  {
      "name": "stream",
      "path": "${baseZk}/streamsAgentEth/${streamName}",
      "config": {
         "componentLoad": "service",
         "showBottomLog": "pathToBottomLogsNode",
         "streamsIcon": "<svg id='Layer_1' data-name='Layer 1' height='1.8em' xmlns='http://www.w3.org/2000/svg' viewBox='0 0 101 101'><title>log_Artboard 2 copy 6</title><path"
        },
       "data": "${baseUrl}/streamsAgent/${streamName}"
  }

```

However, everything that is under mandatory field **path** is put into the node

This part will be put into a HashMap and  it's content will be marshalled to Json string and put into the node that was specified earlier with **name** and **path**

```json

 "config": {
         "componentLoad": "service",
         "showBottomLog": "pathToBottomLogsNode",
         "streamsIcon": "<svg id='Layer_1' data-name='Layer 1' height='1.8em' xmlns='http://www.w3.org/2000/svg' viewBox='0 0 101 101'><title>log_Artboard 2 copy 6</title><path"
        },
       "data": "${baseUrl}/streamsAgent/${streamName}"

```

This is how it looks inside a node after being marshalled and put

```json

{

   "data":"https://11.12.33.44:8899/streamsAgent/${streamName}",  
   "config":{
        "componentLoad": "service",
        "showBottomLog":"pathToBottomLogsNode",
        "streamsIcon":"<svg id='Layer_1' data-name='Layer 1' height='1.8em' xmlns='http://www.w3.org/2000/svg' viewBox='0 0 101 101'><title>log_Artboard 2 copy 6</title><path"

      }
}

```

## Paths

**Paths** section allows us to setup values to the following fields,

**logsPath** - path to logs folder

**sampleCfgsPath** -path to folder containing ,xml parsers

**mainConfigPath** -path to main streams .xml file

**replayTemplatePath** - path to replay file template

**monitoredPath** - path where dir streaming searches for new streaming files, this field is needed only if you have a template and tend to allow to replay blocks

| Notice!|
| ------ |
| Mentioned fields are mandatory and should have valid values, **except for monitoredPath if you do not want such capability you need to specify the values as**: "" |

e.g.

```json

"paths": {
    "logsPath": "C:/development/tnt4j/k2/development/k2-index-search/tnt4j-streams-k2/logs/replay",
    "sampleCfgsPath": "C:/development/tnt4j/k2/development/k2-index-search/tnt4j-streams-k2/samples/ethereum",
    "mainConfigPath": "C:/development/tnt4j/k2/development/k2-index-search/tnt4j-streams-k2/samples/ethereum/ethereumInfuraReplay.xml",
    "replayTemplatePath": "C:/development/tnt4j/k2/development/k2-index-search/tnt4j-streams-k2/samples/templates/ethereumInfuraReplay.xml",
    "monitoredPath": "C:/development/tnt4j/k2/development/k2-index-search/tnt4j-streams-k2/samples/templates/userRequests"
}

```

## Template

```json

{
  "variables": { },
  "jetty": {
    "port": -1,
    "securePort": -1,
    "keyStorePath": "",
    "keyStorePassword": "",
    "keyManagerPassword": ""
  },
  "zookeeper": {
    "connectString": "",
    "zklogin": "",
    "zkpass": "",
    "agentNodes": [ ],
    "streamNodes": [ ]
  },
  "paths": {
    "logsPath": "",
    "sampleCfgsPath": "",
    "mainConfigPath": "",
    "replayTemplatePath": "",
    "monitoredPath": ""
  }
}

```
