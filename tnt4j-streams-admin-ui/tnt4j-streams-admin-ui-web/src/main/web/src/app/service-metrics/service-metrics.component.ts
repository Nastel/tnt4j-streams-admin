import { Component, OnInit,  ViewChild} from '@angular/core';
import { Router } from '@angular/router';

import { MatIconRegistry } from "@angular/material";
import { MatPaginator , MatTableDataSource, MatSort } from '@angular/material';
import { DomSanitizer } from "@angular/platform-browser";
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';
import { incompleteBlocks }  from '../incomplete-blocks/incomplete-blocks.component';
import { TreeViewComponent } from '../tree-view/tree-view.component'

@Component({
  selector: 'app-service-metrics',
  templateUrl: './service-metrics.component.html',
  styleUrls: ['./service-metrics.component.scss']
})
export class ServiceMetricsComponent implements OnInit {


  @ViewChild('paginatorRuntime') paginator: MatPaginator;
  @ViewChild(MatSort) sortMetrics: MatSort;

 displayedColumns = ['name', 'value', 'name1', 'value1'];
 dataSourceMetrics = new MatTableDataSource<any>();

  /** Url address */
  pathToData : string;

  /** Service metrics data */
  serviceMetricsData = [];

  /** ZooKeeper loaded data */
 zooKeeperData: Object;
 nodeConf : string;

