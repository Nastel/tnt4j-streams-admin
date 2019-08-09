import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable  } from 'rxjs';
import { ConfigurationHandler } from './config/configuration-handler';
import { UtilsService } from "./utils/utils.service";
import { map } from 'rxjs/operators';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import 'rxjs/add/observable/throw';

@Injectable({
  providedIn: 'root'
})

export class DataService {

  constructor(private http: HttpClient,
              private configurationHandler:ConfigurationHandler,
              private utilsSvc: UtilsService) { }


    getIncompleteBlocksData(serviceName)  : Observable<Object>{
      let serviceUrl = this.utilsSvc.urlForJKool(serviceName, "IncompleteBlocks");
      console.log("Incomplete blocks get", serviceUrl);
      return this.http.get(serviceUrl);
    }

    getIncompleteBlocksNoReceiptData(serviceName)  : Observable<Object>{
      let serviceUrl = this.utilsSvc.urlForJKool(serviceName, "IncompleteBlocksNoReceipt");
      console.log("Incomplete blocks get", serviceUrl);
      return this.http.get(serviceUrl);
    }


      /** --------------------------- tree node version -------------------*/


    /**
      Get the node data from path created
    */
    getZooKeeperNodeData(zooKeeperPath): Observable<Object>{
      let serviceUrl : string;
      if(!this.utilsSvc.compareStrings(zooKeeperPath, 'undefined')){
        let pathLink = zooKeeperPath.replace(this.configurationHandler.CONFIG["BasePathHide"],'');
        let urlBuild = this.configurationHandler.CONFIG["ZooKeeperBasePath"];
        let urlChoice = this.configurationHandler.CONFIG["ZooKeeperDataCall"];
        if(!this.utilsSvc.compareStrings(urlBuild, "undefined")){
            serviceUrl = urlBuild + pathLink + urlChoice[0];
        }
      }
      else{
        console.log("No path call defined");
      }
      //console.log("link to back end", serviceUrl);
      return this.http.get(serviceUrl, {responseType: 'text'} );
    }

    /**
      Get response from the link provided call without any formatting
    */
    getLinkData(serviceUrl)  : Observable<Object>{
      return this.http.get(serviceUrl);
    }


    /**
      send the request to replay block or blocks and wait for response if the operation was successful
    */
    sendControlsRequest(zooKeeperPath, controlChoice){
      let serviceUrl : string;
      if(!this.utilsSvc.compareStrings(zooKeeperPath, 'undefined')){
         let pathLink = zooKeeperPath.replace(this.configurationHandler.CONFIG["BasePathHide"],'');
         let urlBuild = this.configurationHandler.CONFIG["ZooKeeperBasePath"];
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
     // console.log(serviceUrl)
      return   this.http.get(serviceUrl, {responseType: 'text'} );
    }

    pingStream(streamPath : string){

    }

}
