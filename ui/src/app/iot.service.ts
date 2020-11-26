import {Injectable} from '@angular/core';

import {forkJoin, Observable, throwError} from 'rxjs';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {catchError, map} from 'rxjs/operators';
import {Control} from './dataModels/Control';
import {DatePipe} from '@angular/common';
import {Schedule} from './dataModels/Schedule';
import {AutomationGroup} from './dataModels/AutomationGroup';
import {ActionType, Automation, EventType} from './dataModels/Automation';
import {Event} from './dataModels/Event';
import {Action} from './dataModels/Action';
import {ControlAction} from './dataModels/ControlAction';
import {AutomationGroupAction} from './dataModels/AutomationGroupAction';
import {ScheduleAction} from './dataModels/ScheduleAction';
import {ControlGroup} from './dataModels/ControlGroup';
import {TypedJSON} from 'typedjson';
import {Constructor} from 'typedjson/js/typedjson/types';
import {Metric} from './dataModels/Metric';
import {MetricData} from './dataModels/MetricData';

const apiIotUrl = 'https://morrissey.io/api/iot';
const controlApiUrl = apiIotUrl + '/controls';
const automationGroupApiUrl = apiIotUrl + '/automation-groups';
const automationApiUrl = apiIotUrl + '/automations';
const controlGroupApiUrl = apiIotUrl + '/control-groups';
const scheduleApiUrl = apiIotUrl + '/schedules';
const controlActionApiUrl = apiIotUrl + '/control-actions';
const automationGroupActionApiUrl = apiIotUrl + '/automation-group-actions';
const scheduleActionApiUrl = apiIotUrl + '/schedule-actions';
const metricApiUrl = apiIotUrl + '/metrics'

@Injectable({
    providedIn: 'root'
})
export class IotService {

    constructor(private http: HttpClient, private datePipe: DatePipe) {
    }

    private httpOptions = {
        headers: new HttpHeaders({
            'Content-Type': 'application/json'
        })
    };