 /** Service separated data */
  timerData = [];
  meterData = [];
  /** Values for showing data loading properties */
  valueThatChangesOnDataLoad = false;
  valueThatChangesForSpinnerOnResponse = false;

  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService,
                public treeView: TreeViewComponent) { }

  ngOnInit() {
    this.pathToData = this.router.url.substring(1);
    //console.log("the call to ZooKeeper from URL address",  this.pathToData)
    this.loadZooKeeperNodeData(this.pathToData);
  }


  prepareMetricsData(metricInfo){
      let value = [];
      let dataObjectsCount = 0;
      for(let key in metricInfo){
         if(this.utilsSvc.isObject(metricInfo[key])){
            dataObjectsCount++;
         }
      }
      let tempName = "";
      let tempValue = [];
      let tempNumber = 0;
      for(let key in metricInfo){
         let tempAgentRuntimeInfo = [];
         if(this.utilsSvc.isObject(metricInfo[key])){
            tempNumber++;
            if(dataObjectsCount > tempNumber && (tempNumber%2)==0){
              tempAgentRuntimeInfo["name"]=key;
              tempAgentRuntimeInfo["value"]=metricInfo[key];
              tempAgentRuntimeInfo["name1"]=tempName;
              tempAgentRuntimeInfo["value1"]=tempValue;
              value.push(tempAgentRuntimeInfo);
            }
            else if(dataObjectsCount == tempNumber){
                tempAgentRuntimeInfo["name"]=key;
                tempAgentRuntimeInfo["value"]=metricInfo[key];
                value.push(tempAgentRuntimeInfo);
            }
            tempName = key;
            tempValue = metricInfo[key];
          }
      }
      this.dataSourceMetrics = new MatTableDataSource(value);
  }
  reloadData(){
      this.treeView.loadZooKeeperNodeData(this.pathToData);
      this.ngOnInit();
  }
   loadZooKeeperNodeData(pathToData){
       this.responseShow("")
       try{
         this.nodeConf = this.treeView.nodeConf;
         this.zooKeeperData = this.treeView.zooKeeperData["data"];
        // this.serviceMetricsData =  this.zooKeeperData;
         this.formatData();
         this.prepareMetricsData( this.zooKeeperData);
         setTimeout(() => this.dataSourceMetrics.paginator = this.paginator);
         setTimeout(() => this.dataSourceMetrics.sort = this.sortMetrics);
         this.responseShow("good")
       }
       catch (err){
         this.responseShow("bad");
         console.log("Problem on default node while trying to prepare the showing of node data AGENT LOGS", err);
       }

//      this.valueThatChangesOnDataLoad = false;
//      this.valueThatChangesForSpinnerOnResponse = true;
//      this.data.getZooKeeperNodeData(pathToData).subscribe( data => {
//        try{
//          this.zooKeeperData = data;
//          let result =  JSON.parse(this.zooKeeperData.toString());
//          console.log("SERVICE METRICS DATA", result);
//          result = result['data'];
//          if(this.utilsSvc.isObject(result)){
//            this.valueThatChangesForSpinnerOnResponse = false;
//            this.valueThatChangesOnDataLoad = true;
//            this.serviceMetricsData = result;
//            this.formatData();
//           // this.separateTimerMeterData();
//            this.prepareMetricsData( this.serviceMetricsData);
//            setTimeout(() => this.dataSourceMetrics.paginator = this.paginator);
//            setTimeout(() => this.dataSourceMetrics.sort = this.sortMetrics);
//          }
//          else{
//           // console.log("NON JSON TEXT DATA", result);
//            this.valueThatChangesForSpinnerOnResponse = false;
//            this.valueThatChangesOnDataLoad = true;
//            this.serviceMetricsData = result;
//          }
//        }catch(err){
//          console.log("Problem while reading data from ZooKeeper path for service metrics ", err);
//        }
//      },
//       err =>{
//         this.valueThatChangesForSpinnerOnResponse = false;
//         this.valueThatChangesOnDataLoad = false;
//         console.log("Problem on reading threads data: ", err);
//       }
//      );
   }

   applyFilter(filterValue: string) {
     this.dataSourceMetrics.filter = filterValue.trim().toLowerCase();
   }

   formatData(){
     let value = [];
     for(let key in this.zooKeeperData){
     //console.log("full metrics data",key, this.zooKeeperData[key]);
        if(this.utilsSvc.isObject(this.zooKeeperData[key])){
          for(let keyInner in this.zooKeeperData[key]){
            // console.log("full metrics data",keyInner, this.zooKeeperData[key][keyInner]);
             // let tempKeyInner = this.utilsSvc.insertSpaces(keyInner);
               if(!this.getIfSnapshotTimer(keyInner)){
                 let convertedTime = this.utilsSvc.convertNanoToSeconds(this.zooKeeperData[key][keyInner]);
                 this.zooKeeperData[key][keyInner] = convertedTime;
               }
               else{
                this.zooKeeperData[key][keyInner] = this.utilsSvc.formatData(keyInner , this.zooKeeperData[key][keyInner]);
              }
           }
         }
     }
     this.dataSourceMetrics = new MatTableDataSource(value);
   }

   getIfSnapshotTimer(string){
      if(string.includes('count')){
        return true;
      }
      else if(string.includes('Rate')){
       return true;
      }
      else if(string.includes('snapshot')){
       return true;
     }
     else if(string.includes('values')){
       return true;
     }
   }


   separateTimerMeterData(){
     let objTimer= [];
     let objMeter= [];
     try{
      // console.log("Formatted data: ", this.zooKeeperData);
       for(let dataObj in this.zooKeeperData){
         if(this.utilsSvc.isObject(this.zooKeeperData[dataObj])){
            if( this.checkIfTimer(dataObj)){
              objTimer[dataObj] = this.zooKeeperData[dataObj];
            }
            else{
               objMeter[dataObj] = this.zooKeeperData[dataObj];
            }
         }
       }
     }catch(err){
      console.log("Problem occurred while separating the timers and meters data ", err);
     }
        this.timerData = objTimer;
       // console.log("Timers: ", this.timerData);
        this.meterData = objMeter;
        //console.log("Meters: ", this.meterData);
   }

   checkIfTimer(stringData){
    // console.log("Check if timer:", stringData);
     let string = stringData;
     let expr = /Time/;
     if(string.match(expr)){
         console.log("Check");
       return true;
     }
     else{ return false;}
   }

    /*Response variables set to good, bad or else for showing the data loading state*/
         responseShow(responseData){
          if(this.utilsSvc.compareStrings(responseData, "good")){
            this.valueThatChangesForSpinnerOnResponse = false;
            this.valueThatChangesOnDataLoad = true;
          }
          else if(this.utilsSvc.compareStrings(responseData, "bad")){
            this.valueThatChangesForSpinnerOnResponse = false;
            this.valueThatChangesOnDataLoad = false;
          }
          else{ //still loading
            this.valueThatChangesOnDataLoad = false;
            this.valueThatChangesForSpinnerOnResponse = true;
          }
         }

}
