import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigurableComponentNodeComponent } from './configurable-component-node.component';

describe('ConfigurableComponentNodeComponent', () => {
  let component: ConfigurableComponentNodeComponent;
  let fixture: ComponentFixture<ConfigurableComponentNodeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigurableComponentNodeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigurableComponentNodeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
