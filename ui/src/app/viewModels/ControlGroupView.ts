import {ControlGroup} from '../dataModels/ControlGroup';
import {ControlView} from './ControlView';

export class ControlGroupView {
    constructor(
        private readonly controlGroup: ControlGroup = null
    ) {
        if (this.controlGroup == null) {
            this.controlGroup = new ControlGroup();
            this.controlGroup.id = -1 * Math.floor(Math.random() * Math.floor(Number.MAX_SAFE_INTEGER));
            this.controlGroup.name = 'New Control Group';
            this.controlGroup.items = [];
        }
            this.controls = this.controlGroup.items.map(control => {
                return new ControlView(control);
            });
            this.name = this.controlGroup.name;
    }

    public readonly id = this.controlGroup.id;
    public name: string;
    public controls: ControlView[];
    public deleted: boolean = false;
    public originalName: string = this.controlGroup.name;
    public originalControls: ControlView[];

    public isNew(): boolean {
        return this.controlGroup.id < 0;
    }

    public isEdited(): boolean {
        let nameChanged = this.name != this.controlGroup.name
        let someCvEdited = !this.controls.every(cv => {
                let cvEdited = cv.isEdited()
                if (cvEdited) {
                    console.log(cv);
                }
                return !cvEdited
            });
        return nameChanged || this.deleted || someCvEdited
    }

    public revertEdits() {
        this.name = this.originalName;
        this.deleted = false;
        this.controls = this.controls.filter(c => !c.isNew()).forEach(c => c.revertEdits());
    }

    public delete() {
        this.deleted = true;
    }

    public deleteControl(c: ControlView)

    public saveEdits() {
        this.originalName = name;
        this.controls.forEach(c => c.saveEdits());
    }

    public toControlGroup(): ControlGroup {
        let controlGroup = new ControlGroup();
        controlGroup.id = this.controlGroup.id;
        controlGroup.items = this.controls.filter(c => !c.deleted).map(controlView => {
            return controlView.toControl();
        })
        controlGroup.name = this.name;
        return controlGroup;
    }
}
