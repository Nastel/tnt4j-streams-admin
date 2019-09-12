import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { DataService } from '../data.service';
import { Router, RouterModule} from '@angular/router';
import { ControlUtils } from "../utils/control.utils";
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import {MatPaginator, MatTableDataSource, MatSort } from '@angular/material';
//test for block on load
import { BlockUI, NgBlockUI } from 'ng-block-ui';

import { FormGroup, FormControl, Validators } from '@angular/forms';

import * as CryptoJS from 'crypto-js';


@Component({
  selector: 'app-users-control',
  templateUrl: './users-control.component.html',
  styleUrls: ['./users-control.component.scss']
})
export class UserControlComponent implements OnInit {

//ALL FORM DATA
  @ViewChild('form') form;
  formFill: any = {};
  userAdmin = "false";
  username: string;

  @BlockUI() blockUI: NgBlockUI;

  //Registration input params
  allDataFilled : boolean;
  passwordMatch : boolean;
  dropdownList = [];
  dropdownSettings = {};

  public reactiveForm: FormGroup = new FormGroup({
      recaptchaReactive: new FormControl(null, Validators.required)
  });

  //Properties for user rights
  userRightsList: string[] =  ["read","write","create","delete","admin"];
  userRightsListNeeded: string[] =  ["read","action","admin"];
  rightsArray = [];
  rightsForTheUser = new FormControl();
  userRightWithClustersSelected = {};


  //Properties for user clusters
  clustersList: string[];
  clustersListFull: string[];
  clustersChoice = new FormControl();

  //Encrypt data to send and authenticate properties
  encryptSecretKey: string;
  reCaptchaCheck : boolean = false;
  @ViewChild('reCaptcha') reCaptchaComponent;
  reCaptchaKey: string = this.configurationHandler.CONFIG["siteKey"];

  //UsersTableParams
  @ViewChild('matUsersData') sortUsersData: MatSort;
  @ViewChild('paginatorUsersData') paginatorUsersData: MatPaginator;
  displayedColumnsUsers = [];
  dataSourceUsers = new MatTableDataSource<any>();
  pathToData : string;
  usersListForAdmin : string[];

 //UserTableParams
  @ViewChild('matUserData') sortUserData: MatSort;
  @ViewChild('paginatorUserData') paginatorUserData: MatPaginator;
  displayedColumnsUser = [];
  dataSourceUser = new MatTableDataSource<any>();
  checkUncheckedBoxes = [];

  //Params used to display or clear data;.
  tokenName : string
  usernameSession : string;
  innerHeight;
  @ViewChild('scrollMe')private scrollContainer: ElementRef;
  userList : string;

  //Input fields for edit functionality
  firstUsername :string;
  firstPassword :string
  beginningClustersAndRights: {};

  //Form choice configs
  userManagementActionConfig = this.configurationHandler.CONFIG["UserManagementForm_add"];

  formHeaderLabel: string;
  formActionChoice: string = "info";

  constructor(private data: DataService,
              private router: Router,
              private controlUtils : ControlUtils,
              public utilsSvc: UtilsService,
              private configurationHandler:ConfigurationHandler) { }

  ngOnInit(){
    this.userRightWithClustersSelected = {};
    this.rightsArray = this.createRightsToBinaryArray();
    this.loadDataFromSessionAndConfig();
    this.setValuesForDropdownClusterSelect();
  }

