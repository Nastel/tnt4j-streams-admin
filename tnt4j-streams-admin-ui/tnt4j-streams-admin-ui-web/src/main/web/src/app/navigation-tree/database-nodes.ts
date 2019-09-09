import {FlatTreeControl} from '@angular/cdk/tree';
import {Component, Injectable, OnInit} from '@angular/core';
import {MatTreeFlatDataSource, MatTreeFlattener} from '@angular/material/tree';
import {BehaviorSubject, Observable} from 'rxjs';
import {UtilsService } from "../utils/utils.service";
import {ConfigurationHandler } from '../config/configuration-handler';

/** Nested node */
export class FileNode {
  childrenChange = new BehaviorSubject<FileNode[]>([]);

  get children(): FileNode[] {
    return this.childrenChange.value;
  }

  constructor(public item: string,
              public hasChildren = false) {}
}
/**
 * A database that only load part of the data initially. And then loads more when the main component init method is called
 */
@Injectable()
export class nodeDatabase{
  batchNumber = 10;
  dataChange = new BehaviorSubject<FileNode[]>([]);
  nodeMap = new Map<string, FileNode>();

  constructor(
    private configurationHandler:ConfigurationHandler,
    private utilsSvc: UtilsService) {}

  getTreeList(tempZooKeeperNodeList): Map<string, string[]>{
    let elementsToExcludeFromTreeView = this.configurationHandler.CONFIG["excludeFromTreeView"];
    let dataMap = new Map<string, string[]>();
    for(let node in tempZooKeeperNodeList){
      let tempArray = [];
         let tempParent: string;
         for(let nodeInside in tempZooKeeperNodeList){
             if(this.utilsSvc.compareStrings(node, tempZooKeeperNodeList[nodeInside])){
                if(!elementsToExcludeFromTreeView.includes(this.utilsSvc.getNodePathEnd(nodeInside))){
                   tempArray.push(nodeInside)
                   tempParent = tempZooKeeperNodeList[nodeInside];
                }
             }
          }
          if (tempArray !== undefined && tempArray.length != 0) {
                      dataMap.set(tempParent, tempArray);
                   }
    }
      return dataMap;
  }

  /** The node data */
  rootLevelNodes = [];
  dataMap = new Map<string, string[]>();

  initialize(theNodeTreeForDraw) {
    try{
      let tempZooKeeperNodeList = this.configurationHandler.zooKeeperTreeNodeList;
      let value = this.configurationHandler.CONFIG["StartTreeNodeParentName"];
      this.rootLevelNodes.push(Object.keys(tempZooKeeperNodeList).find(key => tempZooKeeperNodeList[key] === value));
      this.dataMap = this.getTreeList(tempZooKeeperNodeList);
      const data = this.rootLevelNodes.map(name => this._generateNode(name));
      this.dataChange.next(data);
    }catch(err){
      console.log("Problem while trying to format the material tree data from ZooKeeper nodes ", err);
    }
  }

//  public filter(filterText: string) {
//      let filteredTreeData;
//      if (filterText) {
//        console.log(this.treeData);
//        filteredTreeData = this.treeData.filter(d => d.text.toLocaleLowerCase().indexOf(filterText.toLocaleLowerCase()) > -1);
//
//        console.log(filteredTreeData);
//        Object.assign([], filteredTreeData).forEach(ftd => {
//          let str = (<string>ftd.code);
//          while (str.lastIndexOf('.') > -1) {
//            const index = str.lastIndexOf('.');
//            str = str.substring(0, index);
//            if (filteredTreeData.findIndex(t => t.code === str) === -1) {
//              const obj = this.treeData.find(d => d.code === str);
//              if (obj) {
//                filteredTreeData.push(obj);
//              }
//            }
//          }
//        });
//      } else {
//        filteredTreeData = this.treeData;
//      }
//  }

  /** Load a node whose children are not loaded */
  loadMore(item: string, onlyFirstTime = false) {
    if (!this.nodeMap.has(item) || !this.dataMap.has(item)) {
      return;
    }
    const parent = this.nodeMap.get(item)!;
    const children = this.dataMap.get(item)!;
    if (onlyFirstTime && parent.children!.length > 0) {
      return;
    }
    const newChildrenNumber = parent.children!.length + this.batchNumber;
    const nodes = children.slice(0, newChildrenNumber).map(name => this._generateNode(name));
   // nodes.item.sort();
 // console.log(nodes);
    parent.childrenChange.next(nodes);
    this.dataChange.next(this.dataChange.value);
  }

  private _generateNode(item: string): FileNode {
    if (this.nodeMap.has(item)) {
      return this.nodeMap.get(item)!;
    }
   // console.log(item,  this.dataMap.has(item))
    const result = new FileNode(item, this.dataMap.has(item));
    this.nodeMap.set(item, result);
    //console.log(this.nodeMap)
    return result;
  }
}
