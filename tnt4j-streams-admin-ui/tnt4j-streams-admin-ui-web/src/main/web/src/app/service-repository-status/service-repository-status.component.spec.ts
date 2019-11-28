import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceRepositoryStatusComponent } from './service-repository-status.component';

describe('ServiceRepositoryStatusComponent', () => {
  let component: ServiceRepositoryStatusComponent;
  let fixture: ComponentFixture<ServiceRepositoryStatusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ServiceRepositoryStatusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceRepositoryStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
