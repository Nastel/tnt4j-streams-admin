import { Component, OnInit,  ViewChild, AfterViewChecked} from '@angular/core';
import { Router } from '@angular/router';

import { MatIconRegistry } from "@angular/material";
import { MatPaginator , MatTableDataSource, MatSort } from '@angular/material';
import { DomSanitizer } from "@angular/platform-browser";
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';
import {animate, state, style, transition, trigger} from '@angular/animations';
import { TreeViewComponent } from '../tree-view/tree-view.component'

import { ControlUtils } from "../utils/control.utils";

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
  healthyServices = [];
  agentsRuntimeData = [];


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
  childrenParentNodes:string[];
  serviceBaseMetrics: Object
  serviceConfiguration = [];

  /** ZooKeeper loaded data */
  zooKeeperData: Object;
  nodeConf : string;

  /** Values for showing data loading properties */
  valueThatChangesOnDataLoad = false;
  valueThatChangesForSpinnerOnResponse = false;
  streamRegistryNode = this.configurationHandler.CONFIG["activeStreamRegistryNode"];
  agentStatus = true;

  streamDataShowChoice: string;
  iconsRegistered = [];

  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService,
                private matIconRegistry: MatIconRegistry,
                private domSanitizer: DomSanitizer,
                private controlUtils : ControlUtils,
                public treeView: TreeViewComponent) {}

  ngOnInit() {
    this.pathToData = this.router.url.substring(1);
    sessionStorage.setItem("pathBeforeReload", this.pathToData);
    this.loadZooKeeperNodeData(this.pathToData);
    this.iconsRegistered = this.utilsSvc.getAllRegisteredIonsList();
  }
  ngAfterContentInit(){
      setTimeout(() => this.dataSourceRuntime.paginator = this.paginatorAgent);
      setTimeout(() => this.dataSourceRuntime.sort = this.sortAgentRuntime);
  }

  reloadData(){
      this.treeView.loadZooKeeperNodeData(this.pathToData);
      setTimeout(() => this.dataSourceRuntime.paginator = this.paginatorAgent);
      setTimeout(() => this.dataSourceRuntime.sort = this.sortAgentRuntime);
  }

  /** Load data from node that is currently selected */
   loadZooKeeperNodeData(pathToData){
     try{
        this.dataSourceClusters = new MatTableDataSource<any>();
        this.dataSourceCluster = new MatTableDataSource<any>();
        this.responseShow("");
        this.zooKeeperData = this.treeView.zooKeeperData;
//        console.log("PARENT DATA", this.zooKeeperData);
        this.nodeConf = this.treeView.nodeConf;
        this.streamDataShowChoice = this.nodeConf["componentLoad"];
        this.prepareServiceDataTable(this.zooKeeperData["childrenNodes"] );
        for(let name in  this.pathToServiceData){
            this.neededServiceControls(this.nodeConf , name);
        }
        this.agentRuntimeInfo = this.zooKeeperData["data"];
        this.prepareRuntimeData(this.agentRuntimeInfo);
     } catch (err){
       this.controlUtils.openDialogWithHeader("A problem occurred while trying to load ZooKeeper node data: "+this.pathToData, "Error", this.pathToData);
       this.responseShow("bad");
       console.log("Problem on default node while trying to prepare the showing of node data AGENT RUNTIME", err);
     }
   }

  /** Reading data from agent in clusters view */
   prepareClusterViewTableData(parentsInChildrenData, parentName){
    for(let key of parentsInChildrenData){
      let tempPath = this.router.url.substring(1)+'/'+parentName+'/'+key;
        this.data.getZooKeeperNodeData(tempPath).subscribe( data => {
           let result = data;
           if(!this.utilsSvc.compareStrings(result['data'],"undefined")){

             console.log(result)
            // result = JSON.parse(result.toString());
             console.log(result)
             let serviceData = result['data'];
             this.agentsRuntimeData[tempPath] = this.neededDataFromRunTimeClustersPage(serviceData);
             this.prepareTableForClustersView( this.agentsRuntimeData[tempPath],parentsInChildrenData, parentName, tempPath )
             console.log(result);
             let tempKeysData = Object.keys(result['childrenNodes']);
             let tempCountTrueFalse = false;
             this.checkStreamStatus(tempPath, parentName)
           }
        },
        err =>{
           this.healthyServices[parentName+'/'+this.utilsSvc.getNodePathEnd(tempPath)] = false;
           this.responseShow("bad"); });
     }
   }

  checkStreamStatus(agentNodeName: string, streamName: string){
      let tempPath = agentNodeName+'/'+this.streamRegistryNode;
      let healthyPath = streamName+'/'+this.utilsSvc.getNodePathEnd(agentNodeName);
      this.data.getZooKeeperNodeData(tempPath).subscribe( data => {
          let result = data; //JSON.parse(data.toString());
          if(Object.keys(result).length === 0){
            this.healthyServices[healthyPath] = false;
          }
          else{
            for( let serviceName in result){
            console.log(serviceName);
              if(Object.keys(result[serviceName]).length !== 0 &&  !this.healthyServices[healthyPath]){
                  this.healthyServices[healthyPath] = true;
                  console.log(this.healthyServices[healthyPath]);
              }else{
                  this.healthyServices[healthyPath] = false;
                  console.log(this.healthyServices[healthyPath]);
              }
            }
          }
      },
      err =>{
           this.responseShow("bad");
      });
  }

  clusterViewData(parentName){
    let tempPath = this.router.url.substring(1)+'/'+parentName+'/'+this.streamRegistryNode;
    this.data.getZooKeeperNodeData(tempPath).subscribe( data => {
             let result = data; //JSON.parse(data.toString());
        if(Object.keys(result).length === 0 ){
          this.responseShow("bad");
          this.controlUtils.openDialogWithHeader("No data got from REST", "Error", this.pathToData)
        }
        else{
          for( let serviceName in result){
            let pathToServiceNode = tempPath + "/" + serviceName;
            let tempServiceDataValue =  this.serviceDataForCluster(result[serviceName], pathToServiceNode,"StreamsDataForClusterPage");
            if(Object.keys(result[serviceName]).length !== 0 && result[serviceName].constructor === Object){
              this.prepareTableForClusterView(tempServiceDataValue, pathToServiceNode, parentName, true);
              this.healthyServices[parentName+"/"+serviceName] = true;
            } else{
              this.prepareTableForClusterView(tempServiceDataValue, pathToServiceNode, parentName, false);
              this.healthyServices[parentName+"/"+serviceName] = false;
            }
          }
          this.responseShow("good");
        }
    },
    err =>{
         this.responseShow("bad");
    });
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
      return tempArr;
   }

