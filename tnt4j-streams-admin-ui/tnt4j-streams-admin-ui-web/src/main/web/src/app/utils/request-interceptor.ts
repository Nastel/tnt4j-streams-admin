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

import { Injectable } from '@angular/core';
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "./utils.service";
import {Router} from "@angular/router";
import { ControlUtils } from "../utils/control.utils";
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {catchError} from "rxjs/internal/operators";

@Injectable()
export class RequestInterceptor implements HttpInterceptor {

  constructor(private configurationHandler: ConfigurationHandler,
              public utilsSvc: UtilsService,
              private router: Router,
              private controlUtils : ControlUtils,) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
     let input = sessionStorage.getItem("authToken");
     let basePath = this.configurationHandler.CONFIG["BaseAddress"];
     if(request.url.search(basePath) != -1){
       if(input){
          request = request.clone({
            setHeaders: {
              Authorization: input
            }
          });
       }
     }else{
       if(input){
//         console.log("Request to jkool", request.headers);
         var myHeaders = request.headers.delete('Authorization');
              request = request.clone({headers: myHeaders});
            }
     }
    return next.handle(request).pipe(catchError((error, caught) => {
          console.log(error)
            this.handleAuthError(error);
            return of(error);
          }) as any);
  }


 /**
   * manage errors
   * @param err
   * @returns {any}
   */
  private handleAuthError(err: HttpErrorResponse): Observable<any> {
    let pathToData = this.router.url.substring(1);
    let input = sessionStorage.getItem("username");
    let token = sessionStorage.getItem("authToken");
    if(err.status === 401 && token && input){
      this.controlUtils.openDialogWithHeader(" Login session time with no activity exceeded. please login again to continue your work", "Token expired", pathToData);
      sessionStorage.clear();
      setTimeout(function(){  location.reload(); }, 2000);
    }
    else if (err.status === 401 && input) {
      //navigate /delete cookies or whatever
      sessionStorage.clear()
      this.controlUtils.openDialogWithHeader("Authentication failed", "Wrong credentials", pathToData);
    }
    else if(err.status === 401){
      sessionStorage.clear()
    }
    else {
      this.controlUtils.openDialogWithHeader("Problem occurred while trying to access resources. Please try again later.", "Error "+err.status, pathToData);
      //sessionStorage.clear()
    }
    throw err;
  }


}
