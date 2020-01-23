import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

import {MatPaginator, MatTableDataSource, MatSort } from '@angular/material';
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';

import { TreeViewComponent } from '../tree-view/tree-view.component'


@Component({
  selector: 'app-service-repository-status',
  templateUrl: './service-repository-status.component.html',
  styleUrls: ['./service-repository-status.component.scss']
})
export class ServiceRepositoryStatusComponent implements OnInit {


/** Properties for material table */
 displayedColumns = ['name', 'value'];
 dataSourceRepositoryData = new MatTableDataSource<any>();

 @ViewChild('matRepositoryData') sortRepositoryData: MatSort;
 @ViewChild('paginatorRepository') paginatorRepoData: MatPaginator;

  /** Url address */
  pathToData : string;

  /** Service repository status data */
  repositoryData: [];

  /** Values for showing data loading properties */
  valueThatChangesOnDataLoad = false;
  valueThatChangesForSpinnerOnResponse = false;

  /** Repository properties */
    tempServiceRepo: Object;
    dataFromRepositories: Object;
    dataNeededFromRepositories = [];
    serviceRepo = "";

  /** ZooKeeper loaded data */
   zooKeeperData: Object;
   nodeConf : string;

  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService,
                public treeView: TreeViewComponent) { }

  ngOnInit() {
    this.prepareRepositoryDefaultData("ReposDataForAllPage");
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
         this.getRepositoryData(this.zooKeeperData );
       }
       catch (err){
         this.responseShow("bad");
         console.log("Problem on default node while trying to prepare the showing of node data AGENT LOGS", err);
       }
   }

   getRepositoryData(repoData){
    this.responseShow("");
       this.tempServiceRepo = repoData;
       if(repoData.rows.length==0){
           this.responseShow("bad");
       }else{
         this.getAllNeededInfoAboutRepos(this.tempServiceRepo, "ReposDataForAllPage");
         this.dataFromRepositories = this.tempServiceRepo;
         this.prepareRepositoryData( this.dataNeededFromRepositories);
         setTimeout(() => this.dataSourceRepositoryData.paginator = this.paginatorRepoData);
         setTimeout(() => this.dataSourceRepositoryData.sort = this.sortRepositoryData);
         this.responseShow("good");
       }
   }

    getAllNeededInfoAboutRepos(data, strRepoConfig){
      try{
         let confDefaultData = this.configurationHandler.CONFIG[strRepoConfig];
         let arrayTemp = [];
         let regExpSum = /(SUM[a-z])/;
         let regExpAvg = /(AVG[a-z])/;
         if(!this.utilsSvc.compareStrings(data,this.utilsSvc.theValueUsedIsNotSet)){
          if(data['rows']!=0 && !this.utilsSvc.compareStrings(data['rows'], "undefined")){
             let rowsDataStatistic = data['rows'][0]['Statistics'];

             for(let confFirstLvlData in confDefaultData){
               let key = Object.keys(confDefaultData[confFirstLvlData]);

               let rowsCount = data['row-count'];
               if(this.utilsSvc.compareStrings(confFirstLvlData,"rowCount")){
                   arrayTemp[key[0]] = rowsCount;
               }
               else if(confFirstLvlData.match(regExpSum)){
                 let tempName = confFirstLvlData.replace(/(SUM)([a-z])/g, '$2');
                 let tempSum = 0;
                 for(let row of data['rows']){
                   rowsDataStatistic = row['Statistics'];
                    arrayTemp[key[0]]=tempSum + rowsDataStatistic[tempName];
                    tempSum =  arrayTemp[key[0]];
                 }
               }
               else if(confFirstLvlData.match(regExpAvg)){
                 let tempName = confFirstLvlData.replace(/(AVG)([a-z])/g, '$2');
                 let tempSum = 0;
                 for(let row of data['rows']){
                   let tempAverage = rowsDataStatistic[tempName] / data['row-count'];
                    arrayTemp[key[0]]=tempSum + tempAverage;
                    tempSum =  arrayTemp[key[0]];
                 }
               }
               else if(this.utilsSvc.compareStrings(rowsDataStatistic[confFirstLvlData],this.utilsSvc.theValueUsedIsNotSet)){
                 arrayTemp[key[0]] = confDefaultData[confFirstLvlData][key[0]];
               }
               else {
                arrayTemp[key[0]]= rowsDataStatistic[confFirstLvlData];
               }
               arrayTemp[key[0]] = this.utilsSvc.formatData(key[0], arrayTemp[key[0]]);
             }
             this.dataNeededFromRepositories=arrayTemp;
             this.responseShow("good");
            }
            else{
              for(let confFirstLvlData in confDefaultData){
                 let key = Object.keys(confDefaultData[confFirstLvlData]);
                 arrayTemp[key[0]] = confDefaultData[confFirstLvlData][key[0]];
               }
               this.dataNeededFromRepositories=arrayTemp;
               this.responseShow("good");
               console.log("No active repository streams");
            }
         }
         else{
           this.responseShow("good");
           for(let confFirstLvlData in confDefaultData){
             let key = Object.keys(confDefaultData[confFirstLvlData]);
             arrayTemp[key[0]] = confDefaultData[confFirstLvlData][key[0]];

           }
           this.dataNeededFromRepositories=arrayTemp;
         }
      }catch(err){
        this.responseShow("bad");
        console.log("Problem occurred while getting the needed data and formatting the data gotten from repository", err);
      }
    }

    /*The information about agent runtime prepared for material table*/
    prepareRepositoryDefaultData(strRepoConfig){
       let value = [];
       let confDefaultData = this.configurationHandler.CONFIG[strRepoConfig];
         for(let data in confDefaultData) {
         let tempKey = Object.keys(confDefaultData[data]);
              let tempAgentRuntimeInfo = [];
              tempAgentRuntimeInfo["name"]=tempKey;
              tempAgentRuntimeInfo["value"]=confDefaultData[data][tempKey];
              value.push(tempAgentRuntimeInfo);
        }
        this.dataSourceRepositoryData = new MatTableDataSource(value);
    }

    prepareRepositoryData(repoData){
        let value = [];
         for(let data in repoData) {
            if(!this.utilsSvc.isObject(repoData[data])){
              let tempAgentRuntimeInfo = [];
              tempAgentRuntimeInfo["name"]=data;
              tempAgentRuntimeInfo["value"]=repoData[data];
              value.push(tempAgentRuntimeInfo);
            }
        }
        this.dataSourceRepositoryData = new MatTableDataSource(value);
    }

     sortData(){
      this.dataSourceRepositoryData.sort = this.sortRepositoryData;
     }

     applyFilterRepositoryData(filterValue: string) {
       this.dataSourceRepositoryData.filter = filterValue.trim().toLowerCase();
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