  loadDataFromSessionAndConfig(){
      this.username = sessionStorage.getItem('username');
      this.userAdmin = sessionStorage.getItem("admin");
      this.userList = sessionStorage.getItem("userList");
      this.clustersList = this.getUserClusters(this.username);
      this.prepareUserTableData(this.clustersList);
      this.fillCheckUncheckObject(this.clustersList, false);
      this.userManagementActionConfig = this.configurationHandler.CONFIG["UserManagementForm_"+this.formActionChoice];
      this.formHeaderLabel = this.userManagementActionConfig["FormName"];
      this.pathToData = this.router.url.substring(1);
      this.prepareUsersTableData();
      sessionStorage.setItem("pathBeforeReload", this.pathToData);
      this.utilsSvc.navigateToCorrectPathAfterRefreshOrURlChange(this.router);
      this.encryptSecretKey = this.configurationHandler.CONFIG["SecretKeyForEncryption"];
      let tempPath = this.configurationHandler.CONFIG["serviceRegistryStartNode"];
      this.tokenName = this.configurationHandler.CONFIG["sessionTokenName"];
      this.usernameSession = this.configurationHandler.CONFIG["sessionUserName"];
      this.fillStartData();

  }

  /** ------------------------- Only for non admin user  --------------------- */
  fillStartData(){
    this.formFill['username']=this.username
    console.log( this.formFill['username'])
    this.infoUserCall(this.username);
  }
  /** ------------------------- The methods for user actions UTILS  --------------------- */
  setValuesForDropdownClusterSelect(){
    let tempClustersList = sessionStorage.getItem("clustersList");
    if(!this.utilsSvc.compareStrings(tempClustersList, "null")){
      this.clustersListFull= tempClustersList.split(';');
      this.clustersListFull.splice(-1,1);
    }
  }

  onKeyInputRemoveUser(event){
    this.clearFormData();
    let username = event.target.value;
    this.clustersChoice.setValue(this.getUserClusters(username));
    this.clustersList = this.getUserClusters(username);
  }

