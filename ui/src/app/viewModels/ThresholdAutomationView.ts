import {AutomationView} from "./AutomationView";
import {Automation} from "../dataModels/Automation";
import {Action} from "../dataModels/Action";
import {Event} from "../dataModels/Event";
import {AutomationContainer} from "./AutomationContainer";
import {AutomationStatusEnum} from './AutomationStatusEnum';

export class ThresholdAutomationView implements AutomationView {
    constructor(
        public existingAutomation: Automation,
        public action: Action,
        public event: Event
    ) {
        this.id = existingAutomation.id;
    }

    public status: AutomationStatusEnum
    public resumeDate: Date
    public id: number;

    public toAutomations(): AutomationContainer[] {
        return [new AutomationContainer(this.existingAutomation, this.action, this.event)];
    }
}
