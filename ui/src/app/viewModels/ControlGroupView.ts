import {ControlGroup} from '../dataModels/ControlGroup';
import {ControlView} from './ControlView';

export class ControlGroupView {
    constructor(
        private readonly controlGroup: ControlGroup = null
    ) {
        if (this.controlGroup == null) {
            this.controlGroup = ControlGroupView.createEmptyControlGroup();
        }
        this.id = this.controlGroup.id;
        this.originalName = this.controlGroup.name;
        this.controls = this.controlGroup.items.map(control => {
            return new ControlView(control);
        });
        this.name = this.controlGroup.name;
    }

    public readonly id: number;
    public name: string;
    public controls: ControlView[];
    public deleted: boolean = false;
    public originalName: string;
    public originalControls: ControlView[];

    public isNew(): boolean {
        return this.controlGroup.id < 0;
    }

    public isChanged(): boolean {
        let nameChanged = this.name != this.controlGroup.name
        if (nameChanged) {
            console.log(`Control group name changed from ${this.controlGroup.name} to ${this.name}.`)
        }
        let someCvChangedOrCreated = !this.controls.every(cv => {
            let cvChanged = cv.isChanged();
            if (cvChanged) {
                console.log(`Control ${cv.id} was changed.`)
                console.log(cv);
            }
            let cvCreated = cv.isCreated();
            if (cvCreated) {
                console.log(`Control ${cv.id} was created.`)
                console.log(cv);
            }
            return !cvChanged && !cvCreated;
        });
        return nameChanged || this.deleted || someCvChangedOrCreated
    }

    public revertEdits() {
        this.name = this.originalName;
        this.deleted = false;
        this.controls = this.controls.filter(c => !c.isCreated());
        this.controls.forEach(c => c.revertChanges());
    }

    public delete() {
        this.deleted = true;
    }

    public deleteControl(c: ControlView) {

    }

    public saveChanges() {
        this.originalName = name;
        this.controls = this.controls.filter(c => !c.isDeleted());
        this.controls.forEach(c => c.saveChanges());
    }

    private static createEmptyControlGroup(): ControlGroup {
        const cg = new ControlGroup();
        cg.id = -1 * Math.floor(Math.random() * 1000000000);
        cg.name = 'New Control Group';
        cg.items = [];
        return cg;
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
