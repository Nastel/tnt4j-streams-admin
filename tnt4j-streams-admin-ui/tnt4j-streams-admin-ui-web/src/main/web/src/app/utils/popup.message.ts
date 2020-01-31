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
