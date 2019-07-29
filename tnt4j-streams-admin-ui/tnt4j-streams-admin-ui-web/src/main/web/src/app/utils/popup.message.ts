import { Component, OnInit, Inject } from '@angular/core';
import { Router } from '@angular/router';

import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "./utils.service";
import { DataService } from '../data.service';

import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';


@Component({
  selector: 'popup-message.ts',
  templateUrl: 'popup-message.html',
})
export class popupMessage {


  /** Url address */
  pathToData : string;
  responseData : string;
  responseHeaderMessage : string;


  /** Values for showing data loading properties */
  valueThatChangesOnDataLoad = false;
  valueThatChangesForSpinnerOnResponse = false;

  constructor(
               public utilsSvc: UtilsService,
               private router: Router,
               public dialogRef: MatDialogRef<popupMessage>,
                @Inject(MAT_DIALOG_DATA) public data: any,
               private dataService: DataService,) {}

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit() {
    this.responseData = this.data.response;
    this.responseHeaderMessage = this.data.header;
    //console.log(this.responseData);
    this.pathToData = this.router.url.substring(1);

  }

}
