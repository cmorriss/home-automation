import {AutomationGroup} from "../dataModels/AutomationGroup";
import {Event} from "../dataModels/Event";
import {Action} from "../dataModels/Action";

export class AutomationGroupContainer {
    constructor(
        public automationGroup: AutomationGroup,
        public events: Map<number, Event>,
        public actions: Map<number, Action>
    ) {
    }
}