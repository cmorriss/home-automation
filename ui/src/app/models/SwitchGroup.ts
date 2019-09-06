import {SwitchView} from "./SwitchView";

export class SwitchGroup {
    constructor(public groupName: string, public displayName: string) {
    }

    public static switchKindDisplayValue = {
        "IRRIGATION_VALVE": "Irrigation Valves",
        "LIGHT_SWITCH": "Light Switches"
    };

    public switches: SwitchView[] = [];

    public static fromSwitches(res: any): Map<string, SwitchGroup> {
        let switches = res.map(aSwitch => new SwitchView(aSwitch));
        let switchMap = switches.reduce((g : Map<string, SwitchGroup>, aSwitch : SwitchView) => {
            let sg = g.get(aSwitch.kind) || new SwitchGroup(aSwitch.kind, SwitchGroup.switchKindDisplayValue[aSwitch.kind]);
            sg.switches.push(aSwitch);
            g.set(aSwitch.kind, sg);
            return g;
        }, new Map());
        console.log(switchMap);
        console.log(switchMap.values());
        return switchMap;
    }
}

