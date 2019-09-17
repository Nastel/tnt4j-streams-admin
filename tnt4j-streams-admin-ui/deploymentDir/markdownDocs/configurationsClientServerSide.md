## Client side:

|Nr.|Value|Explanation|Example/Value|
|--|--|--|--|
| **1.** | StreamsIcon|Used for registering icons that should be loaded from assets. <br> The format is: _name used to call icon inside UI : relative path to icon| `"StreamsIcon": {  "mainPage": "assets/icons/headerLogo.svg", `![](https://info.singleton-labs.lt/download/attachments/36929901/image2019-9-13_11-26-59.png?version=1&modificationDate=1568363217000&api=v2) 
|**2.** |StreamsData- ForAgentPage | Inside agent page, some of the properties are shown inside the table and other in the expanding component. Here are the properties that should be shown inside the table with their default values.<br> The format is: _Id in data object :( name : default value )| `"StreamsDataForAgentPage": {  "latest block" : {"Last Streamed": "N/A"},`![](https://info.singleton-labs.lt/download/attachments/36929901/image2019-9-13_11-28-19.png?version=1&modificationDate=1568363297000&api=v2)
| **3.** | StreamsDataFor- ClusterPage | Inside cluster page, only some of the stream properties are shown. Here are the properties that should be shown inside the table with their default values.<BR> The format is: _Id in data object : ( name : default value ) | `"StreamsDataForClusterPage": {  "latest block" : {"last streamed": "N/A"},`![](https://info.singleton-labs.lt/download/attachments/36929901/image2019-9-13_11-32-34.png?version=1&modificationDate=1568363552000&api=v2)
| **4.** | ReposDataFor- AllPage |Inside the repository view. The properties that should be shown inside the table with their default values.<br>The format is: _Id in data object : ( name : default value )| `"ReposDataForAllPage": {  "rowCount" :{"Total streams" : 0},`![](https://info.singleton-labs.lt/download/attachments/36929901/image2019-9-13_11-54-38.png?version=1&modificationDate=1568364876000&api=v2)
|**5.**|StreamsData- Formatting|The fields from data objects that need to be formatted to show time in correct format.<br>The format is: _Id in data object : time| `"StreamsDataFormatting": {  "LastBlockQueryTime" : "time",`![](https://info.singleton-labs.lt/download/attachments/36929901/image2019-9-13_11-40-41.png?version=1&modificationDate=1568364039000&api=v2)
| **6.** |NeededData- ToGetJKool-IncompleteBlocks | Inside incomplete items page needed properties to be shown from incomplete blocks object got from JKool and other properties for table data.<br> The format is: _Id in data object of 1 depth|` "NeededDataToGetJKool- IncompleteBlocks" : [  {"Properties('txCount')" "txCount"},  "linkToBlock"`<br>![](https://info.singleton-labs.lt/download/attachments/36929901/image2019-9-13_11-58-11.png?version=1&modificationDate=1568365088000&api=v2)
|**7.**|NeededData- ToGetJKool- Incomplete-Blocks- NoReceipt |Inside incomplete items page needed properties to be shown from incomplete blocks no receipt (for etherium) object got from JKool and other properties for table data.| `"NeededDataTo- GetJKoolIncomplete- BlocksNoReceipt" : [ "Count(EventName)",`![](https://info.singleton-labs.lt/download/attachments/36929901/image2019-9-13_11-59-49.png?version=1&modificationDate=1568365187000&api=v2)
| **8.** |LazyLoad- DataLines|Properties used to configure the amount of shown logs. "log" - how many items from log shown on load more or initial load.<br> "logErr" - the same but for error logs. "bootomLog" - how many lines will be displayed in the bottom log component. "bottomLogLoadIntervalMS" - at what intervals the bottom log data will be updated. |` "LazyLoadDataLines" : {  "log" : 100,  "logErr" : 50,`![](https://info.singleton-labs.lt/download/thumbnails/36929901/image2019-9-13_12-29-55.png?version=1&modificationDate=1568366993000&api=v2)
| **9.** | StartTreeNode- ParentName| On tree view the first node has no parent node so the value BaseNode is set instead. | ` "StartTreeNodeParentName" : "BaseNode", `![](https://info.singleton-labs.lt/download/thumbnails/36929901/image2019-9-13_12-40-25.png?version=1&modificationDate=1568367623000&api=v2)
|**10.**|BasePathHide|All the paths inside the node tree start with the same root that is not shown in node tree directly. That is why it is being hidden for all paths and requests and the provided parameter here shows the hidden path.|`"BasePathHide" : "streams/v1",`![](https://info.singleton-labs.lt/download/thumbnails/36929901/image2019-9-13_12-40-46.png?version=1&modificationDate=1568367644000&api=v2)
|**11.**|BasePathToUsersPage|The path property used to register the angular URL for user management and access/redirect to it when needed|`"BasePathToUsersPage" : "manageUser",`![](https://info.singleton-labs.lt/download/attachments/36929901/image2019-9-13_12-43-17.png?version=1&modificationDate=1568367795000&api=v2)
|**12.**|LogToShowAtBottom|Since more that one type of log is being sent on request, the choice for the log that needs to be shown on the bottom is made with this property.|`"LogToShowAtBottom" : "Service log",`![](https://info.singleton-labs.lt/download/thumbnails/36929901/image2019-9-13_12-43-30.png?version=1&modificationDate=1568367808000&api=v2)
|**13.**|activeStream- RegistryNode|A special node used to get all the streams inside an agent with their metrics data, used for cluster, and agent pages to make less calls to back-end.|`"activeStreamRegistryNode" : "_streamsAndMetrics",`![](https://info.singleton-labs.lt/download/attachments/36929901/image2019-9-13_12-43-45.png?version=1&modificationDate=1568367823000&api=v2)
|**14.**|sessionTokenName|A property used inside the session storage to set the token value key.|`"sessionTokenName" : "authToken",`![](https://info.singleton-labs.lt/download/thumbnails/36929901/image2019-9-13_12-54-2.png?version=1&modificationDate=1568368440000&api=v2)
|**15.**|sessionUserName|A property used inside the session storage to set the username value key.|`"sessionUserName" : "username",`![](https://info.singleton-labs.lt/download/thumbnails/36929901/image2019-9-13_12-54-24.png?version=1&modificationDate=1568368462000&api=v2)
|**16.**|UserManagement- Form_add|Used to fill the form with appropriate properties when add user option is chosen.|`"UserManagementForm_add" : {  "FormName" : "Register new user",`![](https://info.singleton-labs.lt/download/thumbnails/36929901/image2019-9-13_12-57-44.png?version=1&modificationDate=1568368661000&api=v2)
|**17.**|UserManagement- Form_info|Used to fill the form with appropriate properties when information option is chosen.|`"UserManagementForm_info" : {  "FormName" : "User profile",`![](https://info.singleton-labs.lt/download/thumbnails/36929901/image2019-9-13_12-59-17.png?version=1&modificationDate=1568368755000&api=v2)
|**18.**|UserManagement- Form_remove|Used to fill the form with appropriate properties when remove user option is chosen.|`"UserManagementForm_remove" : {  "FormName" : "Remove user",`![](https://info.singleton-labs.lt/download/thumbnails/36929901/image2019-9-13_12-59-53.png?version=1&modificationDate=1568368791000&api=v2)
|**19.**|UserManagement- Form_edit|Used to fill the form with appropriate properties when edit user option is chosen.|`"UserManagementForm_edit" : {  "FormName" : "Edit user data",`![](https://info.singleton-labs.lt/download/attachments/36929901/image2019-9-13_13-0-18.png?version=1&modificationDate=1568368816000&api=v2)
|**20.**|SecretKeyFor- Encryption|A property used to encrypt password before send. The property set inside the server side configuration must mach in order for the decryption to work correctly.|`"SecretKeyForEncryption" : "exampleKey123",`![](https://info.singleton-labs.lt/download/attachments/36929901/image2019-9-13_13-4-23.png?version=1&modificationDate=1568369061000&api=v2)
|**21.**|ZooKeeperTreeNodes|Server side API call to get the node tree list.|`"ZooKeeperTreeNodes" : "/registry/nodeTree",`
|**22.**|ZooKeeperBasePath|Server side API call to get the node data or apply needed actions (start, stop, replay).|`"ZooKeeperBasePath" : "/registry",`
|**23.**|LoginRequestPath|Server side API call to reach the needed endpoints for users management.|`"LoginRequestPath" : "/authenticate",`
|**24.**|reCaptchaRequest|Server side API call to get the node tree list.|`"reCaptchaRequest" : "/reCaptcha",`
|**25.**|BaseAddress|Server side API base path used for all calls to API.|`"BaseAddress" : "https://www.gocypher.com/streamsadminservices",`
|**26.**|siteKey|Site key property that is used for reCaptcha component verification with Google. The provided site key is for development purposes. Valid site key needs to be registered with Google.|`"siteKey" : 6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI",`![](https://info.singleton-labs.lt/download/attachments/36929901/image2019-9-13_13-7-29.png?version=1&modificationDate=1568369247000&api=v2)
|**27.**|ZooKeeperDataCall|Server side API calls used for needed actions (start, stop, replay).|`"ZooKeeperDataCall" : [  "/list",`![](https://info.singleton-labs.lt/download/thumbnails/36929901/image2019-9-13_13-9-10.png?version=1&modificationDate=1568369347000&api=v2)
|**28.**|PageColorConfigs|A property that sets the base colors for all the UI elements.<br>goodResponseHeaderFooter - color set on header, footer, field header when the response is good.<br>colorInnerComponentLabels - color used for table headers and other labels inside the forms.<br>TitleTextColor - color user for text on field titles.<br>colorTableTextLabels - color used for table labels text and alike.<br>agentWindowStreamNoResponse - color used on inner elements such as clusters, cluster view inside the line on no response.<br>badResponse - color used for field header on bad response or waiting forbresponse <br> componentsBackgroundColor - background color for tree node, forms and other fields inside UI.|`"PageColorConfigs" : {  "goodResponseHeaderFooter" : "#489431",`![](https://info.singleton-labs.lt/download/attachments/36929901/image2019-9-13_13-9-27.png?version=1&modificationDate=1568369365000&api=v2)

## Server side:

| **Nr.** |Value|Explanation|Example|
|--|--|--|--
|**1.**|ZooKeeperAddress|The address at which the ZooKeeper that we are connecting to is running.|`ZooKeeperAddress = 172.16.6.86:2181`
|**2.**|serviceRegistry- StartNodeParent|A path used to help to make calls with the paths got from front-end to correct ZooKeeper nodes.|`serviceRegistry StartNodeParent = /streams/v1`
|**3.**|serviceRegistry- StartNode|The node at which the node tree registration should start.|`serviceRegistry StartNode = /streams/v1/clusters`
|**4.**|activeStream-Registry|A node that is used to get all the streams under agent and their metrics.|`activeStreamRegistry = _streamsAndMetrics`
|**5.**|depthToAgentNode|A property that describes the tree depth until agent node is reached.|`depthToAgentNode = 6`
|**6.**|SslConfigFilePath|An optional property that needs to be set if we want to use our own custom trust store. If not leave empty.|`SslConfigFilePath = C:\\development\\ apache-tomcat-9.0.14\\conf \\streamsAdmin.jks`
|**7.**|SslPass|An optional property that needs to be set if we want to use our own custom trust store. If not leave empty.|`SslPass=SomePAss`
|**8.**|authorization-TokenAction|Nodes used to check if the user has corresponding rights: Is able to start stream, stop stream and replay blocks under agent which this node exists on.|`authorization TokenAction = _actionToken`
|**9.**|authorization-TokenRead|Nodes used to check if the user has corresponding rights: Is able to read all nodes inside the agent under which this node exists.|`authorization TokenRead = _readToken`
|**10.**|tokenType|Authorization type for ZooKeeper|`tokenType = Bearer`
|**11.**|tokenExpiration-TimeGuava|Local user token expiration time in minutes|`tokenExpiration TimeGuava = 30`
|**12.**|secretKey|A property used to decrypt passwords received. The property set inside the client side configuration must mach in order for the decryption to work correctly.|`secretKey = exampleKey123`
|**13.**|usersExcludeList|Users list separated with ";" that should not be visible in the users list or accessible through any API calls.|`usersExcludeList = stream;userManager`
|**14.**|UserManager-Username|Username for admin used for temporary connections.|`UserManagerUsername = userManager`
|**15.**|UserManager-Password|Password for admin used for temporary connections.|`UserManagerPassword = userManager`
|**16.**|verifyURL|Properties for reCaptcha verification: Url to Google confirmation.|`verifyURL =  https://www.google.com /recaptcha/api /siteverify`
|**17.**|secretKeyCaptcha|properties for reCaptcha verification: The provided secret key is for development purposes. Valid secret key needs to be registered with Google.|`secretKeyCaptcha = 6LeIxAcTAAAAAGG-vFI1TnRWxMZN FuojJ4WifJWe`|