import {Injectable} from '@angular/core';
import { MatIconRegistry } from "@angular/material";
import {ConfigurationHandler} from '../config/configuration-handler';
import { DataService } from '../data.service';
import { popupMessage } from './popup.message';


import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';

import * as moment from 'moment';

declare var jsPDF: any;

@Injectable ({  providedIn: 'root'})

export class ControlUtils
{

  /** parameters for response */

  responseResult = "";


  constructor (private configurationHandler: ConfigurationHandler,
               private matIconRegistry: MatIconRegistry,
               private data: DataService,
               public dialog: MatDialog){}

  public getCurrentTime (): Date{
   let jsonDate = new Date ();
   return new Date (jsonDate.getTime () + jsonDate.getTimezoneOffset () * 60000)
  }

  public stopStream(path : String){
     path = path + "/" + "stop";
     this.data.sendControlsRequest(path, "stop").subscribe( data => {
          try{
            let result = data;
             this.responseResult =  JSON.parse(result.toString());
            console.log("Response to stream stop ",  this.responseResult);
          }catch(err){
            console.log("Problem while trying to stop stream" , err);
          }
        },
         err =>{
          console.log("Problem while trying to stop stream" , err);
         }
      );
  }

  public startStream(path : String){
     path = path + "/" + "start";
     this.data.sendControlsRequest(path, "start").subscribe( data => {
          try{
            let result = data;
             this.responseResult =  JSON.parse(result.toString());
             console.log("Response to stream start ",  this.responseResult);
          }catch(err){
            console.log("Problem while trying to start stream" , err);
          }
        },
         err =>{
            console.log("Problem while trying to start stream" , err);
         }
      );
  }

  public pauseStream(path : String){
     path = path + "/" + "pause";
     this.data.sendControlsRequest(path, "pause").subscribe( data => {
          try{
            let result = data;
             this.responseResult =  JSON.parse(result.toString());
            console.log("Response to stream pause ",  this.responseResult);
          }catch(err){
             console.log("Problem while trying to pause stream" , err);
          }
        },
         err =>{
            console.log("Problem while trying to pause stream" , err);
         }
      );
  }

  public resumeStream(path : String){
     path = path + "/" + "resume";
     this.data.sendControlsRequest(path, "resume").subscribe( data => {
          try{
            let result = data;
             this.responseResult =  JSON.parse(result.toString());
            console.log("Response to stream resume ",  this.responseResult);
          }catch(err){
            console.log("Problem while trying to resume stream" , err);
          }
        },
         err =>{
           console.log("Problem while trying to resume stream" , err);
         }
      );
  }


  public replayBlock(path : String, blockNumber : number){
     path = path + "/" + blockNumber;
     this.data.sendControlsRequest(path, "blockReplay").subscribe( data => {
          try{
            let result = data;
             this.responseResult =  JSON.parse(result.toString());
            console.log("Response to block replay ",  this.responseResult);
            this.openDialog();
          }catch(err){
            console.log("Problem while trying to replay block "+ blockNumber, err);
          }
        },
         err =>{
           console.log("Problem while trying to replay block "+ blockNumber, err);
         }
      );
  }

  openDialog(): void {
    const dialogRef = this.dialog.open(popupMessage, {
      width: '50em',
      data: { response: this.responseResult }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
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
              result =  JSON.parse(result.toString());
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
