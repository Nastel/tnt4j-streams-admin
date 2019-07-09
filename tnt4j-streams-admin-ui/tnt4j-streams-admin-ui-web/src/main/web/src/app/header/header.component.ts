import { Component, OnInit } from '@angular/core';
import { UtilsService } from "../utils/utils.service";

import { ConfigurationHandler } from '../config/configuration-handler';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  constructor(
          private configurationHandler:ConfigurationHandler,
          public utilsSvc: UtilsService) { }

  navbarOpen = false;
  pathToIcon: string;

    toggleNavbar() {
      this.navbarOpen = !this.navbarOpen;
    }

  ngOnInit() {
;

  }

}
