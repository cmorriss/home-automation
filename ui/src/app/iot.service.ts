import {Injectable} from '@angular/core';

import {Observable, throwError} from 'rxjs';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {catchError, map} from 'rxjs/operators';
import {Switch} from './models/Switch';
import {Schedule} from './models/Schedule';
import {ScheduleStatusView} from './models/ScheduleStatusView';
import {ScheduleStatus} from './models/ScheduleStatus';
import {DatePipe} from '@angular/common';

const apiUrl = '/api/iot';
const switchApiUrl = apiUrl + '/switches';
const scheduleApiUrl = apiUrl + '/schedules';
const scheduleStatusUrl = apiUrl + '/schedule-status';

@Injectable({
    providedIn: 'root'
})
export class IotService {

    constructor(private http: HttpClient, private datePipe: DatePipe) {
    }

    private handleError(error: HttpErrorResponse) {
        console.log('Received error: %O', error);
        if (error.error instanceof ErrorEvent) {
            // A client-side or network error occurred. Handle it accordingly.
            console.error('An error occurred:', error.error.message);
        } else {
            // The backend returned an unsuccessful response code.
            // The response body may contain clues as to what went wrong,
            console.error(
                `Backend returned code ${error.status}, ` +
                `body was: ${error.error}`);
        }
        // return an observable with a user-facing error message
        return throwError('Something bad happened; please try again later.');
    }

    private extractData(res: Response) {
        console.log('extracting data from response ' + res);
        let body = res;
        return body || {};
    }

    getSwitches(): Observable<any> {
        console.log('Retrieving switches');
        return this.http.get(switchApiUrl).pipe(
            map(this.extractData),
            catchError(this.handleError));
    }

    updateSwitch(aSwitch: Switch): Observable<any> {
        console.log('Updating switch to %O', aSwitch);
        const url = `${switchApiUrl}/${aSwitch.id}`;
        console.log('Sending request to api ' + url);
        return this.http.post(url, aSwitch)
            .pipe(
                map(this.extractData),
                catchError(this.handleError)
            );
    }

    updateSchedule(schedule: Schedule): Observable<any> {
        console.log('Updating schedule value to %O', schedule);
        const url = `${scheduleApiUrl}/${schedule.id}`;
        console.log('Sending request to api ' + url);
        return this.http.post(url, schedule)
            .pipe(
                map(this.extractData),
                catchError(this.handleError)
            );
    }

    updateScheduleStatus(scheduleStatus: ScheduleStatusView): Observable<any> {
        console.log("Updating schedule status");
        let dateString = scheduleStatus.pausedUntilDate.toISOString();
        let scheduleStatusDto = new ScheduleStatus(scheduleStatus.status.value, dateString);
        console.log(scheduleStatusDto);
        console.log('Sending request to api ' + scheduleStatusUrl);
        return this.http.post(scheduleStatusUrl, scheduleStatusDto)
            .pipe(
                map(this.extractData),
                catchError(this.handleError)
            );
    }

    getScheduleStatus(): Observable<any> {
        console.log("getting the schedule status...");
        return this.http.get(scheduleStatusUrl);
    }
}
