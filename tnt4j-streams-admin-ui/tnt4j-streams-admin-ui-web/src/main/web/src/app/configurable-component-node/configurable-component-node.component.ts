import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';

import { UtilsService } from '../utils/utils.service';
import { DataService } from '../data.service';

import { TreeViewComponent } from '../tree-view/tree-view.component'

@Component({
  selector: 'app-configurable-component-node',
  templateUrl: './configurable-component-node.component.html',
  styleUrls: ['./configurable-component-node.component.sass']
})
export class ConfigurableComponentNodeComponent implements OnInit {

  /** Url address */
  pathToData : string;

  /** Data object for loading data from ZooKeeper*/
  nodeConf : string;
  zooKeeperData : Object;

  /** Data appearance variables */
  dataHeight = 0;

  constructor( private data: DataService,
               private router: Router,
               public utilsSvc: UtilsService,
               public treeView: TreeViewComponent,
               private changeDetectionRef : ChangeDetectorRef) { }


  ngOnInit() {

    this.dataHeight = parseInt(localStorage.getItem("dataComponentHeight"), 10);
    this.dataHeight = this.dataHeight - 50;
    this.pathToData = this.router.url.substring(1);
    this.loadZooKeeperNodeData(this.pathToData);

  }
  ngAfterViewChecked(){
     this.changeDetectionRef.detectChanges();
  }
  reloadData(){
      this.treeView.loadZooKeeperNodeData(this.pathToData);
      this.ngOnInit();
  }

  loadZooKeeperNodeData(pathToData){
    try{
      this.nodeConf = this.treeView.nodeConf;
      this.zooKeeperData = this.treeView.zooKeeperData;
      this.zooKeeperData = JSON.stringify(this.zooKeeperData, null, ' ');
    }
    catch (err){
      console.log("Problem on default node while trying to prepare the showing of node data", err);
    }
  }
}
