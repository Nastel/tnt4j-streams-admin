import {Compiler, Component, Injectable, Injector, NgModule, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';

import {Router, RouterModule} from '@angular/router';
import { Resolve, ActivatedRouteSnapshot,RouterStateSnapshot } from '@angular/router';

import {ConfigurationHandler} from './configuration-handler';
import {TreeViewComponent} from '../tree-view/tree-view.component';

import {LoginComponent} from '../login/login.component';
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

  iconsDataLoaded = false;

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
            this.createDynamic();
            if(!this.utilsSvc.compareStrings(this.zooKeeperTreeNodeList, "undefined")&&!this.utilsSvc.compareStrings(this.zooKeeperTreeNodeList, "null")){
              this.getConfigurationsFromTree(this.zooKeeperTreeNodeList);
              if(this.iconsDataLoaded){
                setTimeout(function(){ resolve("resolved"); }, 2000);
                console.log( "The data has been loaded resolve the response");
              }
            }
        });
      });
    });
//    var promise2 = new Promise ((resolve, reject) => {
//       this.getConfigurationsFromTree(this.zooKeeperTreeNodeList);
//       if(this.iconsDataLoaded ){
//          resolve(promise)
//       }
//    });
    promise.then(function(value) {
     // this.getConfigurationsFromTree( this.zooKeeperTreeNodeList );
      console.log(value);
    });

    return promise;

  }

  getConfigurationsFromTree(listOfNodes){
   console.log( "Adding svg icons");
    let streamsRegistryNode = this.configurationHandler.CONFIG["activeStreamRegistryNode"];
  //  let nodesToExcludeArray = this.getArrayOfNodesToExclude(elementsToExcludeFromTreeView);

    for(let node in this.zooKeeperTreeNodeList){
        let tempNodeArray = node.split("/");
        if(tempNodeArray.length < 8){
          this.getZooKeeperNodeData(node).subscribe( data => {
            try{
              this.zooKeeperData = data;
              let result =  JSON.parse(this.zooKeeperData.toString());
              this.nodeConf = result["config"];
   //             console.log(node, result["config"]);
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
//        if(tempNodeArray.length == 6){
//        node = node + "/" + streamsRegistryNode;
//          this.getZooKeeperNodeData(node).subscribe( data => {
//              this.zooKeeperData = data;
//              let result =  JSON.parse(this.zooKeeperData.toString());
//              for(let stream in result){
//                console.log(stream);
//              }
//
//          },
//          err =>{
//             console.log("Problem on reading node:", node, "data");
//             console.log(err);
//          });
//       }
    }
    this.iconsDataLoaded = true;
  }

  getZooKeeperNodeData(zooKeeperPath){
      try{
        zooKeeperPath = zooKeeperPath.substr(1);
        let pathLink = zooKeeperPath.replace(this.CONFIG["BasePathHide"],'');
        let serviceUrl : string
        let urlBuild = this.configurationHandler.CONFIG["ZooKeeperBasePath"];
           let urlChoice = this.configurationHandler.CONFIG["ZooKeeperDataCall"];
           if(!this.utilsSvc.compareStrings(urlBuild, "undefined")){
               serviceUrl = urlBuild + pathLink + urlChoice[0];
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

      for (let name in this.zooKeeperTreeNodeList) {
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
//        this.compiler.compileModuleAsync (tmpModule).then ((module) => {
//           const appRoutes = [...router.config];
//           let route;
//           route = {path: "**", component: LoginComponent, runGuardsAndResolvers: 'always'};
//           appRoutes.push (route);
//           router.resetConfig (appRoutes);
//         });
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


