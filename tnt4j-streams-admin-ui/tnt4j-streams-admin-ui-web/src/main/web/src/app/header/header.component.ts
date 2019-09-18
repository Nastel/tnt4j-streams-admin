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
