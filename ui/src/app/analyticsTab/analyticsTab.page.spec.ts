import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AnalyticsTabPage } from './analyticsTab.page';

describe('AnalyticsTabPage', () => {
  let component: AnalyticsTabPage;
  let fixture: ComponentFixture<AnalyticsTabPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AnalyticsTabPage],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AnalyticsTabPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