  getAllUsersList(): string[]{
    let usersList = [];
    this.userList = sessionStorage.getItem("userList");
    if(!this.utilsSvc.compareStrings(this.userList, "undefined")){
      let userListData = JSON.parse(this.userList);
      for(let user in userListData){
        for(let userParam in userListData[user]){
          if(this.utilsSvc.compareStrings(userParam, "username")){
            let tempValue = userListData[user][userParam];
            this.displayedColumnsUsers = this.addParamToListIfNotExists(this.displayedColumnsUsers, userParam);
            usersList = this.addParamToListIfNotExists(usersList, tempValue);
          }
        }
      }
    }
    return usersList;
  }
  prepareUsersTableData(){
    let usersInfo = [];
    this.usersListForAdmin = this.getAllUsersList();
    for(let user in  this.usersListForAdmin){
        let tempUserParamList = [];
        tempUserParamList["username"] =  this.usersListForAdmin[user];
        usersInfo.push(tempUserParamList);
    }
    this.dataSourceUsers = new MatTableDataSource(usersInfo);
    setTimeout(() => this.dataSourceUsers.paginator = this.paginatorUsersData);
    setTimeout(() => this.dataSourceUsers.sort = this.sortUsersData);
  }
  prepareUserTableData(clusterList){
    this.dataSourceUser = new MatTableDataSource<any>();
    this.displayedColumnsUser = [];
    this.displayedColumnsUser.push("cluster");
    let usersInfo = [];
    for(let cluster of clusterList){
      let tempDataForUser = [];
      for(let right of this.userRightsListNeeded){
        this.displayedColumnsUser = this.addParamToListIfNotExists(this.displayedColumnsUser, right);
        tempDataForUser["cluster"] = cluster;
        tempDataForUser[right] = "";
      }
      usersInfo.push(tempDataForUser);
    }
    this.dataSourceUser = new MatTableDataSource(usersInfo);
    setTimeout(() => this.dataSourceUser.paginator = this.paginatorUserData);
    setTimeout(() => this.dataSourceUser.sort = this.sortUserData);
  }
  addParamToListIfNotExists(list, value){
    if(!this.utilsSvc.compareStrings(list, "undefined")){
      list.indexOf(value) === -1 ? list.push(value): "";
      return list;
    }
  }
  applyFilterUsers(filterValue: string) {

    this.dataSourceUsers.filter = filterValue.trim().toLowerCase();

  }
  applyFilterUser(filterValue: string) {

    this.dataSourceUser.filter = filterValue.trim().toLowerCase();

  }
  encryptData(data) {
    try {
      return CryptoJS.AES.encrypt(JSON.stringify(data), this.encryptSecretKey).toString();
    } catch (e) {
      console.log("Problem on encryption: " + e);
    }
  }
  compareIfExists(value){
    if(!(value==null)&&!(value=="")){
         return true;
    }else{ return false; }
  }
  checkTheInput(){
  let allClustersListEmpty = true;
    if(this.utilsSvc.compareStrings(this.formActionChoice , "add") || this.utilsSvc.compareStrings( this.formActionChoice , "edit")){
      if(this.compareIfExists(this.formFill["username"]) && this.compareIfExists(this.formFill["password"])
          && this.compareIfExists(this.formFill["passwordConfirm"]) && this.compareIfExists(this.userRightWithClustersSelected)){
          for(let cluster in this.userRightWithClustersSelected){
              if(this.userRightWithClustersSelected[cluster].includes("read")){
                 allClustersListEmpty = false;
              }
          }
          if(allClustersListEmpty){
             this.controlUtils.openDialogWithHeader("All data fields need to be filled", "Oops", this.pathToData );
             this.allDataFilled = false;
             return;
          }
          if(this.utilsSvc.compareStrings(this.formFill["password"], this.formFill["passwordConfirm"])){
              this.allDataFilled = true;
              this.passwordMatch = true;
          }else{
              this.controlUtils.openDialogWithHeader("Passwords do not match", "Oops", this.pathToData );
              this.passwordMatch = false;
              this.allDataFilled = true;
          }
      }
      else{
        this.controlUtils.openDialogWithHeader("All data fields need to be filled", "Oops", this.pathToData );
        this.passwordMatch = false;
        this.allDataFilled = false;
      }
    }else{
      console.log("here")
      if(this.compareIfExists(this.formFill["username"]) && this.compareIfExists(this.clustersChoice.value)){
        this.allDataFilled = true;
      }else{
        this.controlUtils.openDialogWithHeader("User does not exist be sure that the username matches.", "Oops", this.pathToData );
        this.allDataFilled = false;
      }
    }
  }
  parseDecimalToBinary(n){
     if (n < 0) {n = 0xFFFFFFFF + n + 1;}
     return parseInt(n, 10).toString(2);
  }
  createRightsToBinaryArray(){
    let rightsArrayReverse = this.userRightsList.reverse();
    let iterator = 0;
    let fromBytesToRights =[];
    for(let value of rightsArrayReverse){
      if(iterator!=0){
        fromBytesToRights[value] = 10000/iterator;
        iterator=iterator*10;
      }else{
        fromBytesToRights[value] = 10000;
        iterator=iterator+10;
      }
    }
    return fromBytesToRights;
  }
  showRights(rightNumber){
    let binaryString = Number(this.parseDecimalToBinary(rightNumber));
    let responseArray = [];
    let response: string ="";
    for(let valueBytes in this.rightsArray){
      if(binaryString!=0){
        if((binaryString - this.rightsArray[valueBytes])<0){}
        else{
          binaryString = binaryString - this.rightsArray[valueBytes];
          responseArray.push(valueBytes);
        }
      }
    }
    return responseArray;
  }
  getArrayAsString(arrayObj){
    for(let value in arrayObj){
    let response :string ="";
      for(let key in arrayObj[value]){
        if(this.utilsSvc.compareStrings(response,"")){
          response = arrayObj[value][key];
        }else{
            response = response + ","+arrayObj[value][key];
        }
      }
    this.userRightWithClustersSelected [value]=response;
    }
  }
  fillCheckUncheckObject(clusterList, fillWith){
    for(let cluster of clusterList){
     let rightsList = [];
      for(let rights in this.userRightsListNeeded){
          rightsList[this.userRightsListNeeded[rights]]=fillWith;
      }
         this.checkUncheckedBoxes[cluster] = rightsList;
    }
  }
  checkRightSelectAndAutoSelectIfNeeded(clusterChoice : string, rightsChoice: string){
    if(this.compareIfExists(this.userRightWithClustersSelected)){
      for(let cluster in this.userRightWithClustersSelected){
          let rightsList = [];
          if(this.userRightWithClustersSelected[cluster].length==0){
              rightsList["read"] = false;
              rightsList["action"] = false;
              rightsList["admin"] = false;
          }
          if(this.utilsSvc.compareStrings(clusterChoice, cluster)){
               for(let rightsKey in this.userRightWithClustersSelected[cluster]){
                  let rightsValue = this.userRightWithClustersSelected[cluster][rightsKey];
                  if(this.utilsSvc.compareStrings(rightsChoice, rightsValue)){
                    if(this.utilsSvc.compareStrings(rightsValue, "admin")){
                        rightsList["read"] = true;
                        rightsList["action"] = true;
                    }else if(this.utilsSvc.compareStrings(rightsValue, "action")){
                        rightsList["read"] = true;
                        rightsList["admin"] = false;
                    }else{
                        rightsList["action"] = false;
                        rightsList["admin"] = false;
                    }
                  }
               }
            let rightsForSubmit = [];
            rightsForSubmit = this.userRightWithClustersSelected[cluster];
            for(let right in rightsList){
              if(rightsList[right]){
                rightsForSubmit.push(right);
              }else{
                rightsForSubmit= this.arrayRemoveByValue(rightsForSubmit, right);
              }
            }
           this.userRightWithClustersSelected[cluster]=rightsForSubmit;
           this.checkUncheckedBoxes[cluster] = rightsList;
        }
      }
    }
  }
  onChangeGetRightsAndClusters(cluster :string, chosenRight :string){
    let tempObject = [];
    let rightsArray = [];
    if(this.utilsSvc.compareStrings(this.userRightWithClustersSelected[cluster], "undefined")){
      rightsArray.push(chosenRight);
    }else if(this.userRightWithClustersSelected[cluster].includes(chosenRight)){
       rightsArray = this.userRightWithClustersSelected[cluster];
       rightsArray.splice(0,rightsArray.length);
    }else{
      rightsArray.push(chosenRight);
    }
    tempObject[cluster] = rightsArray;
    this.userRightWithClustersSelected[cluster]=rightsArray;
    this.checkRightSelectAndAutoSelectIfNeeded(cluster, chosenRight);
  }
  arrayRemoveByValue(array, value){
    let index = 0;
    for(let key in array){
        if(this.utilsSvc.compareStrings(array[key], value)){
          array.splice(index,1);
        }
        index++;
    }
    return array;
  }

