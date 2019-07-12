import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { MatIconRegistry } from "@angular/material";
import { DomSanitizer } from "@angular/platform-browser";

  @Injectable()
  export class ConfigurationHandler {

// --- Data from prev version ---
  public CONFIG = {};
  public oneServiceName = "eth";
  public serviceNames = [];
  public serviceNameDiffRepoOrStream = [];
  threadsServiceDataName = "";
  configDataNameInAllData = "";
  zooKeeperStreamConfig = {};

 // --- Data for tree view  ---
  zooKeeperTreeNodeList = {};
  zooKeeperData = {};

  constructor(
      private http: HttpClient,
      private matIconRegistry: MatIconRegistry,
      private domSanitizer: DomSanitizer
      ) {}

  public getConfig (): Promise<string> {
  return new Promise((resolve, reject) => {
    const jsonFile = "assets/configuration.json";
    this.http.get<any>(jsonFile).subscribe(data => {
       try{
          this.CONFIG = data;
          console.log("CONFIG", this.CONFIG);
          this.getAllImageSvg();
          this.threadsServiceDataName = String(this.CONFIG['threads']);
          this.configDataNameInAllData = String(this.CONFIG['config']);
          this.getZooKeeperNodeList();
          setTimeout(() => resolve("done"));
       }
       catch(err){
        console.log("Problem in configurations main data reading", err);
       }
      } );//

    });
  }

  getZooKeeperNodeList(){
      try{
        this.http.get(this.CONFIG["ZooKeeperTreeNodes"]).subscribe(data => {
         this.zooKeeperTreeNodeList = data;
        });
      }
      catch(err){
        console.log("ZooKeeper tree was not read correctly ", err);
      }
        return  this.zooKeeperTreeNodeList;
  }


   /** Methods for config data from JSON config file assets/configuration.json */

  getAllImageSvg(){
    try{
      let neededData = this.CONFIG["StreamsIcon"];
      for(let name in neededData) {
        let pathToIcon =  this.CONFIG["StreamsIcon"][name];
        this.matIconRegistry.addSvgIcon(name, this.domSanitizer.bypassSecurityTrustResourceUrl(pathToIcon));
      }
    }
    catch(err){
     console.log("Problem in configurations on registering .svg icons", err);
    }
  }
}

