import { Component, NgModule, ViewChild, OnInit } from '@angular/core';
import { DataService } from '../data.service';
import { Injectable } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";

import { ControlUtils } from "../utils/control.utils";
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginator , MatTableDataSource, MatSort } from '@angular/material';

import { TreeViewComponent } from '../tree-view/tree-view.component'


@Injectable({
  providedIn: 'root'
})
export class incompleteBlocks {
  activityName: string;
  txCount: number;
  eventCount: number;
  startTime: number;
  linkToBlock : String;
}

@Component({
  selector: 'app-incomplete-blocks',
  templateUrl: './incomplete-blocks.component.html',
  styleUrls: ['./incomplete-blocks.component.scss']
})
export class IncompleteBlocksComponent implements OnInit {

  /** Url address */
  pathToData : string;

/** Data table properties */
  displayedColumns: string[] = ['reason', 'activityName','startTime',  'count',  'control'];
  dataSource = new MatTableDataSource();
  @ViewChild(MatPaginator, { static: false }) paginator: MatPaginator;
  @ViewChild(MatSort, { static: false }) sort: MatSort;

/** incomplete blocks transaction properties */
  message = '';
  linkToBlock: string;
  fullBlocksDataFromJKool: [];
  incompleteBlocksData: incompleteBlocks[];

/** incomplete blocks no receipt properties */
  incompleteBlocksNoReceiptFull: [];
  incompleteBlocksDataNoReceipt: incompleteBlocks[];


/** Object to read data from node */
  serviceControlList = [];

/** Service stream needed properties */
  serviceName: string;
  result;

/** Front end prop */
  valueThatChangesOnDataLoad = true;
  valueThatChangesForSpinnerOnResponse = false;

    /** ZooKeeper loaded data */
   zooKeeperData: Object;
   nodeConf : string;

  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService,
                private matSpinner: MatProgressSpinnerModule,
                private controlUtils : ControlUtils,
                public treeView: TreeViewComponent) {
  }

  public ngOnInit() {
    this.responseShow("");
    this.pathToData = this.router.url.substring(1);
    this.loadZooKeeperNodeData(this.pathToData);
  }
  reloadData(){
      this.treeView.loadZooKeeperNodeData(this.pathToData);
      this.ngOnInit();
  }
   loadZooKeeperNodeData(pathToData){
    try{
      this.nodeConf = this.treeView.nodeConf;
      this.zooKeeperData = this.treeView.zooKeeperData["data"];
      this.serviceName =  this.nodeConf["blockchain"];
      this.loadIncompleteBlocksData(this.zooKeeperData);
    }
    catch (err){
      this.responseShow("bad");
      console.log("Problem on default node while trying to prepare the showing of node data AGENT LOGS", err);
    }
//      this.data.getZooKeeperNodeData(pathToData).subscribe( data => {
//        try{
//          this.zooKeeperData = data;
//          let result =  JSON.parse(this.zooKeeperData.toString());
//           console.log( result)
//          this.serviceName = result["config"]["blockchain"];
//          this.result = result["data"];
//          this.loadIncompleteBlocksData(this.result);
//        }catch(err){
//          this.responseShow("bad");
//          console.log("Problem while trying to read data from incomplete blocks", err);
//        }
//
//      },
//       err =>{
//         this.responseShow("bad");
//         console.log("Problem on reading incomplete blocks data: ", err);
//       }
//      );
   }

