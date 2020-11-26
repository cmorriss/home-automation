import {AutomationContainer} from "./AutomationContainer";
import {Event} from "../dataModels/Event";
import {Action} from "../dataModels/Action";
import {AutomationStatusEnum} from './AutomationStatusEnum';

export interface AutomationView {
    id: number
    action: Action
    event: Event
    status: AutomationStatusEnum
    resumeDate: Date

    toAutomations(): AutomationContainer[]
}
