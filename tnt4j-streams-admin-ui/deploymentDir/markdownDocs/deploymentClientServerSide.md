# Deployment of client and server
## Preparing the deployment directory
1. The example of the full deployment can be found inside the [deployment directory](/tree/master/tnt4j-streams-admin-ui/deploymentDir/exampleDeployDir) .
2. After building the ui-web project create the deployment package and copy the content of **\tnt4j-streams-admin-ui\tnt4j-streams-admin-ui-web\src\main\web\dist** folder into the deployment packages clientSide folder.
3. For the ui-server build  copy the .war file from **\tnt4j-streams-admin-ui\tnt4j-streams-admin-ui-server\target** and paste it into the serverSide folder. 
4. Rename the file from the previous step to "streamsadminservices" .
5. Copy the configuration files from the [deploymentDir](/tnt4j-streams-admin-ui/deploymentDir/serverSideConfigurationFiles) to serverSide deployment folder.
6. The final deployment directory contains two folders for deployment the
    ***serverSide*** and ***clientSide*** folders.
![](streamsFront/serverSide/1.jpg)
## Deployment server side: 

### Step 1

-   Go to **serverSide** directory and open the file  named **streamsProperties.properties** set the property named ***ZooKeeperAddress*** to the ZooKeeper ip that we will be connecting to.
-   Set the secret key to match the one that you got from Google reCaptcha.

|  Parameter name   |   Value/description/example |  Explanation |
| --- | --- | --- | --- |
|  ZooKeeperAddress  |  172.31.39.60:2181    |                                                            The ZooKeeper ip address used for connecting to ZooKeeper|
|  secretKeyCaptcha |  6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe*, or any other received from Google.*  | Should be used depending on domain|
![](streamsFront/serverSide/2.jpg)
![](streamsFront/serverSide/3.jpg)

-   Copy the **streamsProperties.properties** from server side deployment directory to ***c:\\Program Files\\Apache Software  Foundation\\Tomcat 9.0\\conf\\*** directory.

### Step 2

-   Copy the **jaas.config** from server side deployment directory 
    to **c:\\k2\\jass.config** directory.
-   If needed open and change the **setenv.bat** file property path to
    match the one that was used in the previous step.
    ![](streamsFront/serverSide/4.jpg)
-   Copy the **setenv.bat** from server side deployment directory to
    ***c:\\Program Files\\Apache Software Foundation\\Tomcat
    9.0\\bin\\.***

### Step 3

##### Adding the SSL certificate for communication with ZooKeeper:

1.  Navigate to tomcat Java home directory.
2.  Open the ***\\bin*** folder and run the command window inside it.
3.  Call the command to import the certificate to the default keystore:  
`keytool -import -alias certAlias -keystore ..\\lib\\security\\cacerts -file FileName`
4.  **-alias certAlias** - a unique name given to the certificate could be changed.
5.  **FileName** - is the full path to the **zooKeeperCert** file that is saved inside the server side deployment folder.

 **e.g.**  `keytool -import -alias zooKeeperCert -keystore ..\\lib\\security\\cacerts -file ..\\lib\\security\\zooKeeperCert`
 
7.   ***Optional:*** if custom keystore will be used:
      - Open the **streamsProperties.properties** file and set the path to custom trustStore: 
        `SslConfigFilePath = C:\\\\development\\\\apache-tomcat-9.0.14\\\\conf\\\\streamsAdmin.jks`
       - Set the trustStore password:
        `SslPass = passPhrase`
        
### Step 4 

-   Copy the **streamsadminservices.war** from server side deployment folder to ***c:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\webapps\\*** directory. Now you should be able to access the API.

- Here are a few example calls with responses that you should get:

| Example URL                           | Response example             |
| --- | --- |
| <https://gocypher.com/streamsadminservices/registry/nodeTree> | Tried to access protected resources |
| <https://gocypher.com/streamsadminservices/registry> | Streams Services Registry endpoint | 

## Deployment client side:

1.   Inside the client side deployment directory go to **clientSide\streamsadmin\assets** folder and open the **configuration.json** file. Update the parameters found in the table below:

| Parameter name | Value/description/example | Explanation           |
| -- | -- | -- |
| *siteKey*             | _6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI, or any other received from Google._ |   Should be changed to the needed domain   |
| ZooKeeperTreNodes     | /registry/nodeTree | Used to get the main node tree   |
| ZooKeeperBasePath     | /registry | Used to get all the data from nodes and preform stream actions|
| LoginRequestPath      | /authenticate         | Used for user management  |
| reCaptchaRequest      | /reCaptcha            | Used for captcha verification |
| BaseAddress           | [https://www.gocypher.com/streamsadminservices](https://gocypher.com/streamsadminservices/reCaptcha) | [gocypher.com](http://gocypher.com/) should be changed to the needed domain |
2. The properties inside the configuraiton.json file that needs to be changed:
![](streamsFront/serverSide/5.jpg)
3.  Copy the ***streamsadmin*** folder  from client side deployment directory to ***c:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\webapps\\*** directory.