  /** ------------------------- The methods for edit functionality --------------------- */
  fillEditFormFields(username :string, clustersRightsObject){
    this.beginningClustersAndRights = clustersRightsObject;
    this.firstUsername = username;
    this.firstPassword = "************";
    this.formFill["password"] = "************";
    this.formFill["username"] = username;
    this.formFill["passwordConfirm"] = "************";
  }

  compareToGetIfChangeNeeded(){
    if(this.utilsSvc.compareStrings(this.firstUsername, this.formFill["username"]) && this.utilsSvc.compareStrings(this.firstPassword, this.formFill["password"])){
        if(this.compareClusterRightsObjects(this.beginningClustersAndRights, this.userRightWithClustersSelected)){
          this.controlUtils.openDialogWithHeader("No data fields where changed", "Oops", this.pathToData );
          return false;
        }else{
           return true;
        }
    }else{
        return true;
    }
  }

  compareClusterRightsObjects(clusterRightObjectOne, clusterRightObjectTwo): boolean{
    if(Object.keys(clusterRightObjectOne).length == Object.keys(clusterRightObjectTwo).length){
      for(let cluster in clusterRightObjectOne){
        if(clusterRightObjectOne[cluster].length != clusterRightObjectTwo[cluster].length){
            return false;
        }
      }
    }else{
      return false;
    }
    return true;
  }

