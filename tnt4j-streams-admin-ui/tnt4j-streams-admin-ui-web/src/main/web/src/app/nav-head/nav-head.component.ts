import { Component, OnInit } from '@angular/core';
import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { MatIconRegistry } from "@angular/material";
import { DomSanitizer } from "@angular/platform-browser";
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-nav-head',
  templateUrl: './nav-head.component.html',
  styleUrls: ['./nav-head.component.scss']
})
export class NavHeadComponent implements OnInit {


  constructor( private configurationHandler:ConfigurationHandler,
                private utilsSvc: UtilsService,
                private matIconRegistry: MatIconRegistry,
                private router: Router,
                private domSanitizer: DomSanitizer) { }

  streamsNamesFull = this.configurationHandler.serviceNameDiffRepoOrStream;

  navbarOpen = false;
  serviceName = "";
  existingLinksToStreams = this.configurationHandler.serviceNameDiffRepoOrStream;

  toggleNavbar() {
  this.navbarOpen = !this.navbarOpen;
  }

  ngOnInit() {
    this.serviceName = this.router.url.substring(1);
   // console.log(this.serviceName);
  }
}
