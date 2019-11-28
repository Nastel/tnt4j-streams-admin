import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IncompleteBlocksComponent } from './incomplete-blocks.component';

describe('IncompleteBlocksComponent', () => {
  let component: IncompleteBlocksComponent;
  let fixture: ComponentFixture<IncompleteBlocksComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IncompleteBlocksComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IncompleteBlocksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
