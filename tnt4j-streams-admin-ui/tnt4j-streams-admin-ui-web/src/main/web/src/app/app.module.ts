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

import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { NgModule, Compiler, COMPILER_OPTIONS, CompilerFactory, APP_INITIALIZER } from '@angular/core';
import { AppComponent } from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import { JitCompilerFactory, platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { HashLocationStrategy, LocationStrategy } from '@angular/common';
import { HighlightSearch } from './utils/highlight.search';
import { BlockUIModule } from 'ng-block-ui';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {
  MatProgressSpinnerModule,
  MatListModule,
  MatTreeModule,
  MatTableModule,
  MatPaginatorModule,
  MatInputModule,
  MatIconModule,
  MatSortModule,
  MatTooltipModule,
  MatDialogModule,
  MatButtonModule,
  MatMenuModule,
  MatCheckboxModule} from '@angular/material'

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
import { popupMessage } from './utils/popup.message';
import { ServerConfigurationFileComponent } from './server-configuration-file/server-configuration-file.component';
import { AgentLogsComponent } from './agent-logs/agent-logs.component';
import { AgentRuntimeComponent } from './agent-runtime/agent-runtime.component';
import { ServiceDataComponent } from './service-data/service-data.component';
import { ServiceRepositoryStatusComponent } from './service-repository-status/service-repository-status.component';
import { ServiceMetricsComponent } from './service-metrics/service-metrics.component';
import { BottomLogComponent } from './bottom-log/bottom-log.component';
import { DownloadsComponent } from './downloads/downloads.component';
import { ConfigurableComponentNodeComponent } from './configurable-component-node/configurable-component-node.component';
import { LoginComponent } from './login/login.component';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { RequestInterceptor } from './utils/request-interceptor';
import { LogoutComponent } from './logout/logout.component';
import { UserControlComponent } from './users-control/users-control.component';
import { RecaptchaModule } from 'ng-recaptcha';
import { GoogleAnalyticsService } from './services/google-analytics.service';

@NgModule({
  declarations: [
    AppComponent,
    HighlightSearch,
    HeaderComponent,
    FooterComponent,
    IncompleteBlocksComponent,
    DialogOverviewExampleDialog,
    popupMessage,
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
    ConfigurableComponentNodeComponent,
    LoginComponent,
    LogoutComponent,
    UserControlComponent
  ],
  entryComponents: [
    TreeViewComponent,
    DialogOverviewExampleDialog,
    popupMessage,
    LoginComponent,
    UserControlComponent
  ],
  imports: [
    RecaptchaModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatMenuModule,
    MatSortModule,
    MatTreeModule,
    MatListModule,
    MatTableModule,
    MatPaginatorModule,
    BrowserModule,
    MatDialogModule,
    MatTooltipModule,
    MatCheckboxModule,
    AppRoutingModule,
    HttpClientModule,
    MatProgressSpinnerModule,
    MatIconModule,
    BrowserAnimationsModule,
    MatInputModule,
    BlockUIModule.forRoot()
  ],
  providers: [
    GoogleAnalyticsService,

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
      {
        provide: HTTP_INTERCEPTORS,
        useClass: RequestInterceptor,
        multi: true
      },
    { provide: COMPILER_OPTIONS, useValue: {}, multi: true },
    { provide: CompilerFactory, useClass: JitCompilerFactory, deps: [COMPILER_OPTIONS] },
    { provide: Compiler, useFactory: createCompiler, deps: [CompilerFactory] },
    { provide: LocationStrategy, useClass: HashLocationStrategy },

  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
