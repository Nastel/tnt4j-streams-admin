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

import { Component, OnInit, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
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


@ViewChild('viewComponent')private viewComponent: ElementRef;

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
  iconsRegistered = [];


  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService,
                private controlUtils: ControlUtils) { }

  ngOnInit() {
    this.utilsSvc.navigateToCorrectPathAfterRefreshOrURlChange(this.router);
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
       // result =  JSON.parse(result.toString());
        this.zooKeeperData = result;
//        console.log( this.zooKeeperData )
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
      if(!this.utilsSvc.compareStrings(configuration['componentLoad'], 'undefined') && !this.utilsSvc.compareStrings(this.zooKeeperData['data'],"")){
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
//      console.log(this.zooKeeperData["dataReading"])
      if(this.utilsSvc.compareStrings(this.zooKeeperData["dataReading"], "undefined") || this.utilsSvc.compareStrings(this.zooKeeperData["dataReading"], "") ){}
      else{
//        console.log(this.zooKeeperData["Response link"])
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
