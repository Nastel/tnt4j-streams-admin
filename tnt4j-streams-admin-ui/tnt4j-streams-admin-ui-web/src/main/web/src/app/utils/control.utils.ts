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

import {Injectable} from '@angular/core';
import { MatIconRegistry } from "@angular/material";
import {ConfigurationHandler} from '../config/configuration-handler';
import { DataService } from '../data.service';
import { popupMessage } from './popup.message';
import { UtilsService } from "./utils.service";
import { Router } from '@angular/router';
import { BlockUI, NgBlockUI } from 'ng-block-ui';

import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';

import * as moment from 'moment';

declare var jsPDF: any;

@Injectable ({  providedIn: 'root'})

export class ControlUtils
{

  /** parameters for response */
  responseResult;

  /** Url address */
  pathToData : string;


  @BlockUI() blockUI: NgBlockUI;

  constructor (private configurationHandler: ConfigurationHandler,
               private matIconRegistry: MatIconRegistry,
               private data: DataService,
               private router: Router,
               public dialog: MatDialog,
               public utilsSvc: UtilsService){}

  ngOnInit() {
  }

  public getCurrentTime (): Date{
   let jsonDate = new Date ();
   return new Date (jsonDate.getTime () + jsonDate.getTimezoneOffset () * 60000)
  }

  public stopStream(path : String){
     path = path + "/" + "_stop";
     this.blockUI.start("Stopping stream...");
     this.pathToData = this.router.url.substring(1);
     this.data.sendControlsRequest(path, "stop").subscribe( data => {
          try{
            let result = data;
             this.responseResult = result;
             console.log(this.responseResult);
             if(!this.utilsSvc.compareStrings(this.responseResult["action"],"undefined")){
               this.openDialogWithHeader(this.responseResult["action"], "Success", this.pathToData);
               this.blockUI.stop();
             }
          }catch(err){
            this.blockUI.stop();
            console.log("Problem while trying to stop stream" , err);
            this.openDialogWithHeader(this.responseResult["action"], "Failed", this.pathToData);
          }
        },
         err =>{
            this.blockUI.stop();
            console.log("Problem while trying to stop stream" , err);
            this.openDialogWithHeader(this.responseResult["action"], "Failed", this.pathToData);
         }
      );
  }

  public startStream(path : String){
     path = path + "/" + "_start";
     this.blockUI.start("Starting stream...");
     this.pathToData = this.router.url.substring(1);
     this.data.sendControlsRequest(path, "start").subscribe( data => {
          try{
             let result = data;
             if(this.utilsSvc.compareStrings(null, result)){
               this.openDialogWithHeader("No response was sent", "Failed", this.pathToData);
             }
             this.responseResult = result;
             console.log(this.responseResult);
             if(!this.utilsSvc.compareStrings(this.responseResult["action"],"undefined")){
                 this.blockUI.stop();
                 this.openDialogWithHeader(this.responseResult["action"], "Success", this.pathToData);
             }
          }catch(err){
            this.blockUI.stop();
            console.log("Problem while trying to start stream" , err);
            this.openDialogWithHeader(this.responseResult["action"], "Failed", this.pathToData);
          }
        },
         err =>{
            this.blockUI.stop();
            console.log("Problem while trying to start stream" , err);
            this.openDialogWithHeader(this.responseResult["action"], "Failed", this.pathToData);
         }
      );
  }

    public updateStream(path : String){
//       path = path + "/" + "_stop";
//       this.blockUI.start("Stopping stream...");
//       this.pathToData = this.router.url.substring(1);
       path = this.configurationHandler.CONFIG["Rundeck"]["UpdateStream"];
//       console.log(path);
       this.data.sendUpdateStreamRequest(path).subscribe( data => {
            try{
              let result = data;
               this.responseResult = result;
               console.log(this.responseResult);
               if(!this.utilsSvc.compareStrings(this.responseResult["action"],"undefined")){
                 this.openDialogWithHeader(this.responseResult["action"], "Success", this.pathToData);
                 this.blockUI.stop();
               }
            }catch(err){
              this.blockUI.stop();
              console.log("Problem while trying to stop stream" , err);
              this.openDialogWithHeader(this.responseResult["action"], "Failed", this.pathToData);
            }
          },
           err =>{
              this.blockUI.stop();
              console.log("Problem while trying to stop stream" , err);
              this.openDialogWithHeader(this.responseResult["action"], "Failed", this.pathToData);
           }
        );
    }

