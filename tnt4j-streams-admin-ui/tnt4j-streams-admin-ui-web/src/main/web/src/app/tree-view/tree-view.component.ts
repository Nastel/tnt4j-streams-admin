import { Component, OnInit, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatIconRegistry } from "@angular/material";
import { DomSanitizer } from "@angular/platform-browser";
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';
import { Injectable } from '@angular/core';
import { ControlUtils } from "../utils/control.utils";

@Component({
  selector: 'app-tree-view',
  templateUrl: './tree-view.component.html',
  styleUrls: ['./tree-view.component.scss']
})
@Injectable()
export class TreeViewComponent implements OnInit {


@ViewChild('viewComponent', { static: true })private viewComponent: ElementRef;

  /** Url address */
  pathToData : string;

  /** Node choice  */
  public nodeConf : string;

  /** Data object for loading data from ZooKeeper*/
  public zooKeeperData : Object;

  /** Values for showing data loading properties */
  streamDataShowChoice: string;
  valueThatChangesOnDataLoad = false;
  valueThatChangesForSpinnerOnResponse = false;

  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService,
                private matIconRegistry: MatIconRegistry,
                private domSanitizer: DomSanitizer,
                private controlUtils: ControlUtils) { }

  ngOnInit() {
    this.pathToData = this.router.url.substring(1);
    this.loadZooKeeperNodeData(this.pathToData);

  }

  ngAfterViewInit(){
   let height = this.viewComponent.nativeElement.offsetHeight;
   localStorage.setItem("dataComponentHeight", height);
  }

  public loadZooKeeperNodeData(pathToData){
    this.streamDataShowChoice = 'undefined';
    this.responseShow("");
    this.data.getZooKeeperNodeData(pathToData).subscribe( data => {
        let result = data;
        result =  JSON.parse(result.toString());
        this.zooKeeperData = result;
        console.log( this.zooKeeperData )
        this.nodeConf = result["config"];
        this.choiceMaker( this.nodeConf);
        this.responseShow("good");
        },
    err =>{
       this.responseShow("bad");
       console.log("Problem on reading node:", pathToData, "data");
       console.log(err);
    });
  }

  choiceMaker(configuration){
    try{
      if(!this.utilsSvc.compareStrings(configuration['componentLoad'], 'undefined')){
        this.streamDataShowChoice = configuration['componentLoad'];
      }
      else{
        this.controlUtils.openDialog("Check if the path matches" + configuration['componentLoad'], this.pathToData);
        // console.log("Check if the path matches", configuration['nodeName'],  configuration['componentLoad']);
        this.streamDataShowChoice = 'default';
      }
    }
    catch(err) {
       this.streamDataShowChoice = 'default';
       if(!this.utilsSvc.compareStrings(this.zooKeeperData["dataReading"], "undefined")){
       console.log(this.zooKeeperData["Response link"])
        this.controlUtils.openDialogWithHeader(this.zooKeeperData["Response link"], this.zooKeeperData["dataReading"], this.pathToData);
       }
       console.log("Node data not loaded correctly:", err);
       console.log("Most likely reasons:");
       console.log("* Bad ZooKeeper configuration - missing values");
       console.log("* Bad response from the URL Address");
       console.log("* ZooKeeper returned not a JSON format");
    }
  }

  /*Response variables set to good, bad or else for showing the data loading state*/
  public responseShow(responseData){
    if(this.utilsSvc.compareStrings(responseData, "good")){
      this.valueThatChangesForSpinnerOnResponse = false;
      this.valueThatChangesOnDataLoad = true;
      return true;
    }
    else if(this.utilsSvc.compareStrings(responseData, "bad")){
      this.valueThatChangesForSpinnerOnResponse = false;
      this.valueThatChangesOnDataLoad = false;
      return false;
    }
    else{
      this.valueThatChangesOnDataLoad = false;
      this.valueThatChangesForSpinnerOnResponse = true;
      return false;
    }
  }

}