  /** ------------------------- The methods to auto fill form fields --------------------- */

  getUserClusters(username :string): any[]{
    let clustersListForUser = [];
    if(!this.utilsSvc.compareStrings(this.userList, "undefined")){
      let userListData = JSON.parse(this.userList);
      for(let userData in userListData){
          if(this.utilsSvc.compareStrings(userListData[userData]["username"], username)){
            let tempValue = userListData[userData]["cluster"];
            clustersListForUser = this.addParamToListIfNotExists(clustersListForUser, tempValue);
          }
      }
    }
    return clustersListForUser;
  }

  getUserClustersWithRights(username :string): any[]{
    let clustersListForUser = [];
    if(!this.utilsSvc.compareStrings(this.userList, "undefined")){
      let userListData = JSON.parse(this.userList);
      for(let userData in userListData){
        if(this.utilsSvc.compareStrings(userListData[userData]["username"], username)){
          let tempValueCluster = userListData[userData]["cluster"];
          let tempValueRights = this.showRights(userListData[userData]["rights"]);
          if(userListData[userData]["action"]){
            tempValueRights.push("action");
          }
          clustersListForUser[tempValueCluster] =  tempValueRights;
        }
      }
    }
    return clustersListForUser;
  }

  AutoFillUserRights(clusterAndRightsMap){
  let data =clusterAndRightsMap;
    for(let cluster in clusterAndRightsMap){
      if(clusterAndRightsMap[cluster].includes("admin")){
          this.onChangeGetRightsAndClusters(cluster, "admin");
      }else if(clusterAndRightsMap[cluster].includes("action")){
          this.onChangeGetRightsAndClusters(cluster, "action");
      }else{
          this.onChangeGetRightsAndClusters(cluster, "read");
      }
    }
  }

  fillFormFields(username: string){
    this.userRightWithClustersSelected = {};
    this.allDataFilled = false;
    this.passwordMatch = false;
    this.userManagementActionConfig = this.configurationHandler.CONFIG["UserManagementForm_"+this.formActionChoice];
    this.formHeaderLabel = this.userManagementActionConfig["FormName"];
    this.clustersChoice.setValue(this.getUserClusters(username));
    if(!this.compareIfExists(username)){
      this.clustersList = this.getUserClusters(sessionStorage.getItem("username"));
    }else{
      this.clustersList = this.getUserClusters(username);
      }
    this.formFill["username"] = username;
  }

  clearFormData(){
     this.fillCheckUncheckObject(this.clustersList, "");
  }
  /** ------------------------- The reCaptcha confirmation methods --------------------- */

  public resolved(captchaResponse: string) {
    if(captchaResponse!=null){
      this.blockUI.start("Checking captcha...");
      console.log(`Resolved captcha with response: ${captchaResponse}`);
      console.log(captchaResponse);
      try{
        this.data.captchaCheck(captchaResponse).subscribe( data => {
        console.log(data["re-captcha"]);

          this.reCaptchaCheck = true;
          this.blockUI.stop();
          if(!this.utilsSvc.isObject(data["re-captcha"])){
            this.controlUtils.openDialogWithHeader(data["re-captcha"], "Message", this.pathToData );
          }
        }, err =>{
           this.reCaptchaCheck = false;
           this.blockUI.stop();
           console.log(err);
         });
      }catch(e){
        console.log("Problem on resolving the reCaptcha response", e)
        this.blockUI.stop();
      }
    }
  }

  /** ------------------------- The methods to refresh the users table information --------------------- */

