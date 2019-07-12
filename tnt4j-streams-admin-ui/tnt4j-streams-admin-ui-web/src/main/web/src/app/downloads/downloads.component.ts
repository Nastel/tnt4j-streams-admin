import { Component, OnInit,  ViewChild} from '@angular/core';
import { Router } from '@angular/router';

import { MatIconRegistry } from "@angular/material";
import { MatPaginator , MatTableDataSource, MatSort } from '@angular/material';
import { DomSanitizer } from "@angular/platform-browser";
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';

import { Zlib } from 'zlibt';


@Component({
  selector: 'app-downloads',
  templateUrl: './downloads.component.html',
  styleUrls: ['./downloads.component.scss']
})
export class DownloadsComponent implements OnInit {

 @ViewChild('downloadsPaging') downloadsPaging: MatPaginator;
 @ViewChild('downloadsSorting') downloadsSorting: MatSort;

  dataSource = new MatTableDataSource<any>();

  /** Values for showing data loading properties */
  valueThatChangesOnDataLoad = false;
  valueThatChangesForSpinnerOnResponse = false;

  /** Data for downloads nav*/
  navNamesArray = [];
  navigationChoice = "";

  /** Data for downloads table*/
  fullDownloadableData = [];
  navDataTable = [];
  displayedColumns = ['name', 'value'];
  tableData = [];

  /** Full choice data */
  fileZipData = [];

  /** For URL address calls */
  pathToData = "";

  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService,
                private matIconRegistry: MatIconRegistry,
                private domSanitizer: DomSanitizer) { }

  ngOnInit() {




    this.pathToData = this.router.url.substring(1);
    this.loadZooKeeperNodeData();

  }

  loadZooKeeperNodeData(){
    this.tableData = [];
    this.responseShow("");
    this.data.getZooKeeperNodeData(this.pathToData).subscribe( data => {
      try{
        let result = data;
        result =  JSON.parse(result.toString());
        console.log("DOWNLOADS DATA", result);
        this.navNamesArray = Object.keys(result["data"]);
        this.fullDownloadableData = result["data"] ;
        if(this.utilsSvc.compareStrings(this.navigationChoice, "")){
          this.navigationChoice = this.navNamesArray[0];
        }
        if(!this.utilsSvc.compareStrings(this.fullDownloadableData [this.navigationChoice], "undefined")){
          result = this.fullDownloadableData[this.navigationChoice];
          this.loadDownloadsTable(result);
        }
        this.responseShow("good");
      }
      catch{
          this.responseShow("bad");
          console.log("Problem on reading downloads data from : ", this.pathToData);
        }
    },
     err =>{
       this.responseShow("bad");
       console.log("Problem on reading downloads data from : ", this.pathToData);
     }
    );
  }


  downloadsChoice(choice){
     try {
       this.navigationChoice = choice;
       this.tableData = [];
       this.loadDownloadsTable( this.fullDownloadableData[choice]);
     } catch(err) {
         console.log("Problem while changing log choice and reloading data" , err);
      }
  }

  loadDownloadsTable(chosenData){
    let button = "";
    for(let item of chosenData){
      let tempServiceData = [];
      tempServiceData["name"] = item;
      tempServiceData["value"] = button;
      this.tableData.push(tempServiceData);
    }
    this.dataSource = new MatTableDataSource( this.tableData);
    setTimeout(() => this.dataSource.paginator = this.downloadsPaging);
    setTimeout(() => this.dataSource.sort = this.downloadsSorting);
  }

/* Filtering the material table data by input field */
  applyFilter(filterValue: string) {
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }


/*Response variables set to good, bad or else for showing the data loading state*/
   responseShow(responseData){
    if(this.utilsSvc.compareStrings(responseData, "good")){
      this.valueThatChangesForSpinnerOnResponse = false;
      this.valueThatChangesOnDataLoad = true;
    }
    else if(this.utilsSvc.compareStrings(responseData, "bad")){
      this.valueThatChangesForSpinnerOnResponse = false;
      this.valueThatChangesOnDataLoad = false;
    }
    else{ //still loading
      this.valueThatChangesOnDataLoad = false;
      this.valueThatChangesForSpinnerOnResponse = true;
    }
   }

  download(fileName){
    let tempPath = this.pathToData + '/' + fileName;
    console.log(fileName)
    console.log("Sending request to :", tempPath);
      this.data.getZooKeeperNodeData(tempPath).subscribe( data => {
        console.log("Response to Request", data)
        let fileName;
        let encodedFile;
        let result = data;
        this.fileZipData = JSON.parse(result.toString());
        console.log(this.fileZipData)
        if(!this.utilsSvc.compareStrings(this.fileZipData, "undefined")){
          if( this.fileZipData['raw'].includes("Error")){
              console.log("File download failed : File size to big")
          }
          else{
            let dataRaw = this.fileZipData["raw"];
            if(!this.utilsSvc.compareStrings(dataRaw, "undefined")){
              var bytes = this._base64ToArrayBuffer(dataRaw);
            }
            var blob=new Blob([bytes], {type: "application/stream"});
            console.log(" THE VALUE AFTER BLOB ", blob);



            var link=document.createElement('a');
            link.href=window.URL.createObjectURL(blob);
            console.log( link.href);
            document.body.appendChild(link);
            link.download=this.fileZipData["filename"]+".zip";
               console.log(  link.download)
            link.click();
          }
        }

      },
      err =>{
        this.responseShow("bad");
        console.log("Problem on reading downloads data from : ", this.pathToData);
      });
//    }
  }

   downloadModalWindowMessage(){

   }

   _base64ToArrayBuffer(base64) {
      let binary_string =  window.atob(base64);
      let len = binary_string.length;
      let bytes = new Uint8Array( len );
      for (let i = 0; i < len; i++)        {
          bytes[i] = binary_string.charCodeAt(i);
      }
      return bytes.buffer;
}


}