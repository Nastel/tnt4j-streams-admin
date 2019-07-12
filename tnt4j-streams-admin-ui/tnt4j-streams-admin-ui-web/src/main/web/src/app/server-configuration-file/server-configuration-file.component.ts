import { Component, OnInit, ViewChild, ElementRef  } from '@angular/core';
import { Router } from '@angular/router';

import { ConfigurationHandler } from '../config/configuration-handler';
import { UtilsService } from "../utils/utils.service";
import { DataService } from '../data.service';
import { incompleteBlocks }  from '../incomplete-blocks/incomplete-blocks.component';

@Component({
  selector: 'app-server-configuration-file',
  templateUrl: './server-configuration-file.component.html',
  styleUrls: ['./server-configuration-file.component.scss']
})
export class ServerConfigurationFileComponent implements OnInit {

  /** Url address */
  pathToData : string;

  /** Service configuration data */
  serviceConfigParent : string;
  zooKeeperData: Object;
  objectKeys = Object.keys;
  configChoiceData= "";

  /** Values for showing data loading properties */
  valueThatChangesOnDataLoad = false;
  valueThatChangesForSpinnerOnResponse = false;

  constructor(  private data: DataService,
                private router: Router,
                private configurationHandler:ConfigurationHandler,
                public utilsSvc: UtilsService,
                public incBlocks: incompleteBlocks) { }

  ngOnInit() {
    this.pathToData = this.router.url.substring(1);
    this.loadZooKeeperNodeData(this.pathToData);
  }

   loadZooKeeperNodeData(pathToData){
      this.valueThatChangesOnDataLoad = false;
      this.data.getZooKeeperNodeData(pathToData).subscribe( data => {
        try{
          this.zooKeeperData = data;
          this.serviceConfigParent =  JSON.parse(this.zooKeeperData.toString());
          console.log("CONFIGURATIONS DATA", this.serviceConfigParent);
          this.serviceConfigParent = this.serviceConfigParent['data'];
          this.configChoiceData =  this.serviceConfigParent[0]['name'];
          this.valueThatChangesOnDataLoad = true;
        }catch(err){
          console.log("Problem on getting server configurations ", err);
        }
      },
       err =>{
         this.valueThatChangesForSpinnerOnResponse = true;
         this.valueThatChangesOnDataLoad = false;
         console.log("Service Config data was not loaded correctly: ", err);
       }
      );
   }

   configFileChoice(choice){
     this.configChoiceData = choice;
     this.scrollToTop();
   }

   @ViewChild('configuration')private configFiles: ElementRef;

   scrollToTop(): void {
      try {
          this.configFiles.nativeElement.scrollTop = 0;
      } catch(err) {
          console.log("Problem while scrolling to bottom of log" , err);
       }
   }

}