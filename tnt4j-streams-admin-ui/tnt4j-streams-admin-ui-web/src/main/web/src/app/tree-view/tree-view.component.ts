import { Component, OnInit, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatIconRegistry } from "@angular/material";
import { DomSanitizer } from "@angular/platform-browser";
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';
import { incompleteBlocks }  from '../incomplete-blocks/incomplete-blocks.component';

@Component({
  selector: 'app-tree-view',
  templateUrl: './tree-view.component.html',
  styleUrls: ['./tree-view.component.scss']
})
export class TreeViewComponent implements OnInit {


@ViewChild('viewComponent')private viewComponent: ElementRef;

  /** Url address */
  pathToData : string;

  /** Node choice  */
  nodeConf : string;

  /** Data object for loading data from ZooKeeper*/
  zooKeeperData : Object;

  /** Values for showing data loading properties */
  streamDataShowChoice: string;

  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService,
                private matIconRegistry: MatIconRegistry,
                private domSanitizer: DomSanitizer,
                public incBlocks: incompleteBlocks) { }

  ngOnInit() {
    this.pathToData = this.router.url.substring(1);
    this.loadZooKeeperNodeData(this.pathToData);

  }

  ngAfterViewInit(){
   let height = this.viewComponent.nativeElement.offsetHeight;
   localStorage.setItem("dataComponentHeight", height);
  }

  loadZooKeeperNodeData(pathToData){
    this.data.getZooKeeperNodeData(pathToData).subscribe( data => {
        this.zooKeeperData = data;
        let result =  JSON.parse(this.zooKeeperData.toString());
        this.nodeConf = result["config"];
        this.choiceMaker( this.nodeConf);
        },
    err =>{
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
        console.log("Check if the path matches", configuration['nodeName'],  configuration['componentLoad']);
        this.streamDataShowChoice = 'thread';
      }
    }
    catch(err) {
      this.streamDataShowChoice = 'thread';
       console.log("Node data not loaded correctly:", err);
       console.log("Most likely reasons:");
       console.log("* Bad ZooKeeper configuration - missing values");
       console.log("* Bad response from the URL Address");
       console.log("* ZooKeeper returned not a JSON format");
    }
  }

}
