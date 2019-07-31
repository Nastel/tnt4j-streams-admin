import {FlatTreeControl} from '@angular/cdk/tree';
import {Component, Injectable, OnInit} from '@angular/core';
import {MatTreeFlatDataSource, MatTreeFlattener} from '@angular/material/tree';
import {BehaviorSubject, Observable} from 'rxjs';
import { nodeDatabase } from "./database-nodes";
import { Router } from '@angular/router';
import { MatIconRegistry } from "@angular/material";
import { DomSanitizer } from "@angular/platform-browser";
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

/** Flat node with expandable and level information */
export class FileFlatNode {
  constructor(public item: string,
              public level = 1,
              public expandable = true,
              public visible = "",
              public parent: string) {}
}

/**
 * @title Tree with partially loaded data
 */
@Component({
  selector: 'app-navigation-tree',
  templateUrl: './navigation-tree.component.html',
  styleUrls: ['./navigation-tree.component.scss'],
  providers: [nodeDatabase]
})
export class NavigationTreeComponent implements OnInit  {
  /** Url address */
  pathToData : string;

  /** tree control */
  nodeMap = new Map<string, FileFlatNode>();
  AllNodes = new Map<string, FileFlatNode>();
  treeControl: FlatTreeControl<FileFlatNode>;
  treeFlattener: MatTreeFlattener<FileNode, FileFlatNode>;

  // Flat tree data source
  dataSource: MatTreeFlatDataSource<FileNode, FileFlatNode>;

  /** maybe for node expanding */
  expandedNodeSet = new Set<string>();

  expandablePath = [];

  /** all registered icons array */
  iconsRegistered = [];

  constructor(
    private database: nodeDatabase,
    private configurationHandler:ConfigurationHandler,
    private router: Router,
    private utilsSvc: UtilsService,
    private matIconRegistry: MatIconRegistry,
    private domSanitizer: DomSanitizer) {
    this.treeFlattener = new MatTreeFlattener(this.transformer, this.getLevel,this.isExpandable, this.getChildren);
    this.treeControl = new FlatTreeControl<FileFlatNode>(this.getLevel, this.isExpandable);

    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
  //  console.log( this.dataSource)
  }

  getChildren = (node: FileNode): Observable<FileNode[]> => node.childrenChange;
  transformer = (node: FileNode, level: number) => {
    const existingNode = this.nodeMap.get(node.item);
    if (existingNode) {
      return existingNode;
    }
    let regXp= "(.*/).+";
    if(!this.utilsSvc.compareStrings(node.item,'undefined')){
      let parent = node.item.match(regXp);
    }
      const newNode = new FileFlatNode(node.item, level, node.hasChildren, "", parent[1] );
      this.nodeMap.set(node.item, newNode);
    return newNode;
  }
  getLevel = (node: FileFlatNode) => node.level;
  isExpandable = (node: FileFlatNode) => node.expandable;
  hasChild = (_: number, _nodeData: FileFlatNode) => _nodeData.expandable;

  isVisible = (_: number, _nodeData: FileFlatNode) => _nodeData.visible;

  loadChildren(node: FileFlatNode) {
    this.database.loadMore(node.item, true);
  }

  private loadAllTreeData(treeControl: FlatTreeControl<FileFlatNode>){
     treeControl.dataNodes.forEach((node) => {
            this.loadChildren(node);
        });
  }

   ngOnInit(){
     try{
        this.pathToData = this.router.url.substring(1);
        let tempZooKeeperNodeList =   this.configurationHandler.getZooKeeperNodeList();
        this.database.initialize(tempZooKeeperNodeList);
        this.database.dataChange.subscribe(data => {
          this.dataSource.data = data;
          this.loadAllTreeData(this.treeControl);
          this.insertTreeNode(this.treeControl.dataNodes);
          this.expandablePath.push("/"+this.pathToData);
          let item = JSON.parse(localStorage.getItem("openTreeNodes"));
//          console.log("EXPANDED NODE SET", this.treeControl)
          this.expandNodesById( this.treeControl.dataNodes,this.expandablePath);
          if(!this.utilsSvc.compareStrings(item, 'undefined') && !this.utilsSvc.compareStrings(item, 'null')){
            this.expandNodesById( this.treeControl.dataNodes,item);
          }
        });
        this.getAllRegisteredIonsList();
     }catch(err){
        console.log("Problem on loading the tree nodes data for the tree view ", err);
     }
    }

  insertTreeNode(tempNode){
    let tempDataForNodes = tempNode;
    let firstNode = [];
    let secondNode = [];
    for(let dataFirst in tempDataForNodes){
      let nodeCount = 0;
      let nodeInsideList = [];
      firstNode = tempNode[dataFirst];
      for(let dataSecond in tempDataForNodes){
       secondNode = tempNode[dataSecond];
         if(firstNode['expandable']){
            if(secondNode['item'].includes(firstNode['item']+'/')&&(firstNode['level']==(secondNode['level']-1))){
              nodeCount++;
              let nodePosition = nodeCount + parseInt(dataFirst);
              nodeInsideList[nodePosition] = secondNode;
              tempNode[dataFirst] = firstNode;
              tempNode.splice(dataSecond, 1);
              tempNode.splice(nodePosition, 0, secondNode);
              this.sort(nodeInsideList);
            }
         }
       }
       let countNodesInside = nodeInsideList.length;
       if(countNodesInside>0){
         for(let expendables in nodeInsideList){
            let nodeIdToChange = countNodesInside - 1;
            countNodesInside--;
            let data = nodeInsideList[expendables];
            tempNode.splice(nodeIdToChange, 1);
            tempNode.splice(nodeIdToChange, 0, data);
         }
       }
    }
  }

