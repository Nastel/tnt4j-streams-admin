import {Compiler, Component, Injectable, Injector, NgModule, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';

import {Router, RouterModule} from '@angular/router';
import { Resolve, ActivatedRouteSnapshot,RouterStateSnapshot } from '@angular/router';

import {ConfigurationHandler} from './configuration-handler';
import {TreeViewComponent} from '../tree-view/tree-view.component';
import {UtilsService} from "../utils/utils.service";

import { MatIconRegistry } from "@angular/material";
import { DomSanitizer } from "@angular/platform-browser";

@Injectable ()
export class AutoRouteGenerator
{

  pathToIcon: string;
  public CONFIG = {};
  zooKeeperStreamConfig = {};

   // --- Data for tree view  ---
  public nodeConf = {};
  zooKeeperTreeNodeList = {};
  zooKeeperData = {};
  nodesToExcludeArray = [];

  jsonFile = "assets/configuration.json";

  constructor (
    private http: HttpClient,
    private injector: Injector,
    public utilsSvc: UtilsService,
    private compiler: Compiler,
    private configurationHandler: ConfigurationHandler,
    private matIconRegistry: MatIconRegistry,
    private domSanitizer: DomSanitizer){}

  public getLinks (): Promise<{}>
  {
    var promise = new Promise ((resolve, reject) => {
      this.http.get<any> (this.jsonFile).subscribe (data => {
        this.CONFIG = data;
          this.http.get(this.CONFIG["ZooKeeperTreeNodes"]).subscribe(data => {
            this.zooKeeperTreeNodeList = data;
            this.getConfigurationsFromTree( this.zooKeeperTreeNodeList )
            this.createDynamic();
            if(!this.utilsSvc.compareStrings( this.zooKeeperTreeNodeList, "undefined")){
              resolve("done")
            }
        });
      });
    });
//    promise.then(() =>  this.getConfigurationsFromTree( this.zooKeeperTreeNodeList ));
    return promise;

  }

  getConfigurationsFromTree(listOfNodes){
    let elementsToExcludeFromTreeView = this.configurationHandler.CONFIG["excludeFromReadingOnTreeLoad"];
    let nodesToExcludeArray = this.getArrayOfNodesToExclude(elementsToExcludeFromTreeView);
    for(let node in this.zooKeeperTreeNodeList){
        if(!nodesToExcludeArray.includes(node)){
          this.getZooKeeperNodeData(node).subscribe( data => {
            try{
              this.zooKeeperData = data;
              let result =  JSON.parse(this.zooKeeperData.toString());
              this.nodeConf = result["config"];
              if(!this.utilsSvc.compareStrings(this.nodeConf, "undefined")){
                if(!this.utilsSvc.compareStrings(this.nodeConf["streamsIcon"], "undefined")){
                  this.getAllImageSvgZooKeeper(node, this.nodeConf["streamsIcon"]);
                 }
              }
            }
            catch(err){
              console.log("Configurations from tree nodes where not read correctly" , err);
            }
         },
          err =>{
             console.log("Problem on reading node:", node, "data");
             console.log(err);
          });
       }
    }
  }

  getZooKeeperNodeData(zooKeeperPath){
      try{
        zooKeeperPath = zooKeeperPath.substr(1);
        let pathLink = zooKeeperPath.replace(this.CONFIG["BasePathHide"],'');
        let serviceUrl : string
        let urlBuild = this.CONFIG["ZooKeeperDataCall"];
          if(!(urlBuild === "undefined")){
              serviceUrl = urlBuild[0] + pathLink + urlBuild[1];
          }
        return this.http.get(serviceUrl, {responseType: 'text'} );
      }
      catch(err){
        console.log("Problem while trying to rad data from ZooKeeper nodes", err);
      }
    }

  getAllImageSvgZooKeeper(pathToIcon, svgData){
    try{
       if(!this.utilsSvc.compareStrings(pathToIcon, "undefined") || !this.utilsSvc.compareStrings(svgData, "undefined")){
          this.matIconRegistry.addSvgIconLiteral(pathToIcon, this.domSanitizer.bypassSecurityTrustHtml(svgData));
       }
    }
    catch(err){
     // console.log("Problem in configurations on registering .svg icons", err);
    }
  }

  getArrayOfNodesToExclude(elementsToExcludeFromTreeView){
   let nodesToExcludeArray = [];
    try{
      if(!this.utilsSvc.compareStrings(elementsToExcludeFromTreeView, 'undefined')){
        for (let name in this.zooKeeperTreeNodeList)
        {
          let arrayName = name.split('/');
          for (let excludeKey of elementsToExcludeFromTreeView){
            if(arrayName.includes(excludeKey)){
              nodesToExcludeArray.push(name);
            }
          }
        }
      }
    }
    catch(err){
     console.log("Problem finding node names to exclude", err);
    }
     return nodesToExcludeArray;
  }



  createDynamic ()
  {
    try{
      const router = this.injector.get (Router);
      const template = '';
      const tmpCmp = Component ({template: template}) (class {});
      const tmpModule = NgModule ({declarations: [tmpCmp]}) (class {});
      let elementsToExcludeFromTreeView = this.configurationHandler.CONFIG["excludeFromTreeView"];
      let nodesToExcludeArray = this.getArrayOfNodesToExclude(elementsToExcludeFromTreeView);
      for (let name in this.zooKeeperTreeNodeList)
      {
        if(!nodesToExcludeArray.includes(name)){ //!elementsToExcludeFromTreeView.includes(this.utilsSvc.getNodePathEnd(name))){
        name = name.substring(1);
        this.compiler.compileModuleAsync (tmpModule).then ((module) => {
          const appRoutes = [...router.config];
          let route;
          route = {path: name, component: TreeViewComponent, runGuardsAndResolvers: 'always'};
          appRoutes.push (route);
          router.resetConfig (appRoutes);
        });
        }
      }
      this.compiler.compileModuleAsync (tmpModule).then ((module) => {
          const appRoutes = [...router.config];
          let route;
          route = {path: "**", redirectTo: '/streams/v1/clusters', runGuardsAndResolvers: 'always'};
          appRoutes.push (route);
          router.resetConfig (appRoutes);
        });
    }
    catch(err){
      console.log("Problem while creating dynamic routes ", err);
    }
  }
}


