import {Control, ControlType, ControlState} from '../dataModels/Control';

export class ControlView {
    public static readonly offColor: string = 'medium';
    public static readonly onColor: string = 'success';
    public static readonly disconnectedColor: string = 'danger';
    public static readonly errorColor: string = 'warning';

    public buttonColor: string;

    constructor(
        private control: Control
    ) {
        this.id = control.id;
        this.kind = control.type;
        this.name = control.name;
        this.originalName = control.name;
        this.on = control.state.valueOf() === ControlState.ON.valueOf();
        this.lastUpdate = control.lastUpdate;
        this.updateColor();
    }

    public id: number;
    public name: string;
    public kind: ControlType;
    public on: boolean;
    public lastUpdate: string;
    public deleted: boolean = false;
    private originalName: string;

    public toggle() {
        this.on = !this.on;
        this.updateColor();
    }

    public isEdited(): boolean {
        return this.deleted || this.name != this.originalName;
    }

    public revertEdits() {
        this.deleted = false;
        this.name = this.originalName;
    }

    public saveEdits() {
        this.originalName = this.name;
    }

    private updateColor() {
        if (this.on) {
            this.buttonColor = ControlView.onColor;
        } else {
            this.buttonColor = ControlView.offColor;
        }
    }

    public toControl(): Control {
        if (this.on) {
            this.control.state = ControlState.ON;
        } else {
            this.control.state = ControlState.OFF;
        }
        this.control.name = this.originalName;
        return this.control;
    }
}
