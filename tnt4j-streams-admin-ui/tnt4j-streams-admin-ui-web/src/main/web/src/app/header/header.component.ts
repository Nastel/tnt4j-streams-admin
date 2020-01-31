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

import { Component, OnInit } from '@angular/core';
import { UtilsService } from "../utils/utils.service";
import { Router, RouterModule} from '@angular/router';
import { ConfigurationHandler } from '../config/configuration-handler';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  basePath: string;

  constructor(
          private configurationHandler:ConfigurationHandler,
          private router: Router,
          public utilsSvc: UtilsService) { }

  navbarOpen = false;
  pathToIcon: string;

  ngOnInit() {
     this.basePath = this.configurationHandler.CONFIG["BasePathHide"]+'clusters';
  }

  toggleNavbar() {
    this.navbarOpen = !this.navbarOpen;
  }

  logout(){
    let tokenName = this.configurationHandler.CONFIG["sessionTokenName"];
    sessionStorage.removeItem(tokenName);
  }

}