  public replayBlock(path : String, blockNumber : number){
     path = path + "/" + blockNumber;
     this.blockUI.start("Replaying block...");
     this.pathToData = this.router.url.substring(1);
     this.data.sendControlsRequest(path, "blockReplay").subscribe( data => {
        this.pathToData = this.router.url.substring(1);
          try{
            let result = data;
             this.responseResult = result;
             console.log(this.responseResult);
             console.log("Response of block replay",  this.responseResult);
             if(this.utilsSvc.compareStrings(this.responseResult["action"],"undefined")){
                console.log("No result defined");
                result = this.responseResult["action"];
                this.responseResult = result;
             }else if(this.utilsSvc.compareStrings(this.responseResult["action"],"null")){
                this.openDialogWithHeader("There was an error accessing the replay functionality, response returned: "+
                +this.responseResult["action"]+". Please check TomCat logs for more details", "Error", this.pathToData);
             }else{
                this.openDialogWithHeader("Block "+this.responseResult["action"]+" replay started", "Success", this.pathToData);
             }
            this.blockUI.stop();
          }catch(err){
            this.blockUI.stop();
            console.log("Problem while trying to replay block "+ blockNumber, err);
            this.openDialogWithHeader(this.responseResult["action"], "Failed", this.pathToData);
          }
        },
         err =>{
            this.blockUI.stop();
            console.log("Problem while trying to replay block "+ blockNumber, err);
            this.openDialogWithHeader(this.responseResult["action"], "Failed", this.pathToData);
         }
      );
  }

  /** A popup message that shows the response returned for stream control calls*/
  openDialogNoArguments(): void {
   console.log("Response to block replay ",  this.responseResult);
    const dialogRef = this.dialog.open(popupMessage, {
      data: { response: this.responseResult }
    });

    dialogRef.afterClosed().subscribe(result => {
//      console.log('The dialog was closed');
    });
  }

  /** A popup message that shows a string that is passed as variable "message"*/
  openDialog(message :string, dataPathStart :string): void {
  let currentPath = this.router.url.substring(1);
    if(this.utilsSvc.compareStrings(currentPath, dataPathStart)){
      let data = "ERROR"
      const dialogRef = this.dialog.open(popupMessage, {
        data: { response: message, header : data }
      });

      dialogRef.afterClosed().subscribe(result => {
//        console.log('The dialog was closed');
      });
    }
  }

  /** A popup message that shows a string that is passed as variable "message" and has a header "headerMessage" */
  openDialogWithHeader(message :string, headerMessage :string, dataPathStart: string): void {

   let currentPath = this.router.url.substring(1);
    if(this.utilsSvc.compareStrings(currentPath, dataPathStart)){
      const dialogRef = this.dialog.open(popupMessage, {
        data: { response: message, header : headerMessage }
      });
      dialogRef.afterClosed().subscribe(result => {
//        console.log('The dialog was closed');
      });
    }
  }

    /** A popup message that shows a string that is passed as variable "message" and has a header "headerMessage" */
    openDialogWithHeaderTokenExpiration(message :string, headerMessage :string, dataPathStart: string): void {
     let currentPath = this.router.url.substring(1);
//      console.log(currentPath, dataPathStart);
      if(this.utilsSvc.compareStrings(currentPath, dataPathStart)){
        const dialogRef = this.dialog.open(popupMessage, {
          data: { response: message, header : headerMessage }
        });
        dialogRef.afterClosed().subscribe(result => {
//          console.log(currentPath);
          this.router.navigate(["/"+currentPath])
        });
      }
    }

  /** A popup message that shows a string that is passed as variable "message" and has a header "headerMessage" */
  openDialogWithHeaderNoPath(message :string, headerMessage :string): void {
    const dialogRef = this.dialog.open(popupMessage, {
      data: { response: message, header : headerMessage }
    });
    dialogRef.afterClosed().subscribe(result => {
//      console.log('The dialog was closed');
    });
  }


/** TODO : Implement if query block replay will be needed*/
  public replayBlocks(path : String, blockList){
      for(let block in blockList){
        path = path
      }
    console.log("Path to data", path , "Block number to replay", blockList);
       this.data.sendControlsRequest(path, "blockReplay").subscribe( data => {
            try{
              let result = data;
              result =  result; //JSON.parse(result.toString());
              console.log("Response to block replay ", result);
            }catch(err){
              console.log("Problem while trying to replay block "+ blockList, err);
            }
          },
           err =>{
             console.log("Problem while trying to replay block "+ blockList, err);
           }
        );
    }
}
