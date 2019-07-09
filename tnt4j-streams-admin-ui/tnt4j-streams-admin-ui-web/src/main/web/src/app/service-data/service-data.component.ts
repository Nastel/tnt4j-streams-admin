import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

import {MatInputModule} from '@angular/material/input';
import {FormControl, Validators} from '@angular/forms';

import { MatPaginator , MatTableDataSource, MatSort } from '@angular/material';
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';

@Component({
  selector: 'app-service-data',
  templateUrl: './service-data.component.html',
  styleUrls: ['./service-data.component.scss']
})
export class ServiceDataComponent implements OnInit {

/** Properties for material table */
 displayedColumns = ['name', 'value'];
 dataSourceServiceBaseStats = new MatTableDataSource<any>();

 @ViewChild('paginatorServiceBaseStat') paginatorServiceBaseData: MatPaginator;
 @ViewChild('matServiceBaseSort') sortServiceData: MatSort;

  /** Url address */
  pathToData : string;

  /** Service base data */
  serviceBaseData: Object;
  zooKeeperData: Object;
  serviceConfiguration = [];

  /** Values for showing data loading properties */
  valueThatChangesOnDataLoad = false;
  valueThatChangesForSpinnerOnResponse = true;

 /** Stream control window properties */
    streamStartStop = "Stop";
    streamPauseResume = "Pause";
    blockNumber="";

    serviceControlList = [];
    someData = [];

  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService) { }

  ngOnInit() {
    this.pathToData = this.router.url.substring(1);
    //console.log("the call to ZooKeeper from URL address",  this.pathToData)
    this.loadZooKeeperNodeData(this.pathToData);
  }

//    blockNumberFormControl = new FormControl('', [
//     // Validators.required
//    ]);

   loadZooKeeperNodeData(pathToData){
      this.valueThatChangesOnDataLoad = false;
      this.valueThatChangesForSpinnerOnResponse = true;
      this.data.getZooKeeperNodeData(pathToData).subscribe( data => {
        try{
          this.zooKeeperData = data;
          let result =  JSON.parse(this.zooKeeperData.toString());
          this.serviceConfiguration = result['config']
          result = result['data'];
          this.neededServiceControls( this.serviceConfiguration);
          if(this.utilsSvc.isObject(result)){
            console.log("BASE SERVICE DATA", result);
            this.valueThatChangesForSpinnerOnResponse = false;
            this.valueThatChangesOnDataLoad = true;
            this.serviceBaseData = this.provideFormattingForData(result);
            this.prepareServiceBaseData(result);
            setTimeout(() => this.dataSourceServiceBaseStats.paginator = this.paginatorServiceBaseData);
            setTimeout(() => this.dataSourceServiceBaseStats.sort = this.sortServiceData);
          }
          else{
            console.log("NON JSON TEXT DATA", result);
            this.valueThatChangesForSpinnerOnResponse = false;
            this.valueThatChangesOnDataLoad = true;
            this.serviceBaseData = result;
          }
        }catch(err){
          console.log("Problem while reading data from ZooKeeper path for base service stats ", err);
        }
      },
       err =>{
         this.valueThatChangesForSpinnerOnResponse = false;
         this.valueThatChangesOnDataLoad = false;
         console.log("Problem on reading threads data: ", err);
       }
      );
   }

  /*The information about agent runtime prepared for material table*/
  prepareServiceBaseData(streamData){
      let value = [];
       for(let data in streamData) {
          if(!this.utilsSvc.isObject(streamData[data])){
            let tempAgentRuntimeInfo = [];
            tempAgentRuntimeInfo["name"]=data;
            tempAgentRuntimeInfo["value"]=this.utilsSvc.formatData(data, streamData[data]);
            value.push(tempAgentRuntimeInfo);
          }
      }
      this.dataSourceServiceBaseStats = new MatTableDataSource(value);
  }

   sortData(){
    this.dataSourceServiceBaseStats.sort = this.sortServiceData;
   }

   applyFilterServiceData(filterValue: string) {
     this.dataSourceServiceBaseStats.filter = filterValue.trim().toLowerCase();
   }

   provideFormattingForData(streamData){
       let object = [];
       for(let data in streamData) {
           if(this.utilsSvc.isObject(streamData[data])){
           }else {
             let value = this.utilsSvc.formatData(data, streamData[data]);
             object[data] = value;
           }
       }
       return object;
   }

    public neededServiceControls(controls){
//    console.log(controls)
      if(!this.utilsSvc.compareStrings(controls['capabilities'], 'undefined')){
      if(this.utilsSvc.isObject(controls['capabilities'])){
        controls = controls['capabilities'];
        let tempArray = [];
          for( let data of controls){
                console.log(controls)
            tempArray.push(data);
          }
          this.serviceControlList = tempArray;
      }
      else{
        controls = controls['capabilities'].substring(1,controls['capabilities'].length - 1 ).split(',');
        let tempArray = [];
          for( let data of controls){
            tempArray.push(data);
          }
          this.serviceControlList = tempArray;
        }
      }
    }

      onEvent(event) {
         event.stopPropagation();
      }

    startStopStream(streamState){
      if(this.utilsSvc.compareStrings(streamState,"Stop")){
        this.streamStartStop="Start";
      }
      else{
        console.log("Starting ...");
        this.streamStartStop="Stop";
      }
    }

    pauseResumeStream(streamState){
      if(this.utilsSvc.compareStrings(streamState,"Pause")){
        this.streamPauseResume="Resume";
      }
      else{
        console.log("Resuming service...");
        this.streamPauseResume="Pause";
      }
    }

    replayTheBlockFromInput(blockNumber){
     console.log("Trying to replay block "+ blockNumber+" ...");
     this.blockNumber = "";
    }
}
