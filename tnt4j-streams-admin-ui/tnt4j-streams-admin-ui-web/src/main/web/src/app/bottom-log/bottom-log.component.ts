import { Component, OnInit, OnDestroy, ElementRef, ViewChild} from '@angular/core';
import { Router } from '@angular/router';
import { MatIconRegistry } from "@angular/material";

import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';
import { incompleteBlocks }  from '../incomplete-blocks/incomplete-blocks.component';

@Component({
  selector: 'app-bottom-log',
  templateUrl: './bottom-log.component.html',
  styleUrls: ['./bottom-log.component.scss']
})
export class BottomLogComponent implements OnInit, OnDestroy{


  @ViewChild('scrollMe')private myLogScroll: ElementRef;

 /** Url address */
  pathToData : string;
  objectKeys = Object.keys;

  /** Url address */
  findValueLog = "";
  pathToLogs = "";

  /** Service log choice name */
  logChoiceName: string;
  autoUpdate = "Auto update Off";
  findValueBottomLog = "";

  /** Service logs data */
  logDataBottom = [];
  logDataFull = {};
  logDataBottomFiltered = [];


  zooKeeperData: Object;

  /** Values for showing data loading properties */
  valueThatChangesOnDataLoad = false;
  valueThatChangesForSpinnerOnResponse = false;
  updateLogs = true;

  iconsRegistered = [];

  firstScrollToBottom = true;

  interval:any;

  logCount = this.configurationHandler.CONFIG["LazyLoadDataLines"]["bottomLog"];
  logReloadTime = this.configurationHandler.CONFIG["LazyLoadDataLines"]["bottomLogLoadIntervalMS"];


  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService,
                private matIconRegistry: MatIconRegistry) { }

  ngOnInit() {
     this.logChoiceName = this.configurationHandler.CONFIG["LogToShowAtBottom"];
     let item = localStorage.getItem("bottomLogUpdateState");
     this.pathToData = this.router.url.substring(1);
     this.iconsRegistered = this.utilsSvc.getAllRegisteredIonsList();
     this.pathToLogs = this.createPathToLogsData(this.pathToData);
     this.loadLogData(this.pathToLogs);
     this.interval = setInterval(() => {
        this.scrollToBottom();
        this.loadLogData(this.pathToLogs);
     }, this.logReloadTime);
     this.sessionUpdateLog(item);
  }

  ngAfterViewChecked() {
     this.scrollToBottom();
  }

  ngOnDestroy(){
     // console.log ("Destroy called", this.pathToData);
      clearInterval(this.interval);
  }

  sessionUpdateLog(choice){
  // console.log("SESSION CHOICE", this.autoUpdate);
    if(this.utilsSvc.compareStrings(choice, "Auto update Off")){
      this.updateLogs = true;
      this.autoUpdate = "Auto update Off";
      clearInterval(this.interval);
    }
    else{
      this.updateLogs = false;
      this.autoUpdate = "Auto update On";
    }
   // console.log(this.autoUpdate);
  }

  turnOnOffAutoLogUpdate(){
   //console.log("CHOICE", this.autoUpdate);
   let tempVal = this.configurationHandler.CONFIG["LazyLoadDataLines"]
    if(this.utilsSvc.compareStrings(this.autoUpdate, "Auto update Off")){
      this.autoUpdate = "Auto update On";
      this.interval = setInterval(() => {
           this.loadLogData(this.pathToLogs);
       }, this.logReloadTime);
    }
    else{
      this.autoUpdate = "Auto update Off";
      clearInterval(this.interval);
      }
    localStorage.setItem("bottomLogUpdateState",  this.autoUpdate);
  }

  createPathToLogsData(path): string{
    let pathToLogs = "";
    let array = path.split("/");
    if(array.length>5){
      for(let data = 0 ; 5 > data; data++){
        pathToLogs = pathToLogs + '/'+ array[data];
      }
      pathToLogs = pathToLogs.substring(1) + "/logs";
    }
    else{
      pathToLogs = path + "/logs";
    }

    return pathToLogs;
  }

   loadLogData(path){
      this.data.getZooKeeperNodeData(path).subscribe(
        data => {
        var mapped = []; let result;
        try{
            let tempVal =this.configurationHandler.CONFIG["LazyLoadDataLines"];
            tempVal = tempVal["bottomLog"];
            this.zooKeeperData = data;
            result =  this.zooKeeperData; //JSON.parse(this.zooKeeperData.toString());
            if(this.utilsSvc.isObject(result)){
               result =  this.zooKeeperData['data'];
               if(!this.utilsSvc.compareStrings(result[this.logChoiceName], "undefined")){
                  result = result[this.logChoiceName];
                  this.logDataBottom = result.slice(-1*tempVal);
               }
            }
            else{
              this.valueThatChangesForSpinnerOnResponse = false;
              this.valueThatChangesOnDataLoad = true;
              this.logDataBottom = result.slice(-1*tempVal);
            }
          }
          catch(e){
            this.valueThatChangesForSpinnerOnResponse = false;
            this.valueThatChangesOnDataLoad = false;
            this.logDataBottom.push( "Problem on reading log data from : ", path, e);
            this.autoUpdate = "Auto update On";
            clearInterval(this.interval)
          }
          finally{
            this.scrollToBottom();
          }
        },
         err =>{
            console.log("Push to log failed", err)
            this.logDataBottom.push( err.toLocaleString());
            this.autoUpdate = "Auto update On";
            clearInterval(this.interval);
         }
        );
    }

  scrollToBottom(): void {
     try {
         this.myLogScroll.nativeElement.scrollTop = this.myLogScroll.nativeElement.scrollHeight;
     } catch(err) {
         console.log("Problem while scrolling to bottom of log" , err);
      }
  }

  filterLogs(findValue){
    try{
      let elementLog = document.getElementById("scrollMe");
      if (elementLog == null){
        if(this.utilsSvc.compareStrings(findValue, "")){
          this.findValueBottomLog = "";
        }
        this.logDataBottomFiltered = this.logDataBottom.filter(function(el) {
          return el.toLowerCase().indexOf(findValue.toLowerCase()) > -1;
        })
         this.logDataBottom = this.logDataBottomFiltered;
      }
    }
    catch(err){
      console.log("Problem while trying to filter log data", err);
    }
  }


}
