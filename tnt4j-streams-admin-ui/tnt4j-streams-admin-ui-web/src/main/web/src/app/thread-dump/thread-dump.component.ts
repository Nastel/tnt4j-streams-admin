import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';

@Component({
  selector: 'app-thread-dump',
  templateUrl: './thread-dump.component.html',
  styleUrls: ['./thread-dump.component.scss']
})
export class ThreadDumpComponent implements OnInit {


  /** Url address */
  pathToData : string;
  threadsTimeStamp = "";

  /** Service threads data */
  threadData: [];
  zooKeeperData: Object;
  nodeData = "";
  /** Values for showing data loading properties */
  valueThatChangesOnDataLoad = false;
  valueThatChangesForSpinnerOnResponse = false;

  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService) { }

  ngOnInit() {
    this.pathToData = this.router.url.substring(1);
    //console.log("the call to ZooKeeper from URL address",  this.pathToData)
    this.loadZooKeeperNodeData(this.pathToData);
  }

   loadZooKeeperNodeData(pathToData){
      this.valueThatChangesOnDataLoad = false;
      this.valueThatChangesForSpinnerOnResponse = true;
      this.data.getZooKeeperNodeData(pathToData).subscribe( data => {
        try{
          this.zooKeeperData = data;
          let result =  JSON.parse(this.zooKeeperData.toString());
          let config = result['config'];
          result = result['data'];
          console.log(config["componentLoad"], "threadDump")
          if(this.utilsSvc.isObject(result) && this.utilsSvc.compareStrings(config["componentLoad"], "thread")){
          console.log("THREAD DATA", result);
            for(let key in result){
              if(result[key].length>100){
                this.threadSplit(result[key]);
              }
              else{
                this.threadsTimeStamp =result[key];// this.utilsSvc.getDataFromTimestamp(  result[key] ) ;
              }
            }
          }
          else{
            console.log("NON JSON TEXT DATA", JSON.stringify(result));
            this.valueThatChangesForSpinnerOnResponse = false;
            this.valueThatChangesOnDataLoad = true;
            this.nodeData = JSON.stringify(result);
          }
        }catch(err){
          console.log("Problem occurred while trying to get threads data from ZooKeeper node", err);
        }
      },
       err =>{
         this.valueThatChangesForSpinnerOnResponse = false;
         this.valueThatChangesOnDataLoad = false;
         console.log("Problem on reading threads data: ", err);
       }
      );
   }

  threadSplit(data){
    try{
      if(!this.utilsSvc.compareStrings(data, "undefined")){
        this.valueThatChangesOnDataLoad = true;
        this.valueThatChangesForSpinnerOnResponse = false;
        let threadsArray= this.splitLines(data);
        this.threadData = threadsArray;
      }
    }catch(err){
      console.log("Problem occurred while splitting threads data ", err);
    }
  }

  splitLines(t) {
     if(!this.utilsSvc.compareStrings(t, "undefined") && !this.utilsSvc.compareStrings(t, "null") && !(t instanceof Array)){
      return t.split(/\\r\\n|\\r|\\n/);
     }
     else{
      return t;
     }
  }

}
