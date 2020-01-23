import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable  } from 'rxjs';
import { HttpHeaders } from '@angular/common/http';
import { ConfigurationHandler } from './config/configuration-handler';
import { UtilsService } from "./utils/utils.service";
import { map } from 'rxjs/operators';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import {Router, RouterModule} from '@angular/router';
import 'rxjs/add/observable/throw';



@Injectable({
  providedIn: 'root'
})

export class DataService {

  constructor(private http: HttpClient,
              private configurationHandler:ConfigurationHandler,
              private utilsSvc: UtilsService,
              private router: RouterModule) { }

  /**
    Get the node data from path created
  */
  getZooKeeperNodeData(zooKeeperPath): Observable<Object>{
    var header = this.returnHeaderWithToken();

    let serviceUrl : string;
    if(!this.utilsSvc.compareStrings(zooKeeperPath, 'undefined')){
      let pathLink = zooKeeperPath.replace(this.configurationHandler.CONFIG["BasePathHide"],'');
      let urlBuild = this.configurationHandler.CONFIG["BaseAddress"] + this.configurationHandler.CONFIG["ZooKeeperBasePath"];
      let urlChoice = this.configurationHandler.CONFIG["ZooKeeperDataCall"];
      if(!this.utilsSvc.compareStrings(urlBuild, "undefined")){
          serviceUrl = urlBuild + pathLink + urlChoice[0];
      }
    }
    else{
      console.log("No path call defined");
    }
    //console.log("link to back end", serviceUrl);
    return this.http.get(serviceUrl, header);
  }


  /**
    send the request to replay block or blocks and wait for response if the operation was successful
  */
  sendControlsRequest(zooKeeperPath, controlChoice){
    var header = this.returnHeaderWithToken();
    let serviceUrl : string;
    if(!this.utilsSvc.compareStrings(zooKeeperPath, 'undefined')){
       let pathLink = zooKeeperPath.replace(this.configurationHandler.CONFIG["BasePathHide"],'');
       let urlBuild = this.configurationHandler.CONFIG["BaseAddress"]+ this.configurationHandler.CONFIG["ZooKeeperBasePath"];
       let urlChoice = this.configurationHandler.CONFIG["ZooKeeperDataCall"];
       for(let choice of urlChoice){
         if(choice.includes(controlChoice)){
             serviceUrl = urlBuild + pathLink +  choice;
         }
       }
    }
    else{
      console.log("No path call defined");
    }
    console.log(serviceUrl)
    return   this.http.get(serviceUrl, header);
  }


  /**
    send the request to replay block or blocks and wait for response if the operation was successful
  */
  sendUpdateStreamRequest(zooKeeperPath){
    let token = this.configurationHandler.CONFIG["Rundeck"]["token"];
    var header = this.returnHeaderWithTokenRundeck(token);
    return   this.http.post(zooKeeperPath,{"argString":"-testing test"}, header);
  }

  /**
    Call login request with credentials inside header and return the response or error message.
  */
  getBackEndLoginResponse(pathToEndpoint: string, password :string, username: string, reCaptcha :string){
    try{
    let input = "{ \"password\" : \""+password+"\", \"username\" : \""+username+"\" , \"responseCaptcha\" : \""+reCaptcha+"\" } ";
      var header = { headers: new HttpHeaders().set('Authorization',  input)}
      let urlBuild = this.configurationHandler.CONFIG["BaseAddress"]+this.configurationHandler.CONFIG["LoginRequestPath"];
      let serviceUrl = urlBuild + "/"+ pathToEndpoint;
      return this.http.get(serviceUrl,  header);
    }catch(err){
      console.log(err);
    }
  }

  /**
    Call logout request with token inside header and return the response or error message.
  */
  getLogoutRequest(pathToEndpoint: string, endpoint : string ){
    try{
      let urlBuild = this.configurationHandler.CONFIG["BaseAddress"]+this.configurationHandler.CONFIG["LoginRequestPath"];
      let serviceUrl = urlBuild + "/"+ endpoint;
      return this.http.get(serviceUrl);
    }catch(err){
      console.log(err);
    }
  }

  /**
    Get response from the link provided call without any formatting
  */
  getLinkData(serviceUrl)  : Observable<Object>{
    var header = this.returnHeaderWithToken();
          console.log("header", header);
    return this.http.get(serviceUrl, header);
  }

  /**
    A utils method to help with request authorization.
  */
  returnHeaderWithToken(){
    let tokenName = this.configurationHandler.CONFIG["sessionTokenName"];
    let input = sessionStorage.getItem(tokenName);
    var header = { headers: new HttpHeaders({'Authorization':  JSON.stringify(input), 'Content-Type': 'application/json; charset=utf-8'}) };
   // var header = { headers: new HttpHeaders({'Authorization':  JSON.stringify(input), 'Content-Type': 'text/plain; charset=utf-8'}) };
    return header;
  }

