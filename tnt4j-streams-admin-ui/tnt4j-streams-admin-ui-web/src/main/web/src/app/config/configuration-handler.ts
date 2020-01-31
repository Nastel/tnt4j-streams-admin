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

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { MatIconRegistry } from "@angular/material";
import { DomSanitizer } from "@angular/platform-browser";
import { HttpHeaders } from '@angular/common/http';

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
         // this.getAllImageSvg();
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
        var headers = this.returnHeaderWithToken();
        this.http.get(this.CONFIG["BaseAddress"] + this.CONFIG["ZooKeeperTreeNodes"], headers).subscribe(data => {
         this.zooKeeperTreeNodeList = data;
        });
      }
      catch(err){
        console.log("ZooKeeper tree was not read correctly ", err);
      }
        return  this.zooKeeperTreeNodeList;
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
}

