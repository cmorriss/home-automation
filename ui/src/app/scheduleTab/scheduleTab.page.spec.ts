import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduleTabPage } from './scheduleTab.page';

describe('ScheduleTabPage', () => {
  let component: ScheduleTabPage;
  let fixture: ComponentFixture<ScheduleTabPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ScheduleTabPage],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ScheduleTabPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