/** Methods for No Receipt data ----------------------------------------------*/
  loadIncompleteBlocksDataNoReceipt(linkUrlAddress){
    let re1 = new RegExp("jk_maxrows=100");
    let queryToJkool = linkUrlAddress.replace(re1, "jk_maxrows=1000");
    console.log(" <-----> JKool  Blocks No Receipt", linkUrlAddress);
    this.responseShow("");
    this.data.getLinkData(linkUrlAddress).subscribe(data => {
      try{
        setTimeout(() => this.dataSource.paginator = this.paginator);
        this.incompleteBlocksNoReceiptFull = data['rows'];
        console.log(" <-----> JKool  Blocks No Receipt", data);
          this.getNeededDataFromBlockNoReceipt(this.incompleteBlocksNoReceiptFull );
          this.formatData( this.incompleteBlocksDataNoReceipt, "incompleteNoReceipt");
          this.createTableWhenDataLoaded();
      }catch(err){
        this.responseShow("bad");
        console.log("Problem while trying to read data from incomplete blocks no receipt", err);
      }
      this.responseShow("good")
    },
     err =>{
       this.responseShow("bad");
       console.log("Problem on reading incomplete blocks no receipt data: ", err);
     });
  }

  getNeededDataFromBlockNoReceipt(dataBlockRows){
      let index = 0;
      let objTempRows= [];
      try{
        let neededData = this.configurationHandler.CONFIG["NeededDataToGetJKoolIncompleteBlocksNoReceipt"];
        for(let dataBlockRow of dataBlockRows){
              let objTemp= [];
              for(let neededDataName in neededData){
                let dataKey = neededData[neededDataName];
                if(typeof dataBlockRow[dataKey] !== this.utilsSvc.theValueUsedIsNotSet){
                  if(this.utilsSvc.compareStrings(neededData[neededDataName],"Count(EventName)")){
                    objTemp["reason"]="Missing receipt";// for tx: "+dataBlockRow[dataKey];
                     objTemp["count"] = dataBlockRow[dataKey];
                  }
                  else{
                    objTemp[dataKey]=dataBlockRow[dataKey];
                  }
                }
                else{
                 for(let neededDataNameInner in neededData[neededDataName]){
                   for(let dataInRow in dataBlockRow){
                     if(this.utilsSvc.compareStrings(neededDataNameInner, dataInRow) && this.utilsSvc.compareStrings(neededData[neededDataName][neededDataNameInner],"blockNumber")){
                        objTemp["ActivityName"]=dataBlockRow[dataInRow][dataKey[dataInRow]];
                        objTemp["linkToBlock"]= this.linkToGocypherBlock(dataBlockRow[dataInRow][dataKey[dataInRow]]);
                     }
                   }
                 }
                }
              }
              objTempRows[index] = objTemp;
              index++;
          }
          this.incompleteBlocksDataNoReceipt = objTempRows;
      } catch(err){
        console.log("Problem occurred while trying to form the incomplete blocks no receipt dataObject ", err);
      }
    }

/** Methods for wrong event count data ----------------------------------------------*/
  loadIncompleteBlocksData(linkUrlAddress){
      this.responseShow("");
      let re1 = new RegExp("jk_maxrows=100");
      let queryToJkool = linkUrlAddress.replace(re1, "jk_maxrows=1000");
      this.data.getLinkData(queryToJkool).subscribe(data => {
        try{
          this.fullBlocksDataFromJKool = data['rows'];
          console.log(" <-----> JKool  Blocks Transaction count mismatch", data);
            setTimeout(() => this.dataSource.paginator = this.paginator);
            this.getNeededDataFromBlockRows(this.fullBlocksDataFromJKool);
            let tempData = this.incompleteBlocksData;
            this.formatData(this.incompleteBlocksData, "incompleteEventCount");
            this.createTableWhenDataLoaded();
        }catch(err){
          console.log("Problem while trying to read data from incomplete blocks", err);
        }
        this.responseShow("good");
     },
     err =>{
       this.responseShow("bad");
       console.log("Problem on reading incomplete blocks data: ", err);
     });
  }

