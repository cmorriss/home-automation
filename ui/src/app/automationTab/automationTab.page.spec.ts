import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AutomationTabPage } from './automationTab.page';

describe('ScheduleTabPage', () => {
  let component: AutomationTabPage;
  let fixture: ComponentFixture<AutomationTabPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AutomationTabPage],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AutomationTabPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
