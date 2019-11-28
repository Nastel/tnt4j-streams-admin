import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BottomLogComponent } from './bottom-log.component';

describe('BottomLogComponent', () => {
  let component: BottomLogComponent;
  let fixture: ComponentFixture<BottomLogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BottomLogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BottomLogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
