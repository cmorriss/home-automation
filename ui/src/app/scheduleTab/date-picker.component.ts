import { Component, OnInit } from '@angular/core';
import {Events, NavParams, PopoverController} from '@ionic/angular';

@Component({
  selector: 'app-date-picker',
  templateUrl: './date-picker.component.html',
  styleUrls: ['./date-picker.component.scss'],
})
export class DatePickerComponent implements OnInit {
  public pausedUntilDate: Date;

  constructor(private navParams: NavParams, private events: Events, private popoverController: PopoverController) {
    this.pausedUntilDate = navParams.get('date');
  }

  ngOnInit() {}

  public pausedUntilDateSelected(selectedDate: Date) {
    this.pausedUntilDate = selectedDate;
    this.events.publish('PausedUntilDate', selectedDate);
    this.popoverController.dismiss();
  }

  async DismissClick() {
    await this.popoverController.dismiss();
  }
}
