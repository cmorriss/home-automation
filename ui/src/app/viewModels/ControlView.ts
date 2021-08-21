import {Control, ControlState, ControlType} from '../dataModels/Control';

export class ControlView {
    public static readonly offColor: string = 'medium';
    public static readonly onColor: string = 'success';
    public static readonly disconnectedColor: string = 'danger';
    public static readonly errorColor: string = 'warning';

    public buttonColor: string;

    constructor(
        private control: Control,
        private created: boolean = false
    ) {
        this.loadControl();
    }

    public id: number;
    public name: string;
    public kind: ControlType;
    public lastUpdate: string;
    public deleted: boolean = false;
    private originalName: string;

    public setControl(newControl: Control) {
        this.control = newControl;
        this.loadControl();
    }

    private loadControl() {
        this.id = this.control.id;
        this.kind = this.control.type;
        this.name = this.control.name;
        this.originalName = this.control.name;
        this.lastUpdate = this.control.lastUpdate;
        this.updateColor();
    }

    public toggle() {
        this.control.state = this.isOn() ? ControlState.OFF : ControlState.ON;
        this.updateColor();
    }

    public isOn() {
        return this.control.state.valueOf() === ControlState.ON.valueOf();
    }

    public isCreated(): boolean {
        return this.created;
    }

    public isChanged(): boolean {
        return this.name != this.originalName || this.isDeleted();
    }

    public isDeleted(): boolean {
        return this.deleted;
    }

    public revertChanges() {
        this.deleted = false;
        this.name = this.originalName;
    }

    public saveChanges() {
        this.originalName = this.name;
        // If we were new, this now indicates that we have been saved and are no longer "new".
        this.id = this.control.id;
    }

    public setError() {
        this.buttonColor = ControlView.errorColor;
    }

    private updateColor() {
        if (this.isOn()) {
            this.buttonColor = ControlView.onColor;
        } else {
            this.buttonColor = ControlView.offColor;
        }
    }

    public toControl(): Control {
        if (this.isOn()) {
            this.control.state = ControlState.ON;
        } else {
            this.control.state = ControlState.OFF;
        }
        this.control.name = this.originalName;
        return this.control;
    }
}
