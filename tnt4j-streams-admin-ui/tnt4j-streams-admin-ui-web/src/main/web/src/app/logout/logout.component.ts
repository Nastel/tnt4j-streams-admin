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

import { Component, OnInit, ViewChild } from '@angular/core';
import { DataService } from '../data.service';
import { Router, RouterModule} from '@angular/router';
import { ControlUtils } from "../utils/control.utils";
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { BlockUI, NgBlockUI } from 'ng-block-ui';
import { MatMenuTrigger } from '@angular/material';

@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.scss']
})
export class LogoutComponent implements OnInit {

  @ViewChild('matMenu') ddTrigger: MatMenuTrigger;

  //Path to current node.
  pathToData: string;

  //The response after trying to log out.
  result: {};
  returnBack : boolean;
  //Params used to display or clear data;.
  tokenName : string
  usernameSession : string;
  username : string;
  userActiveToken : string;
  userAdmin = "false";
  @BlockUI() blockUI: NgBlockUI;

  value: string;
  selectedValue: string;

  cancelClick(ev: MouseEvent) {
    ev.stopPropagation();
  }

  onCancel() {
    this.value = undefined;
    this.ddTrigger.closeMenu();
  }

  onSave() {
    this.selectedValue = this.value;
    this.value = undefined;
    this.ddTrigger.closeMenu();
  }

  constructor(private data: DataService,
              private router: Router,
              private controlUtils : ControlUtils,
              public utilsSvc: UtilsService,
              private configurationHandler:ConfigurationHandler) { }

    logout(){
      this.blockUI.start("Loading...");
      let tempPath = this.configurationHandler.CONFIG["serviceRegistryStartNode"];
      this.data.getLogoutRequest(this.pathToData, "logout").subscribe( data => {
        this.result = data;
        if(this.result){
        sessionStorage.clear();
          this.userActiveToken = "";
          this.username = "";

          this.blockUI.stop();
          this.router.navigate(["/login"])
        }
        else{
          this.blockUI.stop();
          location.reload();
          this.controlUtils.openDialogWithHeader("Authorization problem ", "No token detected", this.pathToData);
        }
      },
      err => {
          this.blockUI.stop();
          location.reload();
          this.controlUtils.openDialogWithHeader("Authorization problem ", "No token detected", this.pathToData);
      });
    }

  ngOnInit() {
    this.tokenName = this.configurationHandler.CONFIG["sessionTokenName"];
    this.usernameSession = this.configurationHandler.CONFIG["sessionUserName"];
    this.userActiveToken = sessionStorage.getItem(this.tokenName);
    this.userAdmin = sessionStorage.getItem("admin");
    this.username = sessionStorage.getItem(this.usernameSession);
    this.pathToData = this.router.url.substring(1);
    let userPath = this.configurationHandler.CONFIG["BasePathToUsersPage"];
    if(this.utilsSvc.valueExists(this.pathToData)){
        this.returnBack = false;
    }
    else if(this.utilsSvc.compareStrings(this.pathToData, userPath)){
       this.returnBack = true;
    }else{
      this.returnBack = false;
    }
  }

  ngAfterContentChecked(){
    this.pathToData = this.router.url.substring(1);
    let userPath = this.configurationHandler.CONFIG["BasePathToUsersPage"];
      if(this.utilsSvc.compareStrings(this.pathToData, userPath)){
         this.returnBack = true;
      }else{
        this.returnBack = false;
      }
  }



  redirectToUsersPage(){
      this.pathToData = this.router.url.substring(1);
      let userPath = this.configurationHandler.CONFIG["BasePathToUsersPage"];
      if(this.utilsSvc.compareStrings(this.pathToData, userPath)){
        this.returnBack = true;
        this.router.navigate([this.configurationHandler.CONFIG["BasePathHide"]+"/clusters"]);
      }else{
        this.returnBack = false;
        this.router.navigate(["/"+userPath]);
      }
  }

}
