import {Injectable} from '@angular/core';
import { MatIconRegistry } from "@angular/material";
import {ConfigurationHandler} from '../config/configuration-handler';
import { DataService } from '../data.service';
import * as moment from 'moment';

declare var jsPDF: any;

@Injectable ({  providedIn: 'root'})

export class ControlUtils
{

  public repositoryDefaultNoPathName = "repository";
  public serviceResponds = "ServiceResponds";
  public theValueUsedIsNotSet = "undefined";
  public activeRepoShow = "activeRepo";
  public materialTableForRepositoryBigScreen = "StreamsRepo";
  public materialTableForStreamingServiceBigScreen = "StreamsData";
  public serviceDataGetDone = "SpinnerFalse"

  public existingLinksForStreams = "ExistingStreamsLinksGenerate";

  constructor (private configurationHandler: ConfigurationHandler,
               private matIconRegistry: MatIconRegistry,
               private data: DataService){}

  public getCurrentTime (): Date{
   let jsonDate = new Date ();
   return new Date (jsonDate.getTime () + jsonDate.getTimezoneOffset () * 60000)
  }

  public stopStream(path : String){
     path = path + "/" + "stop";
     this.data.sendControlsRequest(path, "stop").subscribe( data => {
          try{
            let result = data;
            result =  JSON.parse(result.toString());
            console.log("Response to stream stop ", result);
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
            result =  JSON.parse(result.toString());
             console.log("Response to stream start ", result);
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
            result =  JSON.parse(result.toString());
            console.log("Response to stream pause ", result);
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
            result =  JSON.parse(result.toString());
            console.log("Response to stream resume ", result);
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
            result =  JSON.parse(result.toString());
            console.log("Response to block replay ", result);
          }catch(err){
            console.log("Problem while trying to replay block "+ blockNumber, err);
          }
        },
         err =>{
           console.log("Problem while trying to replay block "+ blockNumber, err);
         }
      );
  }

/** TODO : Implement if needed not sure yet */

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
