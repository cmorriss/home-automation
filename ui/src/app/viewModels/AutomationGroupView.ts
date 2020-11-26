import {AutomationStatusEnum} from './AutomationStatusEnum';
import {AutomationView} from './AutomationView';
import {AutomationGroup} from '../dataModels/AutomationGroup';
import {Automation} from '../dataModels/Automation';
import {ScheduledAutomationView} from './ScheduledAutomationView';
import {Action} from '../dataModels/Action';
import {Event} from '../dataModels/Event';
import {Schedule} from '../dataModels/Schedule';
import {throwError} from 'rxjs';
import {ThresholdAutomationView} from './ThresholdAutomationView';
import {AutomationGroupContainer} from './AutomationGroupContainer';

export class AutomationGroupView {
    constructor(
        private automationGroup: AutomationGroup,
        events: Event[],
        actions: Action[]
    ) {
        const eventMap = new Map<number, Event>(events.map(event => [event.id, event]));
        const actionMap = new Map<number, Action>(actions.map(action => [action.id, action]));
        const associatedAutomations = new Map<number, Automation>();
        const unassociatedAutomations = new Map<number, Automation>();
        automationGroup.items.forEach(automation => {
            const event = eventMap.get(automation.eventId);
            console.log('event with id ' + event.id + ' instance of Schedule is ' + (event instanceof Schedule));
            if (automation.associatedAutomationId > 0) {
                associatedAutomations.set(automation.id, automation);
            } else {
                unassociatedAutomations.set(automation.id, automation);
            }
        });
        Array.from(associatedAutomations.values()).forEach(automation => {
            const associatedAutomation = unassociatedAutomations.get(automation.associatedAutomationId);
            // Delete from the unassociated once its been used
            unassociatedAutomations.delete(automation.associatedAutomationId);
            const automationView = AutomationGroupView.createAssociatedAutomationView(
                automation, associatedAutomation, actionMap, eventMap);
            this.automations.push(automationView);
        });
        Array.from(unassociatedAutomations.values()).forEach(automation => {
            const automationView = AutomationGroupView.createUnassociatedAutomationView(automation, actionMap, eventMap);
            this.automations.push(automationView);
        });
        this.scheduledAutomations = (this.automations.filter(automation => {
            return automation instanceof ScheduledAutomationView;
        }) as ScheduledAutomationView[]);

        this.status = automationGroup.status;
        this.resumeDate = new Date(automationGroup.resumeDate);
        this.name = automationGroup.name;
    }

    public status: AutomationStatusEnum;
    public automations: AutomationView[] = [];
    public scheduledAutomations: ScheduledAutomationView[] = [];
    public resumeDate: Date;
    public readonly name: string;

    private static createAssociatedAutomationView(
        startAutomation: Automation,
        stopAutomation: Automation,
        actionMap: Map<number, Action>,
        eventMap: Map<number, Event>
    ): AutomationView {
        const startSchedule = AutomationGroupView.verifyIsSchedule(eventMap.get(startAutomation.eventId));
        const stopSchedule = AutomationGroupView.verifyIsSchedule(eventMap.get(stopAutomation.eventId));
        const startAction = actionMap.get(startAutomation.actionId);
        const stopAction = actionMap.get(stopAutomation.actionId);

        // At the moment, all associated automations are scheduled automations.
        return new ScheduledAutomationView(
            startAutomation,
            startSchedule,
            startAction,
            stopAutomation,
            stopSchedule,
            stopAction
        );
    }

    private static createUnassociatedAutomationView(
        automation: Automation,
        actionMap: Map<number, Action>,
        eventMap: Map<number, Event>
    ): AutomationView {
        return new ThresholdAutomationView(automation, actionMap.get(automation.actionId), eventMap.get(automation.eventId));
    }

    private static verifyIsSchedule(event: Event): Schedule {
        if (!(event instanceof Schedule)) {
            throwError(new Error('Event with id ' + event.id + ' found in associated automation but was not a schedule!'));
        }
        return (event as Schedule);
    }

    public toAutomationGroup(): AutomationGroupContainer {
        const events = new Map<number, Event>();
        const actions = new Map<number, Action>();
        const automationItems = new Array<Automation>();

        this.automations.forEach(automation => {
            const automationContainers = automation.toAutomations();
            if (automationContainers.length === 1) {
                const container = automationContainers[0];
                automationItems.push(Object.assign(new Automation(),
                    {
                        id: container.automation.id,
                        eventId: container.event.id,
                        eventType: container.event.type,
                        actionId: container.action.id,
                        actionType: container.action.type,
                        associatedAutomationId: null,
                        status: automation.status,
                        resumeDate: automation.resumeDate.toDateString()
                    }));
            } else {
                const start = automationContainers[0];
                const end = automationContainers[1];
                automationItems.push(Object.assign(new Automation(),
                    {
                        id: start.automation.id,
                        eventId: start.event.id,
                        eventType: start.event.type,
                        actionId: start.action.id,
                        actionType: start.action.type,
                        associatedAutomationId: end.automation.id,
                        status: automation.status,
                        resumeDate: automation.resumeDate.toDateString()
                    }));
                automationItems.push(Object.assign(new Automation(),
                    {
                        id: end.automation.id,
                        eventId: end.event.id,
                        eventType: end.event.type,
                        actionId: end.action.id,
                        actionType: end.action.type,
                        associatedAutomationId: null,
                        status: automation.status,
                        resumeDate: automation.resumeDate.toDateString()
                    }));
            }
        });
        const newAutomationGroup = Object.assign(new AutomationGroup(),
            {
                id: this.automationGroup.id,
                name: this.name,
                status: this.status,
                resumeDate: this.resumeDate.toDateString(),
                items: automationItems
            });
        return new AutomationGroupContainer(
            newAutomationGroup,
            events,
            actions
        );
    }
}