  sort(tempNode){
    tempNode.sort(function (name1, name2) {
      if (name1.expandable<name2.expandable) {
        return -1;
      } else if (name2.expandable<name1.expandable) {
        return 1;
      } else {
        return 0;
      }
    });
  }

  searchNodeTree = (value: string) => {
      if (value && value.length >= 3) {
        this.filterByName(value);
      } else {
        this.clearFilter();
      }
  }

  collapseNodeTree(){
   this.treeControl.collapseAll();
  }
  expandNodeTree(){
   this.treeControl.expandAll();
  }

  filterByName(filterText){
    try{
      let filteredItems = this.treeControl.dataNodes.filter( x => x.item.toLowerCase().indexOf(filterText.toLowerCase()) === -1);
      let visibleItems = this.treeControl.dataNodes.filter( x => x.item.toLowerCase().indexOf(filterText.toLowerCase()) > -1);
      this.treeControl.collapseAll();
        //console.log(filteredItems);
        filteredItems.forEach((node) => {
          node.visible = "false";
        });
        visibleItems.forEach((node) => {
            node.visible = "true";
            this.treeControl.expand(node);
            let parent = this.getParentNode(node);
            while (parent) {
              this.treeControl.expand(parent);
              parent = this.getParentNode(parent);
            }
          });
          }
    catch(err){
      console.log("Problem occurred while trying to search the node tree data ", err);
    }
  }

  clearFilter(){
      this.treeControl.collapseAll();
      this.expandNodesById( this.treeControl.dataNodes,this.expandablePath);
      this.treeControl.dataNodes.forEach(node => node.visible = "");
  }

  getAllRegisteredIonsList(){
  try{
      let iconMap = this.matIconRegistry['_svgIconConfigs'];
      let keys = iconMap.keys();
      for( let index =0; index < iconMap.size; index++){
         let value = keys.next().value.substring(1);
         this.iconsRegistered.push(value);
      }
    }
    catch(err){
      console.log("Problem on getting the registered icon list", err);
    }
  }

  private onExpandCall(){
    //this.rebuildTreeForData(  this.dataSource.data);
    this.rememberExpandedTreeNodes(this.treeControl, this.expandedNodeSet);
    this.forgetMissingExpandedNodes(this.treeControl, this.expandedNodeSet);
    localStorage.setItem("openTreeNodes", JSON.stringify(Array.from(this.expandedNodeSet)));
  }

  private rememberExpandedTreeNodes( treeControl: FlatTreeControl<FileFlatNode>,expandedNodeSet: Set<string>) {
    if (treeControl.dataNodes) {
      treeControl.dataNodes.forEach((node) => {
//        console.log("is expandable", treeControl.isExpandable(node), "is expanded", treeControl.isExpanded(node));
        if (treeControl.isExpandable(node) && treeControl.isExpanded(node)) {
           this.expandedNodeSet.add(node.item);
//           console.log("Node information", node)
        }
//        else if(treeControl.isExpandable(node) && !treeControl.isExpanded(node)) {
//          if (treeControl.dataNodes.find((n) => n.item === node.item)) {
//             console.log("Node To DELETE from SET", node)
//             expandedNodeSet.delete(node.item);
//          }
//        }

      });
    }
  }

  private forgetMissingExpandedNodes(treeControl: FlatTreeControl<FileFlatNode>, expandedNodeSet: Set<string>) {
    if (treeControl.dataNodes) {
      treeControl.dataNodes.forEach((node) => {
          if(treeControl.isExpandable(node) && !treeControl.isExpanded(node)) {
            this.expandedNodeSet.delete(node.item);
          }
      });
    }
  }

//  deleteNode(node:string) {
//      const index: number = this.expandedNodeSet.indexOf(node);
//      if (index !== -1) {
//          this.expandedNodeSet.splice(index, 1);
//      }
//  }




/*
  rebuildTreeForData(data: any) {
    this.rememberExpandedTreeNodes(this.treeControl, this.expandedNodeSet);
    this.dataSource.data = data;
    this.forgetMissingExpandedNodes(this.treeControl, this.expandedNodeSet);
    this.expandNodesById(this.treeControl.dataNodes, Array.from(this.expandedNodeSet));
  }




*/

  private expandNodesById(flatNodes: FileFlatNode[], ids: string[]) {
    try{
      if (!flatNodes || flatNodes.length === 0) return;
      const idSet = ids;
      return flatNodes.forEach((node) => {
        if (idSet.includes(node.item)) {
          this.treeControl.expand(node);
          let parent = this.getParentNode(node);
          while (parent) {
            this.treeControl.expand(parent);
            parent = this.getParentNode(parent);
          }
        }
      });
    }catch(err){
      console.log("Problem on trying to expand node ");
      console.log(err);
    }
  }

  private getParentNode(node: FileFlatNode): FileFlatNode | null {
    const currentLevel = node.level;
    if (currentLevel < 1) {
      return null;
    }
    const startIndex = this.treeControl.dataNodes.indexOf(node) - 1;
    for (let i = startIndex; i >= 0; i--) {
      const currentNode = this.treeControl.dataNodes[i];
      if (currentNode.level < currentLevel) {
        return currentNode;
      }
    }
    return null;
  }
}
