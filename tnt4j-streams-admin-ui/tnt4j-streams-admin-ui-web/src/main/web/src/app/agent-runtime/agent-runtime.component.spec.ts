import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AgentRuntimeComponent } from './agent-runtime.component';

describe('AgentRuntimeComponent', () => {
  let component: AgentRuntimeComponent;
  let fixture: ComponentFixture<AgentRuntimeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AgentRuntimeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AgentRuntimeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
