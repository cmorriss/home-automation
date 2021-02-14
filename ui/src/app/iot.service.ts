import {Injectable} from '@angular/core';

import {forkJoin, Observable, throwError} from 'rxjs';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {catchError, map} from 'rxjs/operators';
import {Control} from './dataModels/Control';
import {DatePipe} from '@angular/common';
import {AutomationGroup} from './dataModels/AutomationGroup';
import {ActionType, Automation} from './dataModels/Automation';
import {Event} from './dataModels/Event';
import {Action} from './dataModels/Action';
import {ControlAction} from './dataModels/ControlAction';
import {AutomationGroupAction} from './dataModels/AutomationGroupAction';
import {AutomationAction} from './dataModels/AutomationAction';
import {ControlGroup} from './dataModels/ControlGroup';
import {TypedJSON} from 'typedjson';
import {Constructor} from 'typedjson/js/typedjson/types';
import {Metric} from './dataModels/Metric';
import {MetricData} from './dataModels/MetricData';
import {Threshold} from './dataModels/Threshold';

const apiIotUrl = 'https://morrissey.io/api/iot';
const controlApiUrl = apiIotUrl + '/controls';
const automationGroupApiUrl = apiIotUrl + '/automation-groups';
const automationApiUrl = apiIotUrl + '/automations';
const controlGroupApiUrl = apiIotUrl + '/control-groups';
const thresholdApiUrl = apiIotUrl + '/thresholds';
const controlActionApiUrl = apiIotUrl + '/control-actions';
const automationGroupActionApiUrl = apiIotUrl + '/automation-group-actions';
const automationActionApiUrl = apiIotUrl + '/automation-actions';
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

    getScheduleActions(): Observable<AutomationAction[]> {
        return this.getTypedResources(automationActionApiUrl, AutomationAction);
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
            this.getTypedResources(thresholdApiUrl, Threshold)
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

    getMetricData(metric, duration): Observable<MetricData> {
        const metricDataApiUrl = metricApiUrl + '/' + metric.id + '/data?duration=' + duration;
        return this.getResources(metricDataApiUrl).pipe(map((value, index) => {
            const deserializer = new TypedJSON(MetricData);
            return deserializer.parse(value);
        }));
    }

    updateControl(control: Control): Observable<any> {
        return this.updateResource(controlApiUrl, control.id, control, Control);
    }

    updateControlGroup(controlGroup: ControlGroup): Observable<any> {
        return this.updateResource(controlGroupApiUrl, controlGroup.id, controlGroup, ControlGroup);
    }

    updateAutomationGroup(automationGroup: AutomationGroup): Observable<any> {
        return this.updateResource(automationGroupApiUrl, automationGroup.id, automationGroup, AutomationGroup);
    }

    updateAutomation(automation: Automation): Observable<any> {
        return this.updateResource(automationApiUrl, automation.id, automation, Automation);
    }

    updateControlAction(controlAction: ControlAction): Observable<any> {
        return this.updateResource(controlActionApiUrl, controlAction.id, controlAction, ControlAction);
    }

    updateAutomationGroupAction(automationGroupAction: AutomationGroupAction): Observable<any> {
        return this.updateResource(automationGroupActionApiUrl, automationGroupAction.id, automationGroupAction, AutomationGroupAction);
    }

    updateScheduleAction(automationAction: AutomationAction): Observable<any> {
        return this.updateResource(automationActionApiUrl, automationAction.id, automationAction, AutomationAction);
    }

    updateEvent(event: Event) {
        // TODO: Once thresholds are supported, implement this.
    }

    createAutomation(scheduledAutomation: Automation): Observable<any> {
        return this.createResource(automationApiUrl, scheduledAutomation);
    }

    createControlGroup(controlGroup: ControlGroup): Observable<any> {
        return this.createResource(controlGroupApiUrl, controlGroup);
    }

    deleteControlGroup(controlGroup: ControlGroup) {
        return this.deleteResource(controlGroupApiUrl, controlGroup.id);
    }

    deleteAutomation(scheduledAutomation: Automation) {
        this.deleteResource(automationApiUrl, scheduledAutomation.id);
    }

    updateAction(action: Action) {
        switch (action.type) {
            case ActionType.CONTROL:
                this.updateControlAction(action as ControlAction);
                break;
            case ActionType.AUTOMATION_GROUP:
                this.updateAutomationGroupAction(action as AutomationGroupAction);
                break;
            case ActionType.AUTOMATION:
                this.updateScheduleAction(action as AutomationAction);
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

    private updateResource<T>(apiUrl: string, id: number, resource: T, type: Constructor<T>): Observable<any> {
        console.log('Updating resource value to %O', resource);
        const serializer = new TypedJSON(type);
        const value = serializer.stringify(resource);
        const url = `${apiUrl}/${id}`;
        console.log('Sending request to api ' + url);
        console.log('value is:');
        console.log(value);
        return this.http.put(url, value, this.httpOptions)
            .pipe(
                map(IotService.extractData),
                catchError(IotService.handleError)
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
