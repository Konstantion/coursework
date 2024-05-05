import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ButtonModule} from 'primeng/button';
import {RippleModule} from 'primeng/ripple';
import {MenuModule} from 'primeng/menu';
import {InputMaskModule} from 'primeng/inputmask';
import {InputTextModule} from 'primeng/inputtext';
import {DropdownModule} from 'primeng/dropdown';
import {MessagesModule} from 'primeng/messages';
import {ProgressSpinnerModule} from 'primeng/progressspinner';
import {MessageModule} from 'primeng/message';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {LoginComponent} from './components/login/login.component';
import {CampComponent} from './components/camp/camp.component';
import {FormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {SidebarModule} from 'primeng/sidebar';
import {CardModule} from 'primeng/card';
import {BadgeModule} from 'primeng/badge';
import {ToggleButtonModule} from 'primeng/togglebutton';
import {ToastModule} from 'primeng/toast';
import {ConfirmPopupModule} from 'primeng/confirmpopup';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {TabViewModule} from 'primeng/tabview';
import {HttpInterceptorService} from './services/interceptor/http-interceptor.service';
import {ExpeditionComponents} from './components/expeditions/expedition-components.component';
import {ModalComponent} from './components/modal/modal.component';
import {ExpeditionCardComponent} from './components/expedition-card/expedition-card.component';
import {NavbarComponent} from './components/navbar/navbar.component';
import {ExpeditionComponent} from './components/expedition/expedition.component';
import {UserCardComponent} from './components/user-card/user-card.component';
import {AdminComponent} from './components/admin/admin.component';
import {GearsComponent} from './components/gears/gears.component';
import {GearCardComponent} from './components/gear-card/gear-card.component';
import {GearComponent} from './components/gear/gear.component';
import {SpinnerComponent} from './components/spinner/spinner.component';
import {UserComponent} from './components/user/user.component';
import {LogComponent} from './components/log/log.component';
import {EquipmentCardComponent} from './components/equipment-card/equipment-card.component';
import {EquipmentsComponent} from './components/equipments/equipments.component';
import {LogCardComponent} from './components/log-card/log-card.component';
import {LogsComponent} from './components/logs/logs.component';
import {GuestComponent} from './components/guest/guest.component';
import {GuestCardComponent} from './components/guest-card/guest-card.component';
import {GuestsComponent} from './components/guests/guests.component';
import {UsersComponent} from './components/users/users.component';
import {CallsComponent} from './components/calls/calls.component';
import {CategoryComponent} from './components/category/category.component';
import {CategoriesComponent} from './components/categories/categories.component';
import {CategoryCardComponent} from './components/category-card/category-card.component';
import {AdminEquipmentsComponent} from './components/admin-equipments/admin-equipments.component';
import {EquipmentComponent} from "./components/equipment/equipment.component";


@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    CampComponent,
    ExpeditionComponents,
    ModalComponent,
    ExpeditionCardComponent,
    NavbarComponent,
    ExpeditionComponent,
    UserCardComponent,
    AdminComponent,
    EquipmentCardComponent,
    GearsComponent,
    GearCardComponent,
    GearComponent, SpinnerComponent, UserComponent, LogComponent, EquipmentCardComponent, EquipmentsComponent, EquipmentComponent, LogCardComponent, LogsComponent, GuestComponent, GuestCardComponent, GuestsComponent, UsersComponent, CallsComponent, AdminEquipmentsComponent, CategoryComponent, CategoriesComponent, CategoryCardComponent
  ],
  imports: [
    BrowserModule,
    DropdownModule,
    ConfirmPopupModule,
    ConfirmDialogModule,
    ToggleButtonModule,
    TabViewModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    FormsModule,
    InputMaskModule,
    InputTextModule,
    ButtonModule,
    RippleModule,
    ConfirmPopupModule,
    MenuModule,
    SidebarModule,
    HttpClientModule,
    MessagesModule,
    MessageModule,
    CardModule,
    BadgeModule,
    ToastModule,
    ProgressSpinnerModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpInterceptorService,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
