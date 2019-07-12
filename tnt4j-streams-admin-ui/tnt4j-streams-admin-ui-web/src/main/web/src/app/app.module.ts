import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { NgModule, Compiler, COMPILER_OPTIONS, CompilerFactory, APP_INITIALIZER } from '@angular/core';
import { AppComponent } from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {
  MatProgressSpinnerModule,
  MatSidenavModule,
  MatNativeDateModule,
  MatListModule,
  MatTreeModule,
  MatRadioModule,
  MatTableModule,
  MatPaginatorModule,
  MatFormFieldModule,
  MatInputModule,
  MatIconModule,
  MatSortModule,
  MatTooltipModule,
  MatDialogModule,
  MatButtonModule} from '@angular/material'
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { JitCompilerFactory, platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { HashLocationStrategy, LocationStrategy } from '@angular/common';
import {CdkTreeModule} from '@angular/cdk/tree';
import { HighlightSearch } from './utils/highlight.search';


  export function createCompiler(compilerFactory: CompilerFactory) {
    return compilerFactory.createCompiler();
  }
  export function initializeApp(appConfig: ConfigurationHandler) {
    return () => appConfig.getConfig();
  }
    export function initializeLinks(appConfig: AutoRouteGenerator) {
      return () => appConfig.getLinks();
    }



import { AppRoutingModule } from './app-routing.module';
import { ConfigurationHandler } from './config/configuration-handler';
import { AutoRouteGenerator } from './config/auto-route-generator';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';

import { IncompleteBlocksComponent } from './incomplete-blocks/incomplete-blocks.component';
import { NavigationTreeComponent } from './navigation-tree/navigation-tree.component';
import { TreeViewComponent } from './tree-view/tree-view.component';
import { ThreadDumpComponent } from './thread-dump/thread-dump.component';

import { DialogOverviewExampleDialog } from './thread-dump/thread-dump.component';


import { ServerConfigurationFileComponent } from './server-configuration-file/server-configuration-file.component';
import { AgentLogsComponent } from './agent-logs/agent-logs.component';
import { AgentRuntimeComponent } from './agent-runtime/agent-runtime.component';
import { ServiceDataComponent } from './service-data/service-data.component';
import { ServiceRepositoryStatusComponent } from './service-repository-status/service-repository-status.component';
import { ServiceMetricsComponent } from './service-metrics/service-metrics.component';
import { BottomLogComponent } from './bottom-log/bottom-log.component';
import { DownloadsComponent } from './downloads/downloads.component';
import { ConfigurableComponentNodeComponent } from './configurable-component-node/configurable-component-node.component';

@NgModule({
  declarations: [
    AppComponent,
    HighlightSearch,
    HeaderComponent,
    FooterComponent,
    IncompleteBlocksComponent,
    DialogOverviewExampleDialog,
    NavigationTreeComponent,
    TreeViewComponent,
    ThreadDumpComponent,
    ServerConfigurationFileComponent,
    AgentLogsComponent,
    AgentRuntimeComponent,
    ServiceDataComponent,
    ServiceRepositoryStatusComponent,
    ServiceMetricsComponent,
    BottomLogComponent,
    DownloadsComponent,
    ConfigurableComponentNodeComponent
  ],
  entryComponents: [
    TreeViewComponent,
    DialogOverviewExampleDialog
  ],
  imports: [
  CdkTreeModule,
    FormsModule,
    MatButtonModule,
    MatSortModule,
    MatTreeModule,
    MatListModule,
    MatTableModule,
    MatPaginatorModule,
    BrowserModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatNativeDateModule,
    MatTooltipModule,
    MatSidenavModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule, MatRadioModule,
    MatIconModule,
    BrowserAnimationsModule,
    MatFormFieldModule,
    MatInputModule
  ],
  providers: [
  ConfigurationHandler,
    { provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [ConfigurationHandler],
      multi: true
    },
  AutoRouteGenerator,
    { provide: APP_INITIALIZER,
      useFactory: initializeLinks,
      deps: [AutoRouteGenerator],
      multi: true
    },
  { provide: COMPILER_OPTIONS, useValue: {}, multi: true },
  { provide: CompilerFactory, useClass: JitCompilerFactory, deps: [COMPILER_OPTIONS] },
  { provide: Compiler, useFactory: createCompiler, deps: [CompilerFactory] },
 {provide: LocationStrategy, useClass: HashLocationStrategy},

  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
