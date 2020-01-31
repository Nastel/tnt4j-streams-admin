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

import { Component,ViewChild, OnInit } from '@angular/core';
import { DataService } from '../data.service';
import { Router, RouterModule} from '@angular/router';
import { ControlUtils } from "../utils/control.utils";
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { BlockUI, NgBlockUI } from 'ng-block-ui';

import * as CryptoJS from 'crypto-js';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.sass']
})
export class LoginComponent implements OnInit {

  //Response form back-end
  loginResponse: string;
  result: {};
  encryptSecretKey: string;
  pathToData : string;

  //reCaptcha
  reCaptchaCheck : boolean = false;
  @ViewChild('reCaptcha') reCaptchaComponent;
  reCaptchaKey: string = this.configurationHandler.CONFIG["siteKey"];

  //Form data
  model: any = {};

  //Params used to display or clear data;.
  tokenName : string
  usernameSession : string;
  @BlockUI() blockUI: NgBlockUI;
  captchaResponse: string;

  constructor(private data: DataService,
              private router: Router,
              private controlUtils : ControlUtils,
              public utilsSvc: UtilsService,
              private configurationHandler:ConfigurationHandler) { }


  ngOnInit() {
    this.pathToData = this.router.url.substring(1);
    this.encryptSecretKey = this.configurationHandler.CONFIG["SecretKeyForEncryption"];
    let tempPath = this.configurationHandler.CONFIG["serviceRegistryStartNode"];
    this.tokenName = this.configurationHandler.CONFIG["sessionTokenName"];
    this.usernameSession = this.configurationHandler.CONFIG["sessionUserName"];
    let tempSession = sessionStorage.getItem(this.tokenName);
    if(tempSession){
       this.router.navigate(["/"+tempPath])
    }
  }

  onSubmit() {
    this.blockUI.start("Loading...");
    let pathToData = this.router.url.substring(1);
    let tempModal =  this.model["password"];
    tempModal = this.encryptData(tempModal);
    if(this.captchaResponse!=null){
      sessionStorage.setItem( this.usernameSession, this.model["username"]);
      this.data.getBackEndLoginResponse(pathToData, tempModal, this.model["username"], this.captchaResponse).subscribe( data => {
        this.result = data;
          if(!this.utilsSvc.compareStrings(this.result["token"], "") && !this.utilsSvc.compareStrings(this.result["token"], "undefined") && this.reCaptchaCheck){
            sessionStorage.setItem(this.tokenName, this.result["token"]);
            sessionStorage.setItem("admin", this.result["admin"]);
            if(!this.utilsSvc.compareStrings(this.result["userList"], "undefined")){
              sessionStorage.setItem("userList", JSON.stringify(this.result["userList"]));
            }
            this.blockUI.stop();
            location.reload();
          } else{
            this.doNotVerify();
            this.blockUI.stop();
            this.controlUtils.openDialogWithHeader("Problem, on confirming form input! Did you fill all the data fields needed?", "Ooops", pathToData);
          }
      }, err =>{
          this.doNotVerify();
          this.blockUI.stop();
          console.log("Problem on authorizations");
          this.model["password"] = "";
          console.log(err);
       });
     }else{
       this.blockUI.stop();
       this.controlUtils.openDialogWithHeader("Problem on resolving the reCaptcha response", "Response", this.pathToData );
       this.doNotVerify();
     }
  }

  encryptData(data) {
      try {
        return CryptoJS.AES.encrypt(JSON.stringify(data), this.encryptSecretKey).toString();
      } catch (e) {
        console.log("Problem on encryption: " + e);
      }
    }

  /** ------------------------- The reCaptcha confirmation methods --------------------- */

//  public resolved(captchaResponse: string) {
//    if(captchaResponse!=null){
//      this.blockUI.start("Checking captcha...");
//      console.log(`Resolved captcha with response: ${captchaResponse}`);
//      console.log(captchaResponse);
//      try{
//        this.data.captchaCheckUnauthorized("unauthorized",captchaResponse).subscribe( data => {
//        let result = data["re-captcha"];
//        console.log(data["re-captcha"]);
//          if(result['success']){
//            this.reCaptchaCheck = true;
//          }else{
//            this.reCaptchaCheck = false;
//            this.doNotVerify();
//            this.controlUtils.openDialogWithHeader(result['success'], "Response", this.pathToData );
//          }
//          this.blockUI.stop();
//          if(!this.utilsSvc.isObject(data["re-captcha"])){
//            this.controlUtils.openDialogWithHeader(data["re-captcha"], "Response", this.pathToData );
//          }
//        }, err =>{
//           this.doNotVerify();
//           this.reCaptchaCheck = false;
//           this.blockUI.stop();
//           console.log(err);
//         });
//      }catch(e){
//        console.log("Problem on resolving the reCaptcha response", e)
//        this.blockUI.stop();
//      }
//    }else{
//       this.controlUtils.openDialogWithHeader("Problem on resolving the reCaptcha response", "Response", this.pathToData );
//       this.doNotVerify();
//     }
//  }


  public resolved(captchaResponse: string) {
    if(captchaResponse!=null){
      this.reCaptchaCheck = true;
      this.captchaResponse = captchaResponse;
    }else{
      this.reCaptchaCheck = false;
    }
  }
  doNotVerify(){
      this.reCaptchaComponent.reset();
  }


}
