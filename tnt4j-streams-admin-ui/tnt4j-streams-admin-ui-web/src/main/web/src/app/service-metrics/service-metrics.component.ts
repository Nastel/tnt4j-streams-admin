import { Component, OnInit,  ViewChild} from '@angular/core';
import { Router } from '@angular/router';

import { MatIconRegistry } from "@angular/material";
import { MatPaginator , MatTableDataSource, MatSort } from '@angular/material';
import { DomSanitizer } from "@angular/platform-browser";
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';
import { incompleteBlocks }  from '../incomplete-blocks/incomplete-blocks.component';

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
  zooKeeperData: Object;

 /** Service separated data */
  timerData = [];
  meterData = [];
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
              console.log("Inside if", dataObjectsCount);
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

   loadZooKeeperNodeData(pathToData){
      this.valueThatChangesOnDataLoad = false;
      this.valueThatChangesForSpinnerOnResponse = true;
      this.data.getZooKeeperNodeData(pathToData).subscribe( data => {
        try{
          this.zooKeeperData = data;
          let result =  JSON.parse(this.zooKeeperData.toString());
          console.log("SERVICE METRICS DATA", result);
          result = result['data'];
          if(this.utilsSvc.isObject(result)){
            this.valueThatChangesForSpinnerOnResponse = false;
            this.valueThatChangesOnDataLoad = true;
            this.serviceMetricsData = result;
            this.formatData();
           // this.separateTimerMeterData();
            this.prepareMetricsData( this.serviceMetricsData);
            setTimeout(() => this.dataSourceMetrics.paginator = this.paginator);
            setTimeout(() => this.dataSourceMetrics.sort = this.sortMetrics);
          }
          else{
           // console.log("NON JSON TEXT DATA", result);
            this.valueThatChangesForSpinnerOnResponse = false;
            this.valueThatChangesOnDataLoad = true;
            this.serviceMetricsData = result;
          }
        }catch(err){
          console.log("Problem while reading data from ZooKeeper path for service metrics ", err);
        }
      },
       err =>{
         this.valueThatChangesForSpinnerOnResponse = false;
         this.valueThatChangesOnDataLoad = false;
         console.log("Problem on reading threads data: ", err);
       }
      );
   }

   applyFilter(filterValue: string) {
     this.dataSourceMetrics.filter = filterValue.trim().toLowerCase();
   }

   formatData(){
     let value = [];
     for(let key in this.serviceMetricsData){
     //console.log("full metrics data",key, this.serviceMetricsData[key]);
        if(this.utilsSvc.isObject(this.serviceMetricsData[key])){
          for(let keyInner in this.serviceMetricsData[key]){
            // console.log("full metrics data",keyInner, this.serviceMetricsData[key][keyInner]);
             // let tempKeyInner = this.utilsSvc.insertSpaces(keyInner);
               if(!this.getIfSnapshotTimer(keyInner)){
                 let convertedTime = this.utilsSvc.convertNanoToSeconds(this.serviceMetricsData[key][keyInner]);
                 this.serviceMetricsData[key][keyInner] = convertedTime;
               }
               else{
                this.serviceMetricsData[key][keyInner] = this.utilsSvc.formatData(keyInner , this.serviceMetricsData[key][keyInner]);
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
      // console.log("Formatted data: ", this.serviceMetricsData);
       for(let dataObj in this.serviceMetricsData){
         if(this.utilsSvc.isObject(this.serviceMetricsData[dataObj])){
            if( this.checkIfTimer(dataObj)){
              objTimer[dataObj] = this.serviceMetricsData[dataObj];
            }
            else{
               objMeter[dataObj] = this.serviceMetricsData[dataObj];
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

}