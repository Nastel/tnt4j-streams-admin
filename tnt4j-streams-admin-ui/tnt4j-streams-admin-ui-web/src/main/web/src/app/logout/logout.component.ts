import { Component, OnInit } from '@angular/core';
import { DataService } from '../data.service';
import { Router, RouterModule} from '@angular/router';
import { ControlUtils } from "../utils/control.utils";
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { BlockUI, NgBlockUI } from 'ng-block-ui';

@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.sass']
})
export class LogoutComponent implements OnInit {

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
