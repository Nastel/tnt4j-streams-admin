
- [Deployment package](#deployment-package)
  - [Streams folder](#streams-folder)
  - [Streams admin jars placement](#streams-admin-jars-placement)
  - [Logging](#logging)
  - [Streams admin json config](#streams-admin-json-config)
  - [Adding replay capabilities](#adding-replay-capabilities)
  - [Hooking up](#hooking-up)
  - [System properties](#system-properties)
  - [JETTY SSL Setup](#jetty-ssl-setup)
  - [Stream admin configuration](#stream-admin-configuration)
  - [On correct setup](#on-correct-setup)

# Deployment package

## Streams folder

Deployed tnt4j-streams project  

![image](../screenshots/deploymentPackage/tnt4j-streams.png)

## Streams admin jars placement

The following modules

- tnt4j-streams-admin-registry

- tnt4j-streams-admin-hc

- tnt4j-streams-admin-utils

Should have the following jars

- tnt4j-streams-admin-utils-1.10.0-SNAPSHOT.jar

- tnt4j-streams-admin-registry-1.10.0-SNAPSHOT.jar

- tnt4j-streams-admin-hc-1.10.0-SNAPSHOT.jar

and should be placed in the following folder

![image](../screenshots/deploymentPackage/pointerToLib.png)

![image](../screenshots/deploymentPackage/pointersToJars.png)  

## Logging

The following logging appender configuration

```properties
### StreamsAdmin activities logger ###
log4j.appender.streamsAdmin_error=org.apache.log4j.RollingFileAppender
log4j.appender.streamsAdmin_error.File=${log4j.logs.root.path}/streamsAdmin_error.log
log4j.appender.streamsAdmin_error.maxFileSize=10MB
log4j.appender.streamsAdmin_error.maxBackupIndex=1
log4j.appender.streamsAdmin_error.layout=org.apache.log4j.EnhancedPatternLayout
log4j.appender.streamsAdmin_error.layout.ConversionPattern={"date" : "%d{ISO8601}", %m} %n
log4j.appender.streamsAdmin_error.Threshold=INFO

log4j.logger.streamsAdmin_error=INFO, streamsAdmin_error

log4j.appender.streamsAdminRestLog=org.apache.log4j.RollingFileAppender
log4j.appender.streamsAdminRestLog.File=${log4j.logs.root.path}/streamsAdminRestLog.log
log4j.appender.streamsAdminRestLog.maxFileSize=10MB
log4j.appender.streamsAdminRestLog.maxBackupIndex=1
log4j.appender.streamsAdminRestLog.layout=org.apache.log4j.EnhancedPatternLayout
log4j.appender.streamsAdminRestLog.layout.ConversionPattern={"date" : "%d{ISO8601}", %m} %n
log4j.appender.streamsAdminRestLog.Threshold=INFO
log4j.logger.streamsAdminRestLog=INFO, streamsAdminRestLog
```

Should be placed

![image](../screenshots/deploymentPackage/pointerToLogs.png)

![image](../screenshots/deploymentPackage/pointerToLog4j.png)

## Streams admin json config

streamsAdmin.json config should be placed

![image](../screenshots/deploymentPackage/pointerToConfig.png)

![image](../screenshots/deploymentPackage/pointerToStreamsAdminDir.png)

![image](../screenshots/deploymentPackage/pointerToStreamsAdminEth.png)

![image](../screenshots/deploymentPackage/pointerToStreamsAdminCfg.png)

## Adding replay capabilities

go to samples

![image](../screenshots/deploymentPackage/pointerToSamples.png)

create a folder named templates, place all your reply configs in there

![image](../screenshots/deploymentPackage/pointerToTemplatesDir.png)

and create a folder named userRequests.

![image](../screenshots/deploymentPackage/pointerToUserRequests.png)

## Hooking up

![image](../screenshots/deploymentPackage/pointerToSamples.png)

Depending on what you're streaming, in my case it is ethereum

![img](../screenshots/deploymentPackage/pointerToEthereumSamples.png)

Open your main xml config

![img](../screenshots/deploymentPackage/pointerToEthInfuraCfg.png)

and add the following line

![img](../screenshots/deploymentPackage/streamsAdminHookXmlCfg.png)

## System properties

Go to registry editor

![img](../screenshots/deploymentPackage/regeditProps.png)

Set the following jvm properties

- streamsAdmin
  
example

-DstreamsAdmin="C:\k2\services\tnt4j-streams-k2-1.0.16\config\streamsAdmin\eth\streamsAdmin.properties"

streamsAdmin should point to the json config

![img](screenshots/deploymentPackage/pointerToConfig.png)

![img](screenshots/deploymentPackage/pointerToStreamsAdminDir.png)

![img](screenshots/deploymentPackage/pointerToStreamsAdminEth.png)

![img](screenshots/deploymentPackage/pointerToStreamsAdminCfg.png)

## JETTY SSL Setup

Place your jks keystore

![img](../screenshots/deploymentPackage/pointerToConfig.png)

![img](../screenshots/deploymentPackage/pointerToStreamsAdminDir.png)

![img](../screenshots/deploymentPackage/pointerToStreamsAdminJksStore.png)

and set the path, login, pass for the keystore in streams admin json config

![img](../screenshots/deploymentPackage/streamsAdminCfgKeystoreProps.png)

## Stream admin configuration

![img](../screenshots/deploymentPackage/pointerToStreamsAdminCfg.png)

Open json

![img](../screenshots/deploymentPackage/configPaths.png)

set logs folder

![img](../screenshots/deploymentPackage/pointerToLogs.png)

set sample configs folder

![img](../screenshots/deploymentPackage/pointerToSamples.png)

![img](../screenshots/deploymentPackage/pointerToEthereumSamples.png)

set main config xml

![img](../screenshots/deploymentPackage/pointerToSamples.png)

![img](../screenshots/deploymentPackage/pointerToEthereumSamples.png)

![img](../screenshots/deploymentPackage/pointerToEthInfuraCfg.png)

set replay

![img](../screenshots/deploymentPackage/pointerToReplayXmlFile.png)

set user requests folder

![img](../screenshots/deploymentPackage/pointerToUserRequests.png)

set libraries path

![img](../screenshots/deploymentPackage/pointerToLib.png)

## On correct setup

![img](../screenshots/deploymentPackage/pointerToLogs.png)

![img](../screenshots/deploymentPackage/pointersToStreamsAdminLogFiles.png)

![img](../screenshots/deploymentPackage/logsOutput.png)

After the above steps are done stream admin should be ready to launch,

Start your streaming service as usual and streams admin client should fire up also

Upon start error log should contain 2 entries ( 1 entry if you haven not set dir streaming )

```json
{"date" : "2019-08-09 14:15:06,864", Starting} 
{"date" : "2019-08-09 14:15:07,129", starting dir streaming} 
```

When receiving a request and when logging is turned on, rest log should contain similar entries

```json
{"date" : "2019-08-09 14:15:08,714", Request[GET https://172.16.6.51:8899/streamsAgent]@68af8929}
{"date" : "2019-08-09 14:15:10,024", Request[GET https://172.16.6.51:8899/streamsAgent]@67e81b36}
{"date" : "2019-08-09 14:16:40,304", Request[GET https://172.16.6.51:8899/streamsAgent/EthereumInfuraStream2]@13301c10}
{"date" : "2019-08-09 14:16:40,320", Request[GET https://172.16.6.51:8899/streamsAgent/threadDump]@4ee5e1eb}
{"date" : "2019-08-09 14:16:40,330", Request[GET https://172.16.6.51:8899/streamsAgent/samples]@4da6d871}
{"date" : "2019-08-09 14:16:40,364", Request[GET https://172.16.6.51:8899/streamsAgent/logs]@5fbd5fdd}
{"date" : "2019-08-09 14:16:40,480", Request[GET https://172.16.6.51:8899/streamsAgent]@28f37705}
```
