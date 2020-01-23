import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked , ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { DomSanitizer } from "@angular/platform-browser";

import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';
import { incompleteBlocks }  from '../incomplete-blocks/incomplete-blocks.component';
import { TreeViewComponent } from '../tree-view/tree-view.component'

@Component({
  selector: 'app-server-configuration-file',
  templateUrl: './server-configuration-file.component.html',
  styleUrls: ['./server-configuration-file.component.scss']
})
export class ServerConfigurationFileComponent implements OnInit {


@ViewChild('viewComponent')private viewComponent: ElementRef;

  /** Url address */
  pathToData : string;

  /** Service configuration data */
  serviceConfigParent : string;
  objectKeys = Object.keys;
  configChoiceData= "";

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
                public incBlocks: incompleteBlocks,
                public treeView: TreeViewComponent,
                private changeDetectionRef : ChangeDetectorRef) { }

  ngOnInit() {
    this.dataHeight = parseInt(localStorage.getItem("dataComponentHeight"), 10);
    this.dataHeight = this.dataHeight - 150;
//    console.log("Logs component data height", this.dataHeight)
    this.pathToData = this.router.url.substring(1);
    this.loadZooKeeperNodeData(this.pathToData);
  }

  ngAfterViewChecked(){
  let height = 0;
    if(!this.utilsSvc.compareStrings(this.viewComponent,"undefined")){
      height = this.viewComponent.nativeElement.offsetHeight;
    }
    else{
      height = 700;
    }
      if(this.dataHeight<height){
        this.dataHeight = height;
      }
     this.changeDetectionRef.detectChanges();
  }
  reloadData(){
      this.treeView.loadZooKeeperNodeData(this.pathToData);
      this.ngOnInit();
  }
  loadZooKeeperNodeData(pathToData){
    try{
      this.nodeConf = this.treeView.nodeConf;
      this.zooKeeperData = JSON.parse(this.treeView.zooKeeperData["data"]);
      this.configChoiceData =  this.zooKeeperData[0]['name'];
      this.responseShow("good")
    }
    catch (err){
      this.responseShow("bad");
      console.log("Problem on default node while trying to prepare the showing of node data AGENT LOGS", err);
    }
   }

   configFileChoice(choice){
     this.configChoiceData = choice;
     this.scrollToTop();
   }

   @ViewChild('configuration')private configFiles: ElementRef;

   scrollToTop(): void {
      try {
          this.configFiles.nativeElement.scrollTop = 0;
      } catch(err) {
          console.log("Problem while scrolling to bottom of log" , err);
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


}
