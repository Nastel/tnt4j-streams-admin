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
