import { Component, OnInit, ElementRef, ViewChild, ChangeDetectorRef} from '@angular/core';
import { Router } from '@angular/router';
import { MatIconRegistry } from "@angular/material";
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';
import { incompleteBlocks }  from '../incomplete-blocks/incomplete-blocks.component';
import { TreeViewComponent } from '../tree-view/tree-view.component'

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

  /** ZooKeeper loaded data */
   zooKeeperData: Object;
   nodeConf : string;

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
                public treeView: TreeViewComponent,
                private changeDetectionRef : ChangeDetectorRef) { }


 /** Preparing component before initialization loading data needed to show on page load */
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

 /** Method that returns a list of all registered icons available */
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
    this.changeDetectionRef.detectChanges();
    this.scrollToLogPos();
  }
  reloadData(){
      this.treeView.loadZooKeeperNodeData(this.pathToData);
      this.ngOnInit();
  }

 /** Method to set and load all logs data */
   loadZooKeeperNodeData(pathToData){
        try{
           this.nodeConf = this.treeView.nodeConf;
           this.zooKeeperData = this.treeView.zooKeeperData["data"];
           this.logArray = Object.keys(this.zooKeeperData);
           let result = this.zooKeeperData[this.logChoiceName];
           this.loadLogData(result);
           this.responseShow("good")
        }
        catch (err){
          this.responseShow("bad");
          console.log("Problem on default node while trying to prepare the showing of node data AGENT LOGS", err);
        }
   }

 /** Method for calling functions needed to prepare the log data to be shown */
  loadLogData(logData){
     this.logDataFull = this.splitLines(logData);
     this.logData = this.logDataFull.slice(-1*this.logCount);
     this.logLinesLoaded = this.logData.length;
     this.logLinesAvailable = this.logDataFull.length;
     this.responseShow("good")
  }

  /** Method for splitting data on new line */
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

  /** Method scroll to log Bottom */
  scrollToBottom(): void {
     try {
         this.myScrollContainerFakeNav.nativeElement.scrollTop = this.myScrollContainerFakeNav.nativeElement.scrollHeight;
     } catch(err) {
         console.log("Problem while scrolling to bottom of log" , err);
      }
  }

  /** Method filter logs data */
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

  /** Method to load more data into log object to show in component field */
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

  /** Method to scroll to the same log value after loading more data */
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

  /** Method used for different log choices */
  logChoice(choice){
     try {
       this.logChoiceName = choice;
       this.loadZooKeeperNodeData(this.pathToData);
     } catch(err) {
         console.log("Problem while changing log choice and reloading data" , err);
      }
 }

  /*Response variables set to good, bad or else for showing the data loading state */
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
      this.logData=[];
      this.valueThatChangesOnDataLoad = false;
      this.valueThatChangesForSpinnerOnResponse = true;
    }
  }


}
