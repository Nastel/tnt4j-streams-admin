import { Component, OnInit, ElementRef, ViewChild} from '@angular/core';
import { Router } from '@angular/router';
import { MatIconRegistry } from "@angular/material";

import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';
import { incompleteBlocks }  from '../incomplete-blocks/incomplete-blocks.component';

@Component({
  selector: 'app-agent-logs',
  templateUrl: './agent-logs.component.html',
  styleUrls: ['./agent-logs.component.scss']
})
export class AgentLogsComponent implements OnInit {

@ViewChild('scrollMeLog')private myScrollContainerFakeNav: ElementRef;

  /** Url address */
  pathToData : string;
  objectKeys = Object.keys;

  /** Url address */
  findValueLog = "";

  /** Service log choice name */
  logChoiceName = "Service log";
  logArray = [];

  /** Service logs data */
  logData = [];
  logDataFull = [];
  logDataFiltered = [];

  scrollPosition = 0;
  logLinesAvailable = 0;
  logLinesLoaded = 0;
  logCount = 0;
  errLogCount = 0;
  zooKeeperData: Object;

  /** Values for showing data loading properties */
  valueThatChangesOnDataLoad = false;
  valueThatChangesForSpinnerOnResponse = false;
  dataHeight = 0;

  iconsRegistered = [];

