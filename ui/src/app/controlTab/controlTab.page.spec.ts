import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ControlTabPage } from './controlTab.page';

describe('ControlTabPage', () => {
  let component: ControlTabPage;
  let fixture: ComponentFixture<ControlTabPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ControlTabPage],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ControlTabPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
