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

  public replayBlock(path : String, blockNumber: number){
  console.log("Path to data", path , "Block number to replay", blockNumber);
     path = path + "/" + blockNumber;
     this.data.sendBlockReplayRequest(path).subscribe( data => {
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

  public replayBlocks(path : String, blockList){
      for(let block in blockList){
        path = path
      }
    console.log("Path to data", path , "Block number to replay", blockList);
       this.data.sendBlockReplayRequest(path).subscribe( data => {
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
