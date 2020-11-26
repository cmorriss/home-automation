import {ControlGroup} from '../dataModels/ControlGroup';
import {ControlView} from './ControlView';

export class ControlGroupView {
    constructor(
        private controlGroup: ControlGroup
    ) {
        this.controls = controlGroup.items.map(control => {
            return new ControlView(control);
        });
        this.name = controlGroup.name;
    }

    public name: string;
    public controls: ControlView[];
}