  firstScrollToBottom = true;

  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService,
                private matIconRegistry: MatIconRegistry,
                public incBlocks: incompleteBlocks) { }

  ngOnInit() {
    this.dataHeight = parseInt(localStorage.getItem("dataComponentHeight"), 10);
    this.dataHeight = this.dataHeight - 150;
    let tempVal =this.configurationHandler.CONFIG["LazyLoadDataLines"];
    this.errLogCount = tempVal["logErr"];
    this.logCount = tempVal["log"];
    this.pathToData = this.router.url.substring(1);
    this.loadZooKeeperNodeData(this.pathToData);
    this.getAllRegisteredIonsList();
  }

    getAllRegisteredIonsList(){
    try{
        let iconMap = this.matIconRegistry['_svgIconConfigs'];
        let keys = iconMap.keys();
        for( let index =0; index < iconMap.size; index++){
           let value = keys.next().value.substring(1);
           this.iconsRegistered.push(value);
        }
      }
      catch(err){
        console.log("Problem on getting the registered icon list", err);
      }
    }

  /** On each view refresh*/
  ngAfterViewChecked() {
    this.scrollToLogPos();
  }

   loadZooKeeperNodeData(pathToData){
      this.valueThatChangesOnDataLoad = false;
      this.valueThatChangesForSpinnerOnResponse = true;
      this.data.getZooKeeperNodeData(pathToData).subscribe( data => {
        try{
          this.zooKeeperData = data;
          let result =  JSON.parse(this.zooKeeperData.toString());
          console.log("LOGS DATA", result);
          this.logArray = Object.keys(result["data"]);
          //  this.logArray = result["config"]["logNavigation"];
          if(this.utilsSvc.isObject(result)){
             result = result['data'];
             if(!this.utilsSvc.compareStrings(result[this.logChoiceName], "undefined")){
                result = result[this.logChoiceName];
                this.loadLogData(result);

             }
          }
          else{
            this.valueThatChangesForSpinnerOnResponse = false;
            this.valueThatChangesOnDataLoad = true;
            this.logData = result;
          }
          }
        catch{
            this.valueThatChangesForSpinnerOnResponse = false;
            this.valueThatChangesOnDataLoad = false;
            console.log("Problem on reading log data from : ", this.pathToData, this.logChoiceName);
          }
      },
       err =>{
         this.valueThatChangesForSpinnerOnResponse = false;
         this.valueThatChangesOnDataLoad = false;
         console.log("Problem on reading log data from : ", this.pathToData, this.logChoiceName);
       }
      );
   }

   /** Method for calling functions needed to prepare the log data to be shown*/
    loadLogData(logData){
       this.logDataFull = this.splitLines(logData);
       this.logData = this.logDataFull.slice(-1*this.logCount);
      // console.log("Log data", this.logData);

       this.logLinesLoaded = this.logData.length;
       this.logLinesAvailable = this.logDataFull.length;

      this.valueThatChangesForSpinnerOnResponse = false;
      this.valueThatChangesOnDataLoad = true;

    }

    /** Method for splitting data on new line*/
    splitLines(t) {
      try{
        if(!this.utilsSvc.compareStrings(t, "undefined") && !this.utilsSvc.compareStrings(t, "null") && !(t instanceof Array)){
          return t.split(/\r?\n/);
        }
        else{
          return t;
        }
        }
        catch(e){
          console.log("Problem on trying to split log data lines", e);
        }
    }

    scrollToBottom(): void {
       try {
           this.myScrollContainerFakeNav.nativeElement.scrollTop = this.myScrollContainerFakeNav.nativeElement.scrollHeight;
       } catch(err) {
           console.log("Problem while scrolling to bottom of log" , err);
        }
    }

    filterLogs(findValue){
      try{
        let elementLog = document.getElementById("scrollElementLog");
        if((typeof this.logDataFull !== 'undefined') && (typeof this.logDataFiltered !== 'undefined')){

            if(this.utilsSvc.compareStrings(findValue, "")){
                this.findValueLog="";
                this.logCount = this.configurationHandler.CONFIG["LazyLoadDataLines"]["log"];
                this.logLinesAvailable = this.logDataFull.length;
            }
            this.logDataFiltered = this.logDataFull.filter(function(el) {
               return el.toLowerCase().indexOf(findValue.toLowerCase()) > -1;
            })
            this.logLinesAvailable = this.logDataFiltered.length;
            this.logData = this.logDataFiltered.slice(-1*this.logCount);
            this.logLinesLoaded = this.logData.length;
        }
      }
      catch(err){
        console.log("Problem while trying to filter log data", err);
      }
    }

    loadMoreLogData(){
    try{
      let elementLog = document.getElementById("scrollElementLog");
      this.scrollPosition =  elementLog.scrollHeight - elementLog.scrollTop;
      this.firstScrollToBottom = true;
      this.logCount = this.logCount + this.configurationHandler.CONFIG["LazyLoadDataLines"]["log"];
      if(typeof this.logDataFiltered !== 'undefined' && (this.logDataFiltered.length > 0)){
        this.logData = this.logDataFiltered.slice(-1*this.logCount);
        this.logLinesLoaded = this.logData.length;
      }
      else if(typeof this.logDataFull !== 'undefined' && (this.logDataFull.length > 0)){
        this.logData = this.logDataFull.slice(-1*this.logCount);
        this.logLinesLoaded = this.logData.length;
      }
      }
      catch(err){
        console.log("Problem while trying to load more log data", err);
      }
    }

  scrollToLogPos(){
    try{
        let elementLogErr = document.getElementById("scrollElementErrLog");
        let elementLog = document.getElementById("scrollElementLog");
        if (elementLog == null){}
        else if(this.firstScrollToBottom){
          let height = elementLog.scrollHeight;
          elementLog.scrollTop = height - this.scrollPosition;
          this.firstScrollToBottom=false;
        }
        if (elementLogErr == null){}
        else if(this.firstScrollToBottom){
          let height = elementLogErr.scrollHeight;
          elementLogErr.scrollTop = height - this.scrollPosition;
          this.firstScrollToBottom=false;
        }
      }
      catch(err){
        console.log("Problem while trying to scroll to the correct position on log window", err);
      }
    }

    logChoice(choice){
       try {
         this.logChoiceName = choice;
         this.loadZooKeeperNodeData(this.pathToData);
       } catch(err) {
           console.log("Problem while changing log choice and reloading data" , err);
        }
   }



}
