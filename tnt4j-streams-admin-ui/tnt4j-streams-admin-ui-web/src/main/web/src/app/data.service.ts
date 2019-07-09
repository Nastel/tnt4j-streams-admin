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


  /** From first version */
    getAllNeeded() : Observable<Object> {
      return this.http.get(this.configurationHandler.CONFIG["EndpointAddressAllService"]);
    }

    getAllDataOfOneService(serviceName)  : Observable<Object> {
      let urlBuild = this.configurationHandler.CONFIG["EndpointAddressService"];
      let urlToServiceData = urlBuild[0]+serviceName+urlBuild[1];
  // console.log(urlToServiceData);
      return this.http.get(urlToServiceData);
    }

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

    getRepositoryData(serviceName): Observable<Object> {
      let serviceUrl = this.utilsSvc.urlForJKool(serviceName, "RepositoryData");
      console.log("repostirory url", serviceUrl);
      return this.http.get(serviceUrl);
    }

    getLogData(streamName, logChoice): Observable<Object>{
     let serviceUrl = this.utilsSvc.urlForLogData(streamName, "EndpointLogData", logChoice);
      return this.http.get(serviceUrl, {responseType: 'text'} );
    }

    getStreamConfigData(streamName): Observable<Object>{
      let urlBuild = this.configurationHandler.CONFIG["EndpointStreamConfigData"];
       let serviceUrl : string
      if(!this.utilsSvc.compareStrings(urlBuild, "undefined")){
          serviceUrl = urlBuild[0] +  urlBuild[1];

      }
      return this.http.get(serviceUrl, {responseType: 'text'} );
    }


      /** --------------------------- tree node version -------------------*/

    getZooKeeperNodeData(zooKeeperPath): Observable<Object>{
      let serviceUrl : string;
      if(!this.utilsSvc.compareStrings(zooKeeperPath, 'undefined')){
        let pathLink = zooKeeperPath.replace(this.configurationHandler.CONFIG["BasePathHide"],'');
        let urlBuild = this.configurationHandler.CONFIG["ZooKeeperDataCall"];
        if(!this.utilsSvc.compareStrings(urlBuild, "undefined")){
            serviceUrl = urlBuild[0] + pathLink + urlBuild[1];
        }
      }
     // console.log("link to back end", serviceUrl);
      return this.http.get(serviceUrl, {responseType: 'text'} );
    }

    getLinkData(serviceUrl)  : Observable<Object>{
      return this.http.get(serviceUrl);
    }

}
