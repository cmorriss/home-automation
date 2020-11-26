import {Action} from "../dataModels/Action";
import {Event} from "../dataModels/Event";
import {Automation} from "../dataModels/Automation";

export class AutomationContainer {
    constructor(
        public automation: Automation,
        public action: Action,
        public event: Event
    ) {
    }
}