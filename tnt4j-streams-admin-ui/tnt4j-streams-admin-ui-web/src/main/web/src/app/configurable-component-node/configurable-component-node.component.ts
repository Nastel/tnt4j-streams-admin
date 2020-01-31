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
