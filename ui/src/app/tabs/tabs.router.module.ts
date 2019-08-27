import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TabsPage} from './tabs.page';

const routes: Routes = [
    {
        path: 'tabs',
        component: TabsPage,
        children: [
            {
                path: 'controlTab',
                loadChildren: '../controlTab/controlTab.module#ControlTabPageModule'
            },
            {
                path: 'scheduleTab',
                loadChildren: '../scheduleTab/scheduleTab.module#ScheduleTabPageModule'
            },
            {
                path: '',
                redirectTo: '/tabs/controlTab',
                pathMatch: 'full'
            }
        ]
    },
    {
        path: '',
        redirectTo: '/tabs/controlTab',
        pathMatch: 'full'
    }
];

@NgModule({
    imports: [
        RouterModule.forChild(routes)
    ],
    exports: [RouterModule]
})
export class TabsPageRoutingModule {
}