/*Calls To different methods in order to get and show services base stats*/
   prepareServiceDataTable(result){
     this.childrenParentNodes = result;
      for(let name of result){
        this.pathToServiceData[name] =  this.pathToData + '/' + name;
      }
      this.getTheServicesBaseData();
   }

  prepareAgentPageView(){
      let tempPath = this.pathToData+'/'+this.streamRegistryNode;
      let agentName = this.utilsSvc.getNodePathEnd(this.pathToData);
      this.data.getZooKeeperNodeData(tempPath).subscribe( data => {
                  let result = data; //JSON.parse(data.toString());
          if(Object.keys(result).length === 0 ){
            this.responseShow("bad");
            this.controlUtils.openDialogWithHeader("No data got from REST", "Error", this.pathToData)
          }
          else{
            for( let serviceName in result){
               this.serviceTableNeededData(result[serviceName], serviceName, "StreamsDataForAgentPage");
//               this.columnsToDisplay.push("control");
//               this.serviceTableLabels["control"]="Stream Control";
               this.dataSourceService = new MatTableDataSource(this.tempServiceData);
               if(Object.keys(result[serviceName]).length !== 0 && result[serviceName].constructor === Object){
                 this.healthyServices[serviceName] = true;
               } else{
                 this.healthyServices[serviceName] = false;
               }
               console.log(   this.dataSourceService)
               setTimeout(() => this.dataSourceService.paginator = this.paginatorService, 500);
               setTimeout(() => this.dataSourceService.sort = this.sortService, 500);
               this.responseShow("good");
            }
          }
      },
      err =>{
           this.responseShow("bad");
      });
  }

