import { Component, OnInit,  ViewChild} from '@angular/core';
import { Router } from '@angular/router';

import { MatIconRegistry } from "@angular/material";
import { MatPaginator , MatTableDataSource, MatSort } from '@angular/material';
import { DomSanitizer } from "@angular/platform-browser";
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';
import {animate, state, style, transition, trigger} from '@angular/animations';

@Component({
  selector: 'app-agent-runtime',
  templateUrl: './agent-runtime.component.html',
  styleUrls: ['./agent-runtime.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class AgentRuntimeComponent implements OnInit {

  /*Table for data from Clusters runtime info*/
  @ViewChild('paginatorClusters') paginatorClusters: MatPaginator;
  @ViewChild('sortClusters') sortClusters: MatSort;
  dataSourceClusters = new MatTableDataSource<any>();
  columnsToDisplayClusters = [];


  /*Table for data from Cluster runtime info*/
  @ViewChild('paginatorCluster') paginatorCluster: MatPaginator;
  @ViewChild('sortCluster') sortCluster: MatSort;
  dataSourceCluster = new MatTableDataSource<any>();
  columnsToDisplayCluster = [];


/*Table for data from service*/
 @ViewChild('paginatorService') paginatorService: MatPaginator;
 @ViewChild('matServiceSort') sortService: MatSort;

/*Table for clusters and cluster view page*/
  healthyNodes = [];
  healthyServices = [];
  allServiceNodes = [];
  agentsRuntimeData = [];
  servicesDataForClusterPage = [];


/*Table for data from service info*/
  dataSource = "";
  columnsToDisplay = [];
  serviceTableLabels = [];
  expandedElement: MatTableDataSource<any>;
  tempServiceData = [];
  dataSourceService= new MatTableDataSource<any>();

/*Table for data from agent runtime info*/
  @ViewChild('paginatorRuntime') paginatorAgent: MatPaginator;
  @ViewChild('matServiceAgentSort') sortAgentRuntime: MatSort;

 displayedColumns = ['name', 'value'];
 dataSourceRuntime = new MatTableDataSource<any>();


  /** Url address */
  pathToData : string;
  pathToServiceData = [];

  /** Stream agent data */
  agentRuntimeInfo: [];
  childrenParentNodes: [];
  zooKeeperData: Object;
  serviceBaseMetrics: Object
  serviceConfiguration = [];

  /** Values for showing data loading properties */
  valueThatChangesOnDataLoad = false;
  valueThatChangesForSpinnerOnResponse = false;

  streamDataShowChoice: string;
  iconsRegistered = [];

  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService,
                private matIconRegistry: MatIconRegistry,
                private domSanitizer: DomSanitizer) { }

  ngOnInit() {
    this.pathToData = this.router.url.substring(1);
    this.loadZooKeeperNodeData(this.pathToData);
    this.iconsRegistered = this.utilsSvc.getAllRegisteredIonsList();
  }

   loadZooKeeperNodeData(pathToData){
      this.dataSourceClusters = new MatTableDataSource<any>();
      this.dataSourceCluster = new MatTableDataSource<any>();
      this.responseShow("");
      this.data.getZooKeeperNodeData(pathToData).subscribe( data => {
          this.zooKeeperData = data;
          let result =  JSON.parse(this.zooKeeperData.toString());
          console.log("PARENT DATA", result);
          if(this.utilsSvc.isObject(result)){
            this.streamDataShowChoice = result["config"]["componentLoad"];
              this.prepareServiceDataTable(result);
              result = result['data'];
              this.agentRuntimeInfo = result;
              this.prepareRuntimeData(this.agentRuntimeInfo);
              setTimeout(() => this.dataSourceRuntime.paginator = this.paginatorAgent);
              setTimeout(() => this.dataSourceRuntime.sort = this.sortAgentRuntime);
          }
          else{
            this.responseShow("bad");
            this.agentRuntimeInfo = result;
          }
      },
       err =>{
         this.responseShow("bad");
         console.log("Problem on reading threads data: ", err);
       }
      );
   }

   prepareClusterViewTableData(agentRuntimeInfo, parentsInChildrenData, parentName, clustersView){
    let dataTemp = Object.keys(parentsInChildrenData);
    let tempHealthyCount = 0;
    for(let key of dataTemp){
      if(clustersView){
      /** READING THE DATA FROM INSIDE THE SERVICE IN CLUSTER VIEW */
        let tempPath = this.router.url.substring(1)+'/'+parentName+'/'+key;
          this.data.getZooKeeperNodeData(tempPath).subscribe( data => {
             let result = data;
             result = JSON.parse(result.toString());
             let serviceData = result['data'];
             this.agentsRuntimeData[tempPath] = this.neededDataFromRunTimeClustersPage(serviceData);
             this.prepareTableForClustersView( this.agentsRuntimeData[tempPath], tempPath )
            // console.log("The data right after setting" , this.agentsRuntimeData);
             let serviceNodes = result['parentsInChildren'];
             let tempKeysData = Object.keys(serviceNodes);
             let tempCountTrueFalse = false;
             for(let keysObj of tempKeysData){
                if(serviceNodes[keysObj]){
                  this.healthyServices[parentName+'/'+this.utilsSvc.getNodePathEnd(tempPath)] = true;
                  tempCountTrueFalse = true;
                }
                else{
                  this.healthyServices[parentName+'/'+this.utilsSvc.getNodePathEnd(tempPath)] = false;
                }
              }
              if(tempCountTrueFalse){
                tempHealthyCount = tempHealthyCount + 1;
              }
             this.healthyNodes[parentName] = tempHealthyCount;
          });
      }
      else if(!this.utilsSvc.compareStrings(parentsInChildrenData[key],'Nan')){
          let tempPath = this.router.url.substring(1)+'/'+parentName+'/'+key;
            this.data.getZooKeeperNodeData(tempPath).subscribe( data => {
               let result = data;
               result = JSON.parse(result.toString());
               let serviceData = result['data'];
               let serviceNodes = result['parentsInChildren']
               this.servicesDataForClusterPage[tempPath] = serviceData;
               let tempServiceDataValue =  this.serviceDataForCluster(serviceData, tempPath,"StreamsDataForClusterPage");
               if(parentsInChildrenData[key]){
                 this.prepareTableForClusterView(tempServiceDataValue, tempPath, true);
                 this.healthyServices[parentName+'/'+this.utilsSvc.getNodePathEnd(tempPath)] = true;
                 tempHealthyCount = tempHealthyCount + 1;
               }
               else{
                 this.prepareTableForClusterView(tempServiceDataValue, tempPath, false);
                 this.healthyServices[parentName+'/'+this.utilsSvc.getNodePathEnd(tempPath)] = false;
               }
            });

      }
    }
    this.healthyNodes[parentName] = tempHealthyCount;
    this.allServiceNodes[parentName] = dataTemp.length;
   }

   /*Add data to object for table component*/
   serviceDataForCluster(serviceData, name, neededDataConfig){
     this.columnsToDisplay = [];
     let neededData = this.configurationHandler.CONFIG[neededDataConfig];
     let tempServiceInformation = [];
     if(!this.utilsSvc.compareStrings(serviceData, "null")){
       for(let keyConfig in neededData){
         if(!this.utilsSvc.compareStrings(serviceData[keyConfig], "undefined")){
           let tempKey = Object.keys(neededData[keyConfig]);
           let data = this.utilsSvc.formatData(keyConfig, serviceData[keyConfig]);
           tempServiceInformation[Object.keys(neededData[keyConfig])[0]] = data;
           this.serviceTableLabels[keyConfig]=tempKey;
         }
         else{
           let tempKey = Object.keys(neededData[keyConfig]);
           tempServiceInformation[Object.keys(neededData[keyConfig])[0]] = neededData[keyConfig][tempKey];
           this.serviceTableLabels[keyConfig]=tempKey;
         }
       }
     }
      this.tempServiceData[name]=tempServiceInformation;
      return this.tempServiceData[name];
   }

   neededDataFromRunTimeClustersPage(agentData): any[]{
      let tempArr = [];
      for( let data in agentData){
        if(this.utilsSvc.compareStrings(data, 'Operating system')){
         for( let dataInside in agentData[data]){
              if(this.utilsSvc.compareStrings(dataInside, 'OS')){
                tempArr[dataInside]=agentData[data][dataInside];
              }
            }
        }
        else if(this.utilsSvc.compareStrings(data, 'Network')){
         for( let dataInside in agentData[data]){
             tempArr[dataInside]=agentData[data][dataInside];
         }
        }
        else if(this.utilsSvc.compareStrings(data, 'Versions')){
          for( let dataInside in agentData[data]){
            if(this.utilsSvc.compareStrings(dataInside, 'Streams Core version')){
               tempArr[dataInside]=agentData[data][dataInside];
            }
          }
        }
      }
      //console.log("DATA FROM AGENT FOR CLUSTERS VIEW", tempArr);
      return tempArr;
   }

   sortData(){ }

/*Calls To different methods in order to get and show services base stats*/
   prepareServiceDataTable(result){
      this.childrenParentNodes = result["parentsInChildren"];
      this.getTheListOfPathToServices(this.childrenParentNodes);
      this.getTheServicesBaseData();
   }

/* The list of paths needed to get the service base stats */
 getTheListOfPathToServices(services){
   for(let name in services){
     this.pathToServiceData[name] =  this.pathToData + '/' + name;
   }
 }

/* Getting the data about all the services in agent base stats*/
 getTheServicesBaseData(){
   this.tempServiceData = [];
   for(let name in  this.pathToServiceData){
      this.data.getZooKeeperNodeData(this.pathToServiceData[name]).subscribe( data => {
        let result = data;
        result = JSON.parse(result.toString());
        //console.log("Children data: ",result)
        this.serviceBaseMetrics = result["data"];
        this.serviceConfiguration = result["config"];
        if( this.utilsSvc.compareStrings(this.serviceConfiguration["componentLoad"],'service')){
          this.serviceTableNeededData(this.serviceBaseMetrics, name, "StreamsDataForAgentPage");
          this.columnsToDisplay.push("control");
          this.neededServiceControls(this.serviceConfiguration , name);
          this.serviceTableLabels["control"]="Stream Control";
          this.dataSourceService = new MatTableDataSource(this.tempServiceData);
          setTimeout(() => this.dataSourceService.paginator = this.paginatorService);
          setTimeout(() => this.dataSourceService.sort = this.sortService);
        }
        else if(this.utilsSvc.compareStrings(this.serviceConfiguration["componentLoad"],'agent')){
          this.prepareClusterViewTableData( this.serviceBaseMetrics, result['parentsInChildren'],  this.serviceConfiguration['nodeName'], false);
          setTimeout(() => this.dataSourceCluster.paginator = this.paginatorCluster, 300);
          setTimeout(() => this.dataSourceCluster.sort = this.sortCluster, 300);
          this.responseShow("good");
        }
        else{
          this.prepareClusterViewTableData( this.serviceBaseMetrics, result['parentsInChildren'],  this.serviceConfiguration['nodeName'], true);
          setTimeout(() => this.dataSourceClusters.paginator = this.paginatorClusters, 300);
          setTimeout(() => this.dataSourceClusters.sort = this.sortClusters, 300);
          this.responseShow("good");
        }
      });
   }
 }

 prepareTableForClustersView(agentsRuntimeData, agentPath){
     let value = [];
     try{
       for(let clusterName in this.childrenParentNodes){
          let tempClustersInfo = [];
          if(agentPath.includes(clusterName)){
            tempClustersInfo["ClusterName"]=clusterName;
            //.match(/[A-Z][a-z]+|[0-9]+/g).join(" ")
            tempClustersInfo["AgentName"]=agentPath;
            this.columnsToDisplayClusters.indexOf("AgentName") === -1 ? this.columnsToDisplayClusters.push("AgentName"): "";
            this.columnsToDisplayClusters.indexOf("ClusterName") === -1 ? this.columnsToDisplayClusters.push("ClusterName"): "";
            for(let data in agentsRuntimeData){
                tempClustersInfo[data]= agentsRuntimeData[data];
                this.columnsToDisplayClusters.indexOf(data) === -1 ? this.columnsToDisplayClusters.push(data): "";
            }
            if(this.utilsSvc.compareStrings(this.dataSourceClusters, 'undefined')){
              this.dataSourceClusters = new MatTableDataSource(tempClustersInfo);
            }
            else{
              this.dataSourceClusters.data.push(tempClustersInfo);
              this.dataSourceClusters = new MatTableDataSource(this.dataSourceClusters.data);
            }
          }
       }
     }
     catch(e){
       console.log("Problem on preparing clusters info for table: ", e)
     }
 }
 prepareTableForClusterView(serviceInformation, pathToService, serviceStatus){
     let value = [];
     try{
       for(let agentName in this.childrenParentNodes){
       let tempClusterInfo = [];
           if(pathToService.includes(agentName+'/')){
             tempClusterInfo["AgentName"]=agentName;
             tempClusterInfo["StreamName"]=pathToService;
             if(serviceStatus){
               tempClusterInfo["Status"]=true;
             }
             else{
              tempClusterInfo["Status"]=false;
             }
             this.columnsToDisplayCluster.indexOf("AgentName") === -1 ? this.columnsToDisplayCluster.push("AgentName"): "";
             this.columnsToDisplayCluster.indexOf("StreamName") === -1 ? this.columnsToDisplayCluster.push("StreamName"): "";
               for( let serviceDataName in serviceInformation){
                 tempClusterInfo[serviceDataName]=serviceInformation[serviceDataName];
                 this.columnsToDisplayCluster.indexOf(serviceDataName) === -1 ? this.columnsToDisplayCluster.push(serviceDataName): "";
               }
             this.columnsToDisplayCluster.indexOf("Status") === -1 ? this.columnsToDisplayCluster.push("Status"): "";
             value.push(tempClusterInfo);
             if(this.utilsSvc.compareStrings(this.dataSourceCluster, 'undefined')){
                this.dataSourceCluster = new MatTableDataSource(tempClusterInfo);
             }
             else{
                this.dataSourceCluster.data.push(tempClusterInfo);
                this.dataSourceCluster = new MatTableDataSource(this.dataSourceCluster.data);
             }
           }
       }
     }
     catch(e){
       console.log("Problem on preparing cluster info for table: ", e)
     }
 }

/*Add data to object for table component*/
  serviceTableNeededData(serviceData, name, neededDataConfig){
    this.columnsToDisplay = [];
    let neededData = this.configurationHandler.CONFIG[neededDataConfig];
    let tempServiceInformation = [];
    tempServiceInformation["description"] = this.serviceTableExpandableData(this.serviceBaseMetrics, name, neededData);
    tempServiceInformation["Stream"] = this.utilsSvc.getNodePathEnd(name);
    this.columnsToDisplay.push("Stream");
    if(!this.utilsSvc.compareStrings(serviceData, "null")){
    this.serviceTableLabels["Stream"]="Stream";
      for(let keyConfig in neededData){
        if(!this.utilsSvc.compareStrings(serviceData[keyConfig], "undefined")){
          let tempKey = Object.keys(neededData[keyConfig]);
          let data = this.utilsSvc.formatData(keyConfig, serviceData[keyConfig]);
          tempServiceInformation[keyConfig] = data;
          this.columnsToDisplay.push(keyConfig);
          this.serviceTableLabels[keyConfig]=tempKey;
        }
        else{
          let tempKey = Object.keys(neededData[keyConfig]);
          tempServiceInformation[keyConfig] = neededData[keyConfig][tempKey];
          this.columnsToDisplay.push(keyConfig);
          this.serviceTableLabels[keyConfig]=tempKey;
        }
      }
    }
    this.tempServiceData.push(tempServiceInformation);
    this.responseShow("good");
  }

  serviceTableExpandableData(serviceData, name, config){
      let serviceName = this.utilsSvc.getNodePathEnd(name);
      let configs = Object.keys(config);
      let tempServiceInformation = [];
       for(let key in serviceData){
           if(!this.utilsSvc.isObject(serviceData[key])){
              if(!configs.includes(key)){
                let data = this.utilsSvc.formatData(key, serviceData[key]);
                tempServiceInformation[key] = data;
              }
          }
       }
       return tempServiceInformation;
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

/*The information about agent runtime prepared for material table*/
  prepareRuntimeData(agentInfo){
      let value = [];
      try{
        for(let key in agentInfo){
        let tempAgentRuntimeInfo = [];
        let tempFormatAgent = [];
            tempAgentRuntimeInfo["name"]=key;
            for(let data in agentInfo[key]){
                tempFormatAgent[data] = this.utilsSvc.formatData(data, agentInfo[key][data])
            }
            agentInfo[key] = tempFormatAgent;
            tempAgentRuntimeInfo["value"]= agentInfo[key];
            value.push(tempAgentRuntimeInfo);
        }
        this.dataSourceRuntime = new MatTableDataSource(value);
        this.responseShow("good");
      }
      catch(e){
        console.log("Problem on preparing agent runtime info for table: ", e)
      }
  }

/* Filterring the material table data by input field */
  applyFilterRuntime(filterValue: string) {
    this.dataSourceRuntime.filter = filterValue.trim().toLowerCase();
  }
  applyFilterService(filterValue: string) {
    this.dataSourceService.filter = filterValue.trim().toLowerCase();
  }
  applyFilterCluster(filterValue: string) {
    this.dataSourceCluster.filter = filterValue.trim().toLowerCase();
  }
  applyFilterClusters(filterValue: string) {
    console.log( this.dataSourceClusters);
    this.dataSourceClusters.filter = filterValue.trim().toLowerCase();
  }

  /** Stream control window properties */
    streamStartStop = "Stop";
    streamPauseResume = "Pause";
    blockNumber="";

    serviceControlList = [];
    someData = [];


    public neededServiceControls(controls, name){
      if(!this.utilsSvc.compareStrings(controls['capabilities'], 'undefined')){
      let tempData = controls['capabilities'];
        controls = tempData.toString().substring(1, tempData.length - 1 ).split(',');
        let tempArray = [];
          for( let data of controls){
            tempArray.push(data);
          }
          this.serviceControlList[name] = tempArray;
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
