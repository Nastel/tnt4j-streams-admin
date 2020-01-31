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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {TreeViewComponent} from './tree-view.component';

describe ('TreeViewComponent', () => {
  let component: TreeViewComponent;
  let fixture: ComponentFixture<TreeViewComponent>;

  beforeEach (async (() => {
    TestBed.configureTestingModule ({
                                      declarations: [TreeViewComponent]
                                    })
      .compileComponents ();
  }));

  beforeEach (() => {
    fixture = TestBed.createComponent (TreeViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges ();
  });

  it ('should create', () => {
    expect (component).toBeTruthy ();
  });
});
