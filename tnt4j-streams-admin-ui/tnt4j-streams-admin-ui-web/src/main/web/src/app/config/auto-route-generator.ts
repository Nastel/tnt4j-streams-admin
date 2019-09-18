import {Compiler, Component, Injectable, Injector, NgModule, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import { HttpHeaders } from '@angular/common/http';
import {Router, RouterModule} from '@angular/router';
import { Resolve, ActivatedRouteSnapshot,RouterStateSnapshot } from '@angular/router';

import {ConfigurationHandler} from './configuration-handler';
import {TreeViewComponent} from '../tree-view/tree-view.component';

import {LoginComponent} from '../login/login.component';
import {UserControlComponent} from '../users-control/users-control.component';
import {UtilsService} from "../utils/utils.service";


import { MatIconRegistry } from "@angular/material";
import { DomSanitizer } from "@angular/platform-browser";

@Injectable ()
export class AutoRouteGenerator
{

  pathToIcon: string;
  public CONFIG = {};
  zooKeeperStreamConfig = {};
  public iconMap = [];

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

  public async getLinks (): Promise<{}>{
  var promise : Promise<{}>
    let authToken = sessionStorage.getItem("authToken");
//    console.log("First try to check token");
    let data = await this.getConfigurations();
    if(authToken){
//      console.log("Call method to get configurations");
//      let data = await this.getTreeAndIconData();
      var promise = new Promise ((resolve, reject) => {
//            setTimeout(function(){ resolve("resolved"); }, 20);
//          });
        this.http.get<any> (this.jsonFile).subscribe (data => {
          this.CONFIG = data;
          var headers = this.returnHeaderWithToken();
            this.http.get(this.CONFIG["BaseAddress"]+this.CONFIG["ZooKeeperTreeNodes"], headers).subscribe(data => {
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
      }
      else{
         this.createDynamic();
      }
    return promise;

  }

  getZooKeeperNodeList(){
      try{
        var headers = this.returnHeaderWithToken();
        this.http.get(this.CONFIG["BaseAddress"]+this.CONFIG["ZooKeeperTreeNodes"], headers).subscribe(data => {
         this.zooKeeperTreeNodeList = data;
         let clusters = "";
         for(let node in this.zooKeeperTreeNodeList){

           if(this.utilsSvc.compareStrings(this.utilsSvc.getNodePathEnd(this.zooKeeperTreeNodeList[node]),"clusters")){
              if(!this.utilsSvc.compareStrings(clusters, "undefined")){
                 clusters = node + ";" + clusters;
              }else{
                clusters = node;
              }
           }
         }
         sessionStorage.setItem("clustersList", clusters);
        });
      }
      catch(err){
        console.log("ZooKeeper tree was not read correctly ", err);
      }
        return  this.zooKeeperTreeNodeList;
  }

  async getConfigurations(){
//    console.log("Read data from JSON config");
    this.CONFIG =  await  this.http.get<any> (this.jsonFile).toPromise();
    this.getAllImageSvg();
  }

  async getTreeAndIconData() {
//     console.log(this.CONFIG);
      var headers = this.returnHeaderWithToken();
//      console.log("REad data from ZooKeeper tree");
      this.zooKeeperTreeNodeList = await  this.http.get(this.CONFIG["BaseAddress"]+ this.CONFIG["ZooKeeperTreeNodes"], headers).toPromise();
      for(let node in this.zooKeeperTreeNodeList){
        if(this.utilsSvc.compareStrings(this.utilsSvc.getNodePathEnd(node),"clusters")){
            console.log(node);
            console.log(this.zooKeeperTreeNodeList[node]);
          }
      }
  //     console.log(this.zooKeeperTreeNodeList);
      this.createDynamic();
       if(!this.utilsSvc.compareStrings(this.zooKeeperTreeNodeList, "undefined")&&!this.utilsSvc.compareStrings(this.zooKeeperTreeNodeList, "null")){
  //        console.log("Wait for data from ZooKeeper tree about svg to be set");
          await this.getConfigurationsFromTree(this.zooKeeperTreeNodeList);
        }
  }

  /**
    A utils method to help with request authorization.
  */
  returnHeaderWithToken(){
    let tokenName = this.CONFIG["sessionTokenName"];
    let input = sessionStorage.getItem(tokenName);
    var header = { headers: new HttpHeaders().set('Authorization',  JSON.stringify(input))}
    return header;
  }

  async getConfigurationsFromTree(listOfNodes){
    let neededData = this.CONFIG["StreamsIcon"];
    let array = Object.keys(neededData);
    for(let node in this.zooKeeperTreeNodeList){
        let tempNodeArray = node.split("/");
        if(array.indexOf(this.utilsSvc.getNodePathEnd(node)) <= -1){
          if(tempNodeArray.length < 8){
            this.getZooKeeperNodeData(node).subscribe( data => {
              try{
                this.zooKeeperData = data;
                let result =  this.zooKeeperData;
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
    this.iconsDataLoaded = true;
  }

    /**
      Get the node data from path created
    */
    getZooKeeperNodeDataLog(zooKeeperPath, logCount){
      var header = this.returnHeaderWithToken();

      let serviceUrl : string;
      if(!this.utilsSvc.compareStrings(zooKeeperPath, 'undefined')){
        let pathLink = zooKeeperPath.replace(this.configurationHandler.CONFIG["BasePathHide"],'');
        let urlBuild =this.configurationHandler.CONFIG["BaseAddress"]+ this.configurationHandler.CONFIG["ZooKeeperBasePath"];
        let urlChoice = this.configurationHandler.CONFIG["ZooKeeperDataCall"];
        if(!this.utilsSvc.compareStrings(urlBuild, "undefined")){
            serviceUrl = urlBuild + pathLink + urlChoice[0];
        }
      }
      else{
        console.log("No path call defined");
      }
        if(logCount != 0){
          serviceUrl = serviceUrl  + "/?logCount="+logCount;
        }
      //console.log("link to back end", serviceUrl);
      return this.http.get(serviceUrl, header);
    }

  getZooKeeperNodeData(zooKeeperPath){
      try{
      var header = this.returnHeaderWithToken();
        zooKeeperPath = zooKeeperPath.substr(1);
        let pathLink = zooKeeperPath.replace(this.CONFIG["BasePathHide"],'');
        let serviceUrl : string
        let urlBuild = this.configurationHandler.CONFIG["BaseAddress"]+ this.configurationHandler.CONFIG["ZooKeeperBasePath"];
           let urlChoice = this.configurationHandler.CONFIG["ZooKeeperDataCall"];
           if(!this.utilsSvc.compareStrings(urlBuild, "undefined")){
               serviceUrl = urlBuild + pathLink + urlChoice[0];
           }
        return this.http.get(serviceUrl, header);
      }
      catch(err){
        console.log("Problem while trying to rad data from ZooKeeper nodes", err);
      }
    }

  getAllImageSvgZooKeeper(pathToIcon, svgData){
    try{
       if(!this.utilsSvc.compareStrings(pathToIcon, "undefined") || !this.utilsSvc.compareStrings(svgData, "undefined")){
          this.matIconRegistry.addSvgIconLiteral(pathToIcon, this.domSanitizer.bypassSecurityTrustHtml(svgData));
          this.iconMap.push(pathToIcon);
       }
    }
    catch(err){
      console.log("Problem in configurations on registering .svg icons", err);
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
      let userPath = this.configurationHandler.CONFIG["BasePathToUsersPage"];
      this.compiler.compileModuleAsync (tmpModule).then ((module) => {
        const appRoutes = [...router.config];
        let route;
        route = {path: "login", component: LoginComponent, runGuardsAndResolvers: 'always'};
        appRoutes.push (route);
        route = {path: userPath, component: UserControlComponent, runGuardsAndResolvers: 'always'};
        appRoutes.push (route);
        router.resetConfig (appRoutes);
        this.utilsSvc.navigateToCorrectPathAfterRefreshOrURlChange(router);
      });
      for (let name in this.zooKeeperTreeNodeList) {
          name = name.substring(1);
          this.compiler.compileModuleAsync (tmpModule).then ((module) => {
            const appRoutes = [...router.config];
            let route;
            route = {path: name, component: TreeViewComponent, runGuardsAndResolvers: 'always'};
            appRoutes.push (route);
            router.resetConfig (appRoutes);
          });
      }

      this.compiler.compileModuleAsync (tmpModule).then ((module) => {
        const appRoutes = [...router.config];
        let route;
        route = {path: "**", redirectTo: this.CONFIG["BasePathHide"]+'/clusters', runGuardsAndResolvers: 'always'};
        appRoutes.push (route);
        router.resetConfig (appRoutes);
      });
    }
    catch(err){
      console.log("Problem while creating dynamic routes ", err);
    }
  }

   /** Methods for config data from JSON config file assets/configuration.json */

  getAllImageSvg(){
    try{
      let neededData = this.CONFIG["StreamsIcon"];
      for(let name in neededData) {
        let pathToIcon =  this.CONFIG["StreamsIcon"][name];
        this.matIconRegistry.addSvgIcon(name, this.domSanitizer.bypassSecurityTrustResourceUrl(pathToIcon));
          this.iconMap.push(name);
      }
    }
    catch(err){
     console.log("Problem in configurations on registering .svg icons", err);
    }
  }
}


