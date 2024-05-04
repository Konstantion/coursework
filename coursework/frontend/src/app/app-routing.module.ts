import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { CampComponent } from './components/camp/camp.component';
import { AccessGuardService } from './services/guard/access-guard.service';
import { ExpeditionComponents } from './components/expeditions/expedition-components.component';
import { ExpeditionComponent } from './components/expedition/expedition.component';
import { AdminComponent } from './components/admin/admin.component';
import { AdminAccessGuardService } from './services/admin-guard/admin-access-guard.service';
import { OrderComponent } from './components/order/order.component';
import { GearsComponent } from './components/gears/gears.component';
import { GearComponent } from './components/gear/gear.component';
import { UserComponent } from './components/user/user.component';
import { BillComponent } from './components/bill/bill.component';
import { OrdersComponent } from './components/orders/orders.component';
import { BillsComponent } from './components/bills/bills.component';
import { GuestComponent } from './components/guest/guest.component';
import { CategoryComponent } from './components/category/category.component';

const routes: Routes = [{
  path: '',
  redirectTo: 'expeditions',
  pathMatch: 'full'
},
{
  path: 'login',
  component: LoginComponent
},
{
  path: 'camps/:id',
  component: CampComponent,
  canActivate: [AccessGuardService]
},
{
  path: 'users/:id',
  component: UserComponent,
  canActivate: [AccessGuardService]
},
{
  path: 'expeditions/:id',
  component: ExpeditionComponent,
  canActivate: [AccessGuardService]
},
{
  path: 'equipments/:id',
  component: OrderComponent,
  canActivate: [AccessGuardService]
},
{
  path: 'equipments',
  component: OrdersComponent,
  canActivate: [AccessGuardService]
},
{
  path: 'logs',
  component: BillsComponent,
  canActivate: [AccessGuardService]
},
{
  path: 'logs/:id',
  component: BillComponent,
  canActivate: [AccessGuardService]
},
{
  path: 'gears/:id',
  component: GearComponent,
  canActivate: [AccessGuardService]
},
{
  path: 'guests/:id',
  component: GuestComponent,
  canActivate: [AccessGuardService]
},
{
  path: 'categories/:id',
  component: CategoryComponent,
  canActivate: [AdminAccessGuardService]
},
{
  path: 'expeditions',
  component: ExpeditionComponents,
  canActivate: [AccessGuardService]
},
{
  path: 'gears',
  component: GearsComponent,
  canActivate: [AccessGuardService]
},
{
  path: 'admin',
  component: AdminComponent,
  canActivate: [AdminAccessGuardService]
},
{
  path: '**',
  redirectTo: 'expeditions'
}];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
