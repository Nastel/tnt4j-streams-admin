import {Injectable} from '@angular/core';
import { MatIconRegistry } from "@angular/material";
import {ConfigurationHandler} from '../config/configuration-handler';
import * as moment from 'moment';

declare var jsPDF: any;

@Injectable ({
               providedIn: 'root'
             })
export class UtilsService
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
               private matIconRegistry: MatIconRegistry,)
  {
  }

  public checkIfRepository (stringRepoName)
  {
    if (this.compareStrings (stringRepoName, this.repositoryDefaultNoPathName))
    {
      return true;
    }
    else
    { return false; }
  }

  navigateToCorrectPathAfterRefreshOrURlChange(router){
    let authToken = sessionStorage.getItem("authToken");
    let isAdmin: boolean = JSON.parse(sessionStorage.getItem("admin"));
    let currentPath = sessionStorage.getItem("pathBeforeReload");
    let userPath = this.configurationHandler.CONFIG["BasePathToUsersPage"];
    if(!authToken || this.compareStrings(authToken, "null")){
       router.navigate(['/login']);
    }else if(this.compareStrings(currentPath, "null") || this.compareStrings(currentPath, "undefined")){
       router.navigate([this.configurationHandler.CONFIG["BasePathHide"]+'/clusters']);
    }else{
//      console.log("Do not navigate");
    }
  }

  public getCurrentTime (): Date
  {
    let jsonDate = new Date ();
    return new Date (jsonDate.getTime () + jsonDate.getTimezoneOffset () * 60000)

  }

  public getDataFromTimestamp(timestamp): string{
     let date = new Date(timestamp * 1000)
     let dateValues = "N/A"
     let tempMonth = +date.getMonth()+1;
     if( tempMonth < 10){
       dateValues = date.getFullYear() + "-" + 0 + tempMonth + "-" + date.getDate()  + " " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
     }
     else{
       dateValues = date.getFullYear() + "-" + tempMonth + "-" + date.getDate()  + " " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
     }
     return dateValues.toString();
  }

  public convertToUTCTime (fieldValue, dateFormatPipe): any
  {
    try
    {
      let jsonDate = new Date (fieldValue / 1000);
      let newUTC = new Date (jsonDate.getTime () + jsonDate.getTimezoneOffset () * 60000);
      let result = dateFormatPipe.transform (newUTC);
      return result;
    }
    catch (error)
    {
      return fieldValue;
    }
  }

  public compareStrings (str1, str2)
  {
    if (String (str1).replace (/\s/g, "") === String (str2).replace (/\s/g, ""))
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  public valueExists(value){
    if(!this.compareStrings(value, "undefined")){
      if(!(value==null)&&!(value=="")){
           return true;
      }else{ return false; }
    }else{ return false; }
  }

  public changeStringToString (idx: String): string
  {
    return idx.toString ();
  }

  public getColorForText(): string{
       return  this.configurationHandler.CONFIG["PageColorConfigs"]["TitleTextColor"];
  }

  public getColorTableLabels(): string{
       return  this.configurationHandler.CONFIG["PageColorConfigs"]["colorTableTextLabels"];
  }

  public getColorAgentListService(value, columns): string{
    if(value[columns]){
      return "";
    }
    else{
       return this.configurationHandler.CONFIG["PageColorConfigs"]["agentWindowStreamNoResponse"];
       }
  }

  public getColorAgentList(value): string{
    if(value){
      return "";
    }
    else{
       return this.configurationHandler.CONFIG["PageColorConfigs"]["agentWindowStreamNoResponse"];
       }
  }

   public getColorFooter(): string{
       return this.configurationHandler.CONFIG["PageColorConfigs"]["footerColor"];
   }


  public getColorFalseAgent(value): string{
    if(value){
       return this.configurationHandler.CONFIG["PageColorConfigs"]["agentWindowStreamNoResponse"];
    }
    else{
       return "";
       }
  }

    public getColorInnerLabels(): string{
         return this.configurationHandler.CONFIG["PageColorConfigs"]["colorInnerComponentLabels"];
    }

    public getColorBackground(): string{
       return this.configurationHandler.CONFIG["PageColorConfigs"]["componentsBackgroundColor"];
    }

  public getColor (str)
  {
    if (!this.compareStrings (str, 'undefined)'))
    {
      if (this.compareStrings (str, 'true'))
      { return  this.configurationHandler.CONFIG["PageColorConfigs"]["goodResponseHeaderFooter"];}
      else
      {
        return this.configurationHandler.CONFIG["PageColorConfigs"]["badResponse"];;
      }
    }
    else
    {
      return this.configurationHandler.CONFIG["PageColorConfigs"]["badResponse"];;
    }
  }

  public getColorAllPage (str)
  {
    if (!this.compareStrings (str, 'undefined)'))
    {
      if (this.compareStrings (str, 'true'))
      { return "#000000"; }
      else
      {
        return "#e00000";
      }
    }
    else
    {
      return "#e00000";
    }
  }

  public isObject (val)
  {
    return typeof val === 'object';
  }

  public convertDateToAge (dateReceived: number)
  {
    if (dateReceived == undefined)
    {
      return "N/A";
    }
    var dateNow = Date.now ()
    var dateDiff = Math.abs (dateNow - dateReceived);

    var time = moment.duration (dateDiff);

    if (time.years () != 0)
    {
      return time.years () + "y " + time.months () + "mm" + " ago";
    }
    else if (time.months () != 0)
    {
      return time.months () + "y " + time.days () + "d" + " ago";
    }
    else if (time.days () != 0)
    {
      return time.days () + "d " + time.hours () + "h" + " ago";
    }
    else if (time.hours () != 0)
    {
      return time.hours () + "h " + time.minutes () + "m" + " ago";
    }
    else if (time.minutes () != 0)
    {
      return time.minutes () + "m " + time.seconds () + "s" + " ago";
    }
    else if (time.seconds () != 0)
    {
      return time.seconds () + "s" + " ago";
    }
    else if (time.seconds () == 0)
    {
      return "less than 1s" + " ago";
    }
    return "N/A";
  }

  public convertNanoToSeconds(time){
    if(time!=0){
      let value = time/1000000;
      let tempNum =  (value).toFixed (2);
      return tempNum.toLocaleString() + ' s';
    }
    else return 0.0 + ' s';
  }

  public insertSpaces (string): string
  {
    string = string.replace (/([a-z]+)([A-Z])/g, '$1 $2');
    // string = string.replace(/([A-Z])([A-Z][a-z])/g, '$1 $2');
    string = string.replace (/([.])([a-z]+)/g, ' $2');
    string = string.replace (/(jsp)/g, '');
    string = string.replace (/(:com.*)/g, '');
    string = string.replace (/([a-z])(type)/g, '$1 $2');
    string = string.replace (/([a-z])(activities)/g, '$1 $2');
    string = string.replace (/([a-z])(bytes)/g, '$1 $2');
    string = string.replace (/(output)([a-z])/g, '$1 $2');
    return string;
  }

 getAllRegisteredIonsList(){
     try{
         let iconMap = this.matIconRegistry['_svgIconConfigs'];
         let keys = iconMap.keys();
         let iconsRegistered = [];
         for( let index =0; index < iconMap.size; index++){
            let value = keys.next().value.substring(1);
            iconsRegistered.push(value);
         }
         return iconsRegistered;
       }
       catch(err){
         console.log("Problem on getting the registered icon list", err);
       }
     }

  urlForLogData (serviceName, queryId, logChoice): string
  {
    let urlBuild = this.configurationHandler.CONFIG[queryId];
    let logUrl = "";
    for (let params in urlBuild)
    {
      if (this.compareStrings (params, logChoice))
      {
        let urlForLog = urlBuild[logChoice];
        logUrl = urlForLog[0] + serviceName + urlForLog[1];
        // console.log(logUrl);
      }
    }
    return logUrl;
  }

  public urlForJKool (serviceName, queryId): string
  {
    /*      let streamToken = this.configurationHandler.CONFIG["aOuthTokens"][serviceName];
          let serviceUrl = "";
          let urlBuild = this.configurationHandler.CONFIG["JKoolCallConfig"][queryId];
          for(let params in urlBuild){
            if(this.compareStrings(params, "param_token")){
              serviceUrl = serviceUrl + urlBuild[params] + streamToken;
            }
            else{
             serviceUrl = serviceUrl + urlBuild[params];
            }
          }*/
    let serviceUrl = "";

    let urlBuild = this.configurationHandler.CONFIG["EndpointJKoolData"];
    for (let params in urlBuild)
    {
      if (this.compareStrings (params, queryId))
      {
        let urlForJKoolData = urlBuild[queryId];
        serviceUrl = urlForJKoolData[0] + serviceName + urlForJKoolData[1];
      }
    }
    //  console.log("The jKool URl address: ", serviceUrl);
    return serviceUrl;
  }

  public getNodePathEnd (string): string {
    try{
       if(!this.compareStrings(string, 'undefined')){
         let regex = /([^\/]*)$/;
         string = string.match(regex);
         return string[0];
       }else{
         return ""
       }
    }
    catch(err){
      console.log(" No node was provided to the method", err);
    }
  }

   public breadcrumbTitle (stringParam): string {

       let string = stringParam.replace(this.configurationHandler.CONFIG["BasePathHide"]+'/','');
       return string;
   }

  public formatData (dataForFormatting, dataValue)
     {
       let confParameters = this.configurationHandler.CONFIG["StreamsDataFormatting"];
       //console.log(confParameters[dataForFormatting]);
       if (!this.compareStrings (confParameters, "undefined"))
       {
         if (this.compareStrings (dataValue, "N/A"))
         {
           return dataValue;
         }
         else if (this.compareStrings (dataValue, "pong"))
         {
           return true;
         }
         else if (this.compareStrings (dataValue, "true") || this.compareStrings (dataValue, "false"))
         {
           return dataValue;
         }
         else if (this.compareStrings (confParameters[dataForFormatting], "time"))
         {
           if (dataValue == 0)
           {
             return "N/A";
           }
           else if ((
                      dataValue % 1000) === 0)
           {dataValue = dataValue / 1000;}
           return this.convertDateToAge (dataValue);
         }
         else if (this.compareStrings (confParameters[dataForFormatting], "timeWords"))
         {
           if (dataValue == 0)
           {
             return "N/A";
           }
           else if ((
                      dataValue % 1000) === 0)
           {dataValue = dataValue / 1000;}
           return this.convertDateToAge (new Date (dataValue).valueOf ());
         }
         else if (this.compareStrings (dataValue, true))
         {
           return true;
         }
         else if (this.compareStrings (+dataValue, "NaN"))
         {
           return dataValue;
         }
         else if (parseFloat (dataValue) < 1000 && (
           dataValue % 1) != 0)
         {
           return parseFloat (dataValue).toFixed (2);
         }
         else if (parseFloat (dataValue) < 1000 && (
           dataValue % 1) == 0)
         {
           return parseFloat (dataValue).toFixed (0);
         }
         else if (parseFloat (dataValue) > 1000)
         {
           dataValue = parseFloat (dataValue).toFixed (2);
           return parseFloat (dataValue).toLocaleString ();
         }
         else
         {
           console.log ("the data formating for the " + dataForFormatting + " " + dataValue
                        + " was not found. You need to add it to config file StreamsDataFormatting.");
           return false;
         }
       }
       else{
          return dataValue;
       }
     }


public formatIncompleteBlocksData (dataForFormatting, dataValue)
  {
      if (this.compareStrings (dataValue, "N/A"))
      {
        return dataValue;
      }
      else if (this.compareStrings (dataValue, "pong"))
      {
        return true;
      }
      else if (this.compareStrings (dataValue, "true") || this.compareStrings (dataValue, "false"))
      {
        return dataValue;
      }
      else if (this.compareStrings (dataForFormatting, "StartTime"))
      {
        if (dataValue == 0)
        {
          return "N/A";
        }
        else if ((
                   dataValue % 1000) === 0)
        {dataValue = dataValue / 1000;}
        return this.convertDateToAge (dataValue);
      }

      else if (this.compareStrings (dataValue, true))
      {
        return true;
      }
      else if (this.compareStrings (+dataValue, "NaN"))
      {
        return dataValue;
      }
      else if (parseFloat (dataValue) < 1000 && (
        dataValue % 1) != 0)
      {
        return parseFloat (dataValue).toFixed (2);
      }
      else if (parseFloat (dataValue) < 1000 && (
        dataValue % 1) == 0)
      {
        return parseFloat (dataValue).toFixed (0);
      }
      else if (parseFloat (dataValue) > 1000)
      {
        dataValue = parseFloat (dataValue).toFixed (2);
        return parseFloat (dataValue).toLocaleString ();
      }
  }
}