   async reloadUsersData(username :string){
      let clusterList = sessionStorage.getItem("clustersList");
      this.blockUI.start('Updating users list...');
      let data = {};
      console.log()
      try{
        if(JSON.parse(this.userAdmin)){
          data = await   this.data.loadUsersList("refreshUserList", clusterList).toPromise();
        }else{
          data = await   this.data.loadUserData("refreshUser", clusterList, this.username).toPromise();
        }
      }catch(e){
        this.controlUtils.openDialogWithHeader("Problem occurred on reloading user data", "Error", this.pathToData );
        this.blockUI.stop();
      }
      if(!this.utilsSvc.compareStrings(data["userList"], "undefined")){
        sessionStorage.removeItem("userList");
        sessionStorage.setItem("userList", JSON.stringify(data["userList"]));
        await this.loadDataFromSessionAndConfig();
        if(!this.compareIfExists(this.formFill["username"])){
          this.clustersList = this.getUserClusters(this.formFill["username"]);
        }
        setTimeout(() => this.dataSourceUsers.paginator = this.paginatorUsersData);
        setTimeout(() => this.dataSourceUsers.sort = this.sortUsersData);
      }
      this.blockUI.stop();
      if(this.compareIfExists(username)){
        this.infoUserCall(username);
      }else if(this.compareIfExists(this.formFill['username'])){
        this.infoUserCall(this.formFill['username']);
      }else{
        this.infoUserCall(this.username);
      }
      console.log(this.clustersList)
    }
  /** ------------------------- The methods for user actions ADD, REMOVE, EDIT --------------------- */

  onSubmitCreateUser() {
    this.checkTheInput();
    if(this.allDataFilled && this.passwordMatch && this.reCaptchaCheck){
        this.reCaptchaCheck = false;
        this.blockUI.start('Adding a new user...');
        let tempModal =  this.formFill["password"];
        let username = this.formFill["username"];
        let encryptedPassword = this.encryptData(tempModal);
        this.data.addNewUser(this.userManagementActionConfig["MethodCallName"], username, encryptedPassword, this.userRightWithClustersSelected).subscribe( data => {
          this.form.resetForm();
          this.reloadUsersData(username);
          this.blockUI.stop();
          this.controlUtils.openDialogWithHeader(data["Register"], "Message", this.pathToData );
        }, err =>{
           this.blockUI.stop();
           console.log("Problem on registering a new user");
           console.log(err);
         });
      }else{
        if(!this.reCaptchaCheck){
          this.controlUtils.openDialogWithHeader("reCaptcha component needs to be checked", "Oops", this.pathToData );
        }else{
        this.reCaptchaComponent.reset();
        this.reCaptchaCheck = false;
//        this.form.resetForm();
        }
      }
  }

  onSubmitEditUser(){
    this.checkTheInput();
    if(this.allDataFilled && this.passwordMatch){
      if(this.compareToGetIfChangeNeeded()){
        this.blockUI.start('Editing user information...');
        let username = this.formFill["username"];
        let encryptedPassword = this.encryptData(this.formFill["password"]);
        let clustersWithRights = this.userRightWithClustersSelected;
          if(JSON.parse(this.userAdmin)){
            if(this.utilsSvc.compareStrings(sessionStorage.getItem("username"), username)){
              this.blockUI.stop();
              this.controlUtils.openDialogWithHeader("You can not edit your own user data. Try logging in to another admin account to proceed with this action.", "Error", this.pathToData );
            }else{
              this.data.editUser(this.userManagementActionConfig["MethodCallName"], username, encryptedPassword, clustersWithRights).subscribe( data => {
                this.reloadUsersData(username);
                this.blockUI.stop();
                this.controlUtils.openDialogWithHeader(data["Edit"], "Response", this.pathToData );
              }, err =>{
                 this.blockUI.stop();
                 console.log("Problem on removing user from chosen clusters");
                 console.log(err);
              });
            }
          }else{
                  this.blockUI.start('Editing user information...');
            this.data.editUserNonAdmin(this.userManagementActionConfig["MethodCallNameAdminFalse"], username, encryptedPassword, clustersWithRights).subscribe( data => {
              this.reloadUsersData(username);
              this.controlUtils.openDialogWithHeader(data["Edit"], "Response", this.pathToData );
              this.blockUI.stop();
            }, err =>{
               this.blockUI.stop();
               console.log("Problem on removing user from chosen clusters");
               console.log(err);
            });
            this.blockUI.stop();
          }
            this.blockUI.stop();
      }
    }
  }