/**
*
*TO DO check if the incomplete items should have any controls or smth like that.
*
*/
  public neededServiceControls(controls){
  console.log(controls)
    if(!this.utilsSvc.compareStrings(controls['capabilities'], 'undefined')){
      controls = controls['capabilities'].substring(1,controls['capabilities'].length - 1 ).split(',');
      let tempArray = [];
        for( let data of controls){
              console.log(controls)
          tempArray.push(data);
        }
        this.serviceControlList = tempArray;
    }
  }

  getNeededDataFromBlockRows(dataBlockRows){
    let index = 0;
    let objTempRows = [];
    let tempArr = [];
    try{
    if(dataBlockRows.length > 0){
      let tempArr = Object.keys(dataBlockRows[0]);

       if(tempArr.includes("ActivityName")){
        let neededData = this.configurationHandler.CONFIG["NeededDataToGetJKoolIncompleteBlocks"];
        for(let dataBlockRow of dataBlockRows){

        let objTemp= [];
          for(let neededDataName in neededData){
            let dataKey = neededData[neededDataName];
            if(this.utilsSvc.compareStrings(neededData[neededDataName],"ActivityName")){
              if(typeof dataBlockRow[dataKey] !== this.utilsSvc.theValueUsedIsNotSet){
                objTemp["linkToBlock"]= this.linkToGocypherBlock(dataBlockRow[dataKey]);
              }

            }
            if(typeof dataBlockRow[dataKey] !== this.utilsSvc.theValueUsedIsNotSet){
              objTemp[dataKey]=dataBlockRow[dataKey];
            }
            else{
             for(let neededDataNameInner in neededData[neededDataName]){
               for(let dataInRow in dataBlockRow){
                 if(this.utilsSvc.compareStrings(neededDataNameInner, dataInRow)){
                    objTemp[dataKey[dataInRow]]=dataBlockRow[dataInRow][dataKey[dataInRow]];
                 }
               }
             }
            }
          }
          let tempCount = objTemp["txCount"]-objTemp["EventCount"];
          objTemp["reason"] = "Missing transactions";
          objTemp["count"] = tempCount;
          objTempRows[index] = objTemp;
          index++;
        }
        this.incompleteBlocksData = objTempRows;
        }
        else{
         // this.loadIncompleteBlocksDataNoReceipt(this.result);
          this.incompleteBlocksNoReceiptFull = this.fullBlocksDataFromJKool;
           //console.log(" <-----> JKool  Blocks No Receipt",  this.fullBlocksDataFromJKool);
           this.getNeededDataFromBlockNoReceipt(this.incompleteBlocksNoReceiptFull );
           this.formatData( this.incompleteBlocksDataNoReceipt, "incompleteNoReceipt");
           this.createTableWhenDataLoaded();
        }
      }
      else{
        this.responseShow("bad");
      }
    } catch(err){
      console.log("Problem occurred while trying to form the incomplete blocks no receipt dataObject ", err);
    }
  }

/** Methods for all data ----------------------------------------------*/
  createTableWhenDataLoaded(){
    try{
        if(typeof this.incompleteBlocksData !== 'undefined' && typeof this.incompleteBlocksDataNoReceipt !== 'undefined'){
          //console.log(this.incompleteBlocksData, this.incompleteBlocksDataNoReceipt)
          let tryArr = this.incompleteBlocksDataNoReceipt.concat(this.incompleteBlocksData);
          this.dataSource = new MatTableDataSource<incompleteBlocks>( tryArr);
          this.responseShow("good");
        }
        else if(typeof this.incompleteBlocksData !== 'undefined'){
          this.dataSource = new MatTableDataSource<incompleteBlocks>( this.incompleteBlocksData);
          this.responseShow("good");
        }
        else if(typeof this.incompleteBlocksDataNoReceipt !== 'undefined') {
          this.dataSource = new MatTableDataSource<incompleteBlocks>(this.incompleteBlocksDataNoReceipt);
          this.responseShow("good");
        }
        else{
          this.responseShow("bad");
        }
    } catch(err){
      console.log("Problem occurred while trying to create data table", err);
    }
  }



  linkToGocypherBlock(blockName): string{
    try{
      let capitalLetterRegEx  = ".*[A-Z]+.*"
      let activityName = parseFloat(blockName);
      let service = this.serviceName;
      service = service.replace(/([a-z])([A-Z])([a-z]+)/g, '$1');
      return this.linkToBlock = "https://www.gocypher.com/gocypher/"+service+"/block/"+ activityName;
    }catch(err){
      console.log("Problem occurred while creating link to GoCypher fro incomplete block", err);
    }
  }

  fakeComponentReload(){
    this.valueThatChangesOnDataLoad = false;
    this.ngOnInit();
  }

  formatData(dataObject, objective){
    try{
      for(let dataBlockRow in dataObject){
        for(let data in dataObject[dataBlockRow]){
          if(this.utilsSvc.compareStrings(objective, "incompleteEventCount")){
            this.incompleteBlocksData[dataBlockRow][data] = this.utilsSvc.formatIncompleteBlocksData(data, dataObject[dataBlockRow][data]);
          }
          else if(this.utilsSvc.compareStrings(objective, "incompleteNoReceipt")){
           this.incompleteBlocksDataNoReceipt[dataBlockRow][data] = this.utilsSvc.formatIncompleteBlocksData(data, dataObject[dataBlockRow][data]);
          }
        }
      }
    } catch(err){
      console.log("Problem occurred while trying to format the data ", err);
    }
  }


 applyFilterIncompleteBlocks(filterValue: string) {
   this.dataSource.filter = filterValue.trim().toLowerCase();
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


  public replayTheBlockFromInput(blockNumber){
    let activityName = blockNumber.replace(/,/g, '');
    console.log("Trying to replay block "+ activityName+" ...");
    this.controlUtils.replayBlock(this.pathToData, activityName);
  }

}
