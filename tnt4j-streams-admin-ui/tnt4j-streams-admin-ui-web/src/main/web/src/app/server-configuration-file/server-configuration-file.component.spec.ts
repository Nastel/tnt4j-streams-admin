import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ServerConfigurationFileComponent } from './server-configuration-file.component';

describe('ServerConfigurationFileComponent', () => {
  let component: ServerConfigurationFileComponent;
  let fixture: ComponentFixture<ServerConfigurationFileComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ServerConfigurationFileComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServerConfigurationFileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
