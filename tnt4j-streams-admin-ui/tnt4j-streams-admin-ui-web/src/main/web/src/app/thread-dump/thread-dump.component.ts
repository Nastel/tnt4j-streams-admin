/*
 * Copyright 2014-2020 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component, OnInit, Inject, AfterContentChecked, ChangeDetectorRef} from '@angular/core';
import { Router } from '@angular/router';

import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';

import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import { TreeViewComponent } from '../tree-view/tree-view.component'

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
  nodeData = "";
  /** Values for showing data loading properties */
  valueThatChangesOnDataLoad = false;
  valueThatChangesForSpinnerOnResponse = false;
  dataHeight = 0;

    /** ZooKeeper loaded data */
   zooKeeperData: Object;
   nodeConf : string;

  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService,
                public dialog: MatDialog,
                public treeView: TreeViewComponent,
                private changeDetectionRef : ChangeDetectorRef) { }

  ngOnInit() {
    this.dataHeight = parseInt(localStorage.getItem("dataComponentHeight"), 10);
    this.dataHeight = this.dataHeight - 50;
    this.pathToData = this.router.url.substring(1);
    this.loadZooKeeperNodeData(this.pathToData);
  }
  ngAfterViewChecked(){
     this.changeDetectionRef.detectChanges();
  }
  reloadData(){
      this.treeView.loadZooKeeperNodeData(this.pathToData);
      this.ngOnInit();
  }

  loadZooKeeperNodeData(pathToData){
    this.treeView.responseShow("")
    try{
      this.nodeConf = this.treeView.nodeConf;
      this.zooKeeperData = this.treeView.zooKeeperData["data"];
      // this.serviceMetricsData =  this.zooKeeperData;
      for(let key in this.zooKeeperData){
        if(this.zooKeeperData[key].length>100){
          this.threadSplit(this.zooKeeperData[key]);
        }
        else{
          this.threadsTimeStamp =this.zooKeeperData[key];// this.utilsSvc.getDataFromTimestamp(  result[key] ) ;
        }
      }
      this.responseShow("good");
    }
     catch (err){
       this.responseShow("bad");
       console.log("Problem on default node while trying to prepare the showing of node data AGENT LOGS", err);
     }
//      this.data.getZooKeeperNodeData(pathToData).subscribe( data => {
//        try{
//          this.zooKeeperData = data;
//          let result =  JSON.parse(this.zooKeeperData.toString());
//          let config =  result["config"];
//          result = result['data'];
//          console.log(config["componentLoad"], result, this.utilsSvc.compareStrings(config["componentLoad"], "thread"))
//          if(this.utilsSvc.isObject(result) && this.utilsSvc.compareStrings(config["componentLoad"], "thread")){
//          console.log("THREAD DATA", result);
//            for(let key in result){
//              if(result[key].length>100){
//                this.threadSplit(result[key]);
//              }
//              else{
//                this.threadsTimeStamp =result[key];// this.utilsSvc.getDataFromTimestamp(  result[key] ) ;
//              }
//            }
//            this.responseShow("good");
//          }
//          else{
//            console.log("NON JSON TEXT DATA", JSON.stringify(result));
//            this.responseShow("good");
//            this.threadData = result;
//          }
//        }catch(err){
//          console.log("Problem occurred while trying to get threads data from ZooKeeper node", err);
//          this.responseShow("bad");
//        }
//      },
//       err =>{
//        this.responseShow("bad");
//         console.log("Problem on reading threads data: ", err);
//       }
//      );
   }

  threadSplit(data){
    try{
      if(!this.utilsSvc.compareStrings(data, "undefined")){
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


  openDialog(): void {
    const dialogRef = this.dialog.open(DialogOverviewExampleDialog, {
      width: '150em',
      height: '82em'
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }

}

@Component({
  selector: 'dialog-overview-example-dialog',
  templateUrl: 'dialog-overview-example-dialog.html',
})
export class DialogOverviewExampleDialog {

  /** Url address */
  pathToData : string;
  threadsTimeStamp = "";

  /** Service threads data */
  threadData: {};
  zooKeeperData: Object;
  nodeData = "";

    /** Values for showing data loading properties */
    valueThatChangesOnDataLoad = false;
    valueThatChangesForSpinnerOnResponse = false;
    dataHeight = 0;

  constructor(
               public utilsSvc: UtilsService,
               private router: Router,
               public dialogRef: MatDialogRef<DialogOverviewExampleDialog>,
               private dataService: DataService,) {}

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit() {
    this.pathToData = this.router.url.substring(1);
    this.loadZooKeeperNodeData(this.pathToData);
  }

  loadZooKeeperNodeData(pathToData){


      this.responseShow("");
      this.dataService.getZooKeeperNodeData(pathToData).subscribe( data => {
        try{
          this.zooKeeperData = data;
          let result = data;
          //let config = JSON.parse(result["config"]);
          let config =  result["config"];
          result = result['data'];
//          console.log(config["componentLoad"], "threadDump")
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
            this.responseShow("good");
          }
          else{
            console.log("NON JSON TEXT DATA", JSON.stringify(result));
            this.responseShow("good");
            this.threadData = result;
          }
        }catch(err){
          console.log("Problem occurred while trying to get threads data from ZooKeeper node", err);
          this.responseShow("bad");
        }
      },
       err =>{
        this.responseShow("bad");
         console.log("Problem on reading threads data: ", err);
       }
      );
   }

  threadSplit(data){
    try{
      if(!this.utilsSvc.compareStrings(data, "undefined")){
        this.responseShow("good");
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
      this.threadData=[];
      this.valueThatChangesOnDataLoad = false;
      this.valueThatChangesForSpinnerOnResponse = true;
    }
   }
}