    private static handleError(error: HttpErrorResponse) {
        console.log('Received error:');
        console.log(error);
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

    private static extractData(res: Response) {
        console.log('extracting data from response:');
        console.log(res);
        return res || {};
    }

    getControls(): Observable<Control[]> {
        return this.getTypedResources(controlApiUrl, Control);
    }

    getControlGroups(): Observable<ControlGroup[]> {
        return this.getTypedResources(controlGroupApiUrl, ControlGroup);
    }

    getAutomationGroups(): Observable<AutomationGroup[]> {
        return this.getTypedResources(automationGroupApiUrl, AutomationGroup);
    }

    getControlActions(): Observable<ControlAction[]> {
        return this.getTypedResources(controlActionApiUrl, ControlAction);
    }

    getAutomationGroupActions(): Observable<AutomationGroupAction[]> {
        return this.getTypedResources(automationGroupActionApiUrl, AutomationGroupAction);
    }

    getScheduleActions(): Observable<ScheduleAction[]> {
        return this.getTypedResources(scheduleActionApiUrl, ScheduleAction);
    }

    getMetrics(): Observable<Metric[]> {
        return this.getTypedResources(metricApiUrl, Metric)
    }

    getActions(): Observable<Action[]> {
        const actionObservables = [
            this.getControlActions(),
            this.getAutomationGroupActions(),
            this.getScheduleActions()
        ];
        return forkJoin(actionObservables).pipe(map((value, index) => {
            console.log('returning actions array');
            console.log(value);
            return [].concat(...value);
        }));
    }

    getEvents(): Observable<Event[]> {
        const eventObservables = [
            this.getTypedResources(scheduleApiUrl, Schedule)
        ];
        return forkJoin(eventObservables).pipe(map((value, index) => {
            console.log('returning events array');
            console.log(value);
            return [].concat(...value);
        }));
    }

    private getTypedResources<T>(url: string, type: Constructor<T>): Observable<T[]> {
        return this.getResources(url).pipe(map((value, index) => {
            const deserializer = new TypedJSON(type);
            return deserializer.parseAsArray(value);
        }));
    }

    private getResources(url: string): Observable<any> {
        console.log('Retrieving resource at url: ' + url);
        return this.http.get(url).pipe(
            map(IotService.extractData),
            catchError(IotService.handleError));

    }

    getMetricData(metric): Observable<MetricData> {
        const metricDataApiUrl = metricApiUrl + '/' + metric.id + '/data'
        return this.getResources(metricDataApiUrl).pipe(map((value, index) => {
            const deserializer = new TypedJSON(MetricData);
            return deserializer.parse(value);
        }));
    }

    updateControl(control: Control) {
        return this.updateResource(controlApiUrl, control.id, control, Control);
    }

    updateSchedule(schedule: Schedule) {
        return this.updateResource(scheduleApiUrl, schedule.id, schedule, Schedule);
    }

    updateAutomationGroup(automationGroup: AutomationGroup) {
        return this.updateResource(automationGroupApiUrl, automationGroup.id, automationGroup, AutomationGroup);
    }

    createAutomation(scheduledAutomation: Automation): Observable<any> {
        return this.createResource(automationApiUrl, scheduledAutomation);
    }

    updateAutomation(automation: Automation) {
        return this.updateResource(automationApiUrl, automation.id, automation, Automation);
    }

    deleteAutomation(scheduledAutomation: Automation) {
        this.deleteResource(automationApiUrl, scheduledAutomation.id);
    }

    updateControlAction(controlAction: ControlAction) {
        return this.updateResource(controlActionApiUrl, controlAction.id, controlAction, ControlAction);
    }

    updateAutomationGroupAction(automationGroupAction: AutomationGroupAction) {
        return this.updateResource(automationGroupActionApiUrl, automationGroupAction.id, automationGroupAction, AutomationGroupAction);
    }

    updateScheduleAction(scheduleAction: ScheduleAction) {
        return this.updateResource(scheduleActionApiUrl, scheduleAction.id, scheduleAction, ScheduleAction);
    }


    updateEvent(event: Event) {
        switch (event.type) {
            case EventType.SCHEDULE:
                this.updateSchedule(event as Schedule);
                break;
        }
    }

    updateAction(action: Action) {
        switch (action.type) {
            case ActionType.CONTROL:
                this.updateControlAction(action as ControlAction);
                break;
            case ActionType.AUTOMATION_GROUP:
                this.updateAutomationGroupAction(action as AutomationGroupAction);
                break;
            case ActionType.SCHEDULE:
                this.updateScheduleAction(action as ScheduleAction);
                break;
        }
    }

    private createResource(apiUrl: string, resource: any): Observable<any> {
        console.log('Creating resource value %O', resource);
        const url = `${apiUrl}`;
        console.log('Sending request to api ' + url);
        return this.http.post(url, resource, this.httpOptions)
            .pipe(
                map(IotService.extractData),
                catchError(IotService.handleError)
            );
    }

    private updateResource<T>(apiUrl: string, id: number, resource: T, type: Constructor<T>) {
        console.log('Updating resource value to %O', resource);
        const serializer = new TypedJSON(type);
        const value = serializer.stringify(resource);
        const url = `${apiUrl}/${id}`;
        console.log('Sending request to api ' + url);
        console.log('value is:');
        console.log(value);
        this.http.put(url, value, this.httpOptions)
            .pipe(
                map(IotService.extractData),
                catchError(IotService.handleError)
            ).subscribe(
            response => console.log(response),
            err => console.log(err)
        );
    }

    private deleteResource(apiUrl: string, id: number) {
        console.log('deleting resource with id ' + id);
        const url = `${apiUrl}/${id}`;
        console.log('Sending request to api ' + url);
        this.http.delete(url)
            .pipe(
                map(IotService.extractData),
                catchError(IotService.handleError)
            ).subscribe(
            response => console.log(response),
            err => console.log(err)
        );
    }
}
