import { Component, OnInit } from '@angular/core';
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

  //Form data
  model: any = {};

  //Params used to display or clear data;.
  tokenName : string
  usernameSession : string;
  @BlockUI() blockUI: NgBlockUI;

  constructor(private data: DataService,
              private router: Router,
              private controlUtils : ControlUtils,
              public utilsSvc: UtilsService,
              private configurationHandler:ConfigurationHandler) { }


  ngOnInit() {
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
    sessionStorage.setItem( this.usernameSession, this.model["username"]);
    this.data.getBackEndLoginResponse(pathToData, tempModal, this.model["username"]).subscribe( data => {
      this.result = data;
      console.log("the response from back end", JSON.stringify(this.result));
        if(!this.utilsSvc.compareStrings(this.result["token"], "") || !this.utilsSvc.compareStrings(this.result["token"], "undefined")){
          sessionStorage.setItem(this.tokenName, this.result["token"]);
          sessionStorage.setItem("admin", this.result["admin"]);
          if(!this.utilsSvc.compareStrings(this.result["userList"], "undefined")){
            sessionStorage.setItem("userList", JSON.stringify(this.result["userList"]));
          }
          this.blockUI.stop();
          location.reload();
        }
        else{
          this.blockUI.stop();
          this.controlUtils.openDialogWithHeader(this.result.toString(), "Ooops", pathToData);
        }
    }, err =>{
        this.blockUI.stop();
        console.log("Problem on authorizations");
        this.model["password"] = "";
        console.log(err);
     });
  }

  encryptData(data) {
      try {
        return CryptoJS.AES.encrypt(JSON.stringify(data), this.encryptSecretKey).toString();
      } catch (e) {
        console.log("Problem on encryption: " + e);
      }
    }


}
