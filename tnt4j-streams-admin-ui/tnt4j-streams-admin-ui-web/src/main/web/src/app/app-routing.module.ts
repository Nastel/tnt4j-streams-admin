import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import {TreeViewComponent} from './tree-view/tree-view.component';

const routes: Routes = [
  { path: 'streams/v1/clusters', component: TreeViewComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {onSameUrlNavigation: 'reload'})],
  exports: [RouterModule]
})
export class AppRoutingModule {



}
