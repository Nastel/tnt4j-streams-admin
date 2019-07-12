import { Component, NgModule, OnInit} from '@angular/core';
import { MatIconRegistry } from "@angular/material/icon";
import { DomSanitizer } from "@angular/platform-browser";
import { RouterModule, Routes} from '@angular/router';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent{
  constructor( private route:ActivatedRoute, private router:Router) {
  }

  ngOnInit(){
//    let pathUrl = this.router.url.substring(1);
//
//     setTimeout((console.log(pathUrl), 1000))
//     if(pathUrl===''){
//       this.router.navigate(['/streams/v1/clusters']);
//     }
  }
}