    /**
      A utils method to help with request authorization.
    */
    returnHeaderWithTokenRundeck(token: string){
      var header = { headers: new HttpHeaders({'X-Rundeck-Auth-Token': token, 'Content-Type': 'application/json; charset=utf-8'}) };
      return header;
    }

  /**
    A utils method to help manage users by adding new users to the ZooKeeper with form data
  */
  addNewUser(pathToPost: string, username: string, password: string, clustersWithRights){
//  console.log(clustersWithRights);
    let urlBuild = this.configurationHandler.CONFIG["BaseAddress"]+this.configurationHandler.CONFIG["LoginRequestPath"];
    let serviceUrl = urlBuild + "/"+ pathToPost;
//    console.log(serviceUrl);
    var header = this.returnHeaderWithToken();
              console.log("header", header);
    return this.http.post(serviceUrl,{
                            "username" : username,
                            "password" : password,
                            "clusters" : clustersWithRights,
                          }, header);
  }

  /**
    A utils method to help manage users by removing them from ZooKeeper
  */
  removeUser(addressPath: string, username: string, clusterNames: string[]){
    let urlBuild = this.configurationHandler.CONFIG["BaseAddress"]+this.configurationHandler.CONFIG["LoginRequestPath"];
    let serviceUrl = urlBuild + "/"+ addressPath;
//    console.log(serviceUrl);
    var header = this.returnHeaderWithToken();
              console.log("header", header);
    return this.http.post(serviceUrl,{
                            "username" : username,
                            "clusters" : clusterNames
                          }, header);
  }

  /**
    A utils method to help manage users by refreshing the users list for admin
  */
  loadUsersList(addressPath: string, clusters :string){
    let urlBuild = this.configurationHandler.CONFIG["BaseAddress"]+this.configurationHandler.CONFIG["LoginRequestPath"];
    let serviceUrl = urlBuild + "/"+ addressPath;
//    console.log(serviceUrl);
    let clusterList = clusters.split(';');
//    console.log(clusterList)
    var header = this.returnHeaderWithToken();
              console.log("header", header);
    return this.http.post(serviceUrl, {"clusters" :clusterList}, header);
  }
  /**
    A utils method to help manage users by refreshing single user information
  */
  loadUserData(addressPath: string, clusters :string, username : string){
    let urlBuild = this.configurationHandler.CONFIG["BaseAddress"]+this.configurationHandler.CONFIG["LoginRequestPath"];
    let serviceUrl = urlBuild + "/"+ addressPath;
//    console.log(serviceUrl);
    let clusterList = clusters.split(';');
//    console.log(clusterList)
    var header = this.returnHeaderWithToken();
              console.log("header", header);
    return this.http.post(serviceUrl, {"clusters" :clusterList, "username" : username}, header);
  }
  /**
    A utils method to help manage users editing the chosen user password or rights
  */
  editUser(addressPath: string, username: string, password: string, clustersWithRights){
    let urlBuild = this.configurationHandler.CONFIG["BaseAddress"]+this.configurationHandler.CONFIG["LoginRequestPath"];
    let serviceUrl = urlBuild + "/"+ addressPath;
//    console.log(serviceUrl);
    var header = this.returnHeaderWithToken();
              console.log("header", header);
    return this.http.post(serviceUrl,{
                            "username" : username,
                            "password" : password,
                            "clusters" : clustersWithRights,
                          }, header);
  }


  /**
    A utils method that lets a simple user edit his password
  */
  editUserNonAdmin(addressPath: string, username: string, password: string, clustersWithRights){
    let urlBuild = this.configurationHandler.CONFIG["BaseAddress"]+this.configurationHandler.CONFIG["LoginRequestPath"];
    let serviceUrl = urlBuild + "/"+ addressPath;
//    console.log(serviceUrl);
    var header = this.returnHeaderWithToken();
              console.log("header", header);
    return this.http.post(serviceUrl,{
                            "username" : username,
                            "password" : password,
                            "clusters" : clustersWithRights,
                          }, header);
  }

  /**
    A method to protect from dos and similar attacks.
  */
  captchaCheck(responseToken : string){
    let serviceUrl =this.configurationHandler.CONFIG["BaseAddress"]+ this.configurationHandler.CONFIG["reCaptchaRequest"];
//    console.log(serviceUrl);
    var header = this.returnHeaderWithToken();
    console.log("header", header);
    return this.http.post(serviceUrl,{  "responseCaptcha" : responseToken }, header);
  }

    /**
      A method to protect from dos and similar attacks.
    */
    captchaCheckUnauthorized(urlEnd: string, responseToken : string){
      let serviceUrl =this.configurationHandler.CONFIG["BaseAddress"]+ this.configurationHandler.CONFIG["reCaptchaRequest"];
      serviceUrl = serviceUrl +"/"+urlEnd;
//      console.log(serviceUrl);
      return this.http.post(serviceUrl,{  "responseCaptcha" : responseToken });
    }
}