/* Getting the data about all the services for agent view base stream stats*/
 getTheServicesBaseData(){
   this.tempServiceData = [];
   try{
   if( this.utilsSvc.compareStrings(this.streamDataShowChoice, "agent" )){
      this.prepareAgentPageView();
   }
     for(let name in  this.pathToServiceData){
        this.data.getZooKeeperNodeData(this.pathToServiceData[name]).subscribe( data => {
          let result = data;
          //result = JSON.parse(result.toString());

          this.serviceBaseMetrics = result["data"];
          this.serviceConfiguration = result["config"];
          if(!this.utilsSvc.compareStrings( this.serviceConfiguration , "undefined")){

            if(this.utilsSvc.compareStrings(this.serviceConfiguration["componentLoad"],'agent')){
              this.clusterViewData(name);
              setTimeout(() => this.dataSourceCluster.paginator = this.paginatorCluster, 1000);
              setTimeout(() => this.dataSourceCluster.sort = this.sortCluster, 1000);
              this.responseShow("good");
            }
            else if(this.utilsSvc.compareStrings(this.serviceConfiguration["componentLoad"],'cluster')){
              this.prepareClusterViewTableData(result['childrenNodes'], name);
              setTimeout(() => this.dataSourceClusters.paginator = this.paginatorClusters, 1000);
              setTimeout(() => this.dataSourceClusters.sort = this.sortClusters, 1000);
              this.responseShow("good");
            }
          }
          else{
            if(this.utilsSvc.compareStrings(result["dataReading"], "undefined") || this.utilsSvc.compareStrings(result["dataReading"], "") ){}
            else{
              console.log(this.zooKeeperData["Response link"])
              this.controlUtils.openDialogWithHeader(result["Response link"], result["dataReading"], this.pathToData);
            }
            console.log("Problem occurred:  no configuration data loaded")
          }
        });
     }
   }
   catch(err){
    console.log("Problem on preparing the data for table ", err)
   }
 }


/*Add data to object for table component fort stream table in agent view page*/
  serviceTableNeededData(serviceData, name, neededDataConfig){
    this.columnsToDisplay = [];
    let neededData = this.configurationHandler.CONFIG[neededDataConfig];
    let tempServiceInformation = [];
    tempServiceInformation["description"] = this.serviceTableExpandableData(serviceData, name, neededData);
    tempServiceInformation["Stream"] = this.utilsSvc.getNodePathEnd(name);
    console.log(tempServiceInformation);
    this.columnsToDisplay.push("Stream");
        console.log( this.columnsToDisplay);
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
    console.log( this.tempServiceData)
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


 /** Setting all the needed data for clusters view table */
 prepareTableForClustersView(agentsRuntimeData, parentsInChildrenData, parentName, agentPath){
     let value = [];
     try{
      let agentName =  this.utilsSvc.getNodePathEnd(agentPath);
          let tempClustersInfo = [];
          if(agentPath.includes(parentName)){
            tempClustersInfo["ClusterName"]=parentName;
            //.match(/[A-Z][a-z]+|[0-9]+/g).join(" ")
            tempClustersInfo["AgentName"]=agentPath;
            this.columnsToDisplayClusters.indexOf("AgentName") === -1 ? this.columnsToDisplayClusters.push("AgentName"): "";
//            this.columnsToDisplayClusters.indexOf("Control") === -1 ? this.columnsToDisplayClusters.push("Control"): "";
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
     catch(e){
       console.log("Problem on preparing clusters info for table: ", e)
     }
 }

 /** Setting all the needed data for cluster view table */
 prepareTableForClusterView(serviceInformation, pathToService, agentName, serviceStatus){
     let value = [];
     try{
       let tempClusterInfo = [];
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
     catch(e){
       console.log("Problem on preparing cluster info for table: ", e)
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
            this.responseShow("good");
        }
        this.dataSourceRuntime = new MatTableDataSource(value);
      }
      catch(e){
        this.responseShow("bad");
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
    this.dataSourceClusters.filter = filterValue.trim().toLowerCase();
  }

  /** Stream control window properties */
  streamStartStop = "Stop";
  streamPauseResume = "Pause";
  blockNumber="";

  serviceControlList = [];
  someData = [];

  sortData(){}

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

  startStopStream(streamState, streamName){
    try{
      let path = this.pathToData  + "/" + streamName;
      if(this.utilsSvc.compareStrings(streamState,"stop")){
        console.log("Stopping ...");
         this.controlUtils.stopStream(path);
      }
      else{
        console.log("Starting ...");
         this.controlUtils.startStream(path);
      }
    }catch(e){
      console.log("Problem occurred in start/stop");
      console.log(e);
    }
  }

  replayTheBlockFromInput(streamName, blockNumber){
    try{
       console.log("Trying to replay block: ", blockNumber, " from stream: ", streamName)
       let path = this.pathToData  + "/" + streamName;
       this.controlUtils.replayBlock(path, blockNumber);
    }catch(e){
     console.log("Problem occurred in start/stop");
     console.log(e);
    }
  }

  updateStream(streamName){
    try{
       console.log("Updating stream: ", streamName)
       let path = this.pathToData  + "/" + streamName;
       this.controlUtils.updateStream(path);
    }catch(e){
       console.log("Problem occurred in start/stop");
       console.log(e);
    }
  }

  rollbackStream(streamName){
    try{
       console.log("Updating stream: ", streamName)
       let path = this.pathToData  + "/" + streamName;
       this.controlUtils.updateStream(path);
    }catch(e){
       console.log("Problem occurred in start/stop");
       console.log(e);
    }
  }


}