  onSubmitRemoveUser(){
    this.checkTheInput();
    if(this.allDataFilled){
      this.blockUI.start('Removing the user data...');
      let username = this.formFill["username"];
      let clusterName = this.clustersChoice.value;
      console.log("username", username, "cluster name", clusterName);
      if(this.utilsSvc.compareStrings(sessionStorage.getItem("username"), username)){
          this.blockUI.stop();
          this.controlUtils.openDialogWithHeader("You can not delete your own user data. Try logging in to another admin account to proceed with this action.", "Error", this.pathToData );
      }else{
        this.data.removeUser(this.userManagementActionConfig["MethodCallName"], username, clusterName).subscribe( data => {
          this.form.resetForm();
          this.reloadUsersData("");
          this.blockUI.stop();
          this.controlUtils.openDialogWithHeader(data["Remove"], "Response", this.pathToData );
        }, err =>{
           this.blockUI.stop();
           console.log("Problem on removing user from chosen clusters");
           console.log(err);
        });
      }
    }
  }

  addUserCall(): void {
    this.formActionChoice = "add";
    this.formFill["password"] = "";
    this.formFill["passwordConfirm"] = "";
    this.fillFormFields("");
    this.prepareUserTableData(this.clustersList);
    this.fillCheckUncheckObject(this.clustersList, false);
    this.form.resetForm();
  }

  removeUserCall(username: string){
    this.formActionChoice = "remove";
    this.fillFormFields(username)
  }

  editUserCall(username: string){
    console.log(this.clustersListFull);
    console.log(this.clustersList);
    if(this.compareIfExists(this.clustersListFull)){
      this.fillCheckUncheckObject(this.clustersListFull, false);
    }else{
      console.log("okay");
      this.fillCheckUncheckObject(this.clustersList, false);
    }
    this.formActionChoice = "edit";
    this.fillFormFields("");
    this.prepareUserTableData(this.clustersList);
    let userListForAutoFillRights = this.getUserClustersWithRights(username);
    this.fillEditFormFields(username, userListForAutoFillRights);
    this.AutoFillUserRights(userListForAutoFillRights);
  }

  infoUserCall(username: string){
    this.formActionChoice = "info";
    this.fillFormFields(username);
    this.prepareUserTableData(this.clustersList);
    let userListForAutoFillRights = this.getUserClustersWithRights(username);
    this.AutoFillUserRights(userListForAutoFillRights);
  }

  cancelAction(username: string){
    if(JSON.parse(this.userAdmin)){
    console.log(this.formActionChoice)
      if(this.utilsSvc.compareStrings(this.formActionChoice, 'add')){
        this.formFill["username"] = this.username;
        this.infoUserCall(this.username);
      }else{
         if(!this.compareIfExists(username)){
          this.infoUserCall(username);
         }
        this.formFill["username"] = username;
      }
    }else{
     this.infoUserCall(username);
    }
    this.formActionChoice = "info";
    this.userManagementActionConfig = this.configurationHandler.CONFIG["UserManagementForm_"+this.formActionChoice];
    this.formHeaderLabel = this.userManagementActionConfig["FormName"];
  }

  chooseSubmit(){
    if(this.utilsSvc.compareStrings(this.formActionChoice, 'add')){
      this.onSubmitCreateUser();
    } else if(this.utilsSvc.compareStrings(this.formActionChoice, 'edit')){
      this.onSubmitEditUser();
    } else if(this.utilsSvc.compareStrings(this.formActionChoice, 'remove')){
      this.onSubmitRemoveUser();
    }
  }


}
