import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { ToastrModule } from '@relynn/ngx-toastr';
import { provideHttpClient } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';

import { Logincomponent } from './components/logincomponent/logincomponent';
import { Iniciousuariocomponent } from './components/iniciousuariocomponent/iniciousuariocomponent';
import { Inicioadministradorcomponent } from './components/inicioadministradorcomponent/inicioadministradorcomponent';
import { Registrocomponent } from './components/registrocomponent/registrocomponent';
import { Navbarcomponent } from './components/navbarcomponent/navbarcomponent';
import { Footercomponent } from './components/footercomponent/footercomponent';
import { Scrapingcomponent } from './components/scrapingcomponent/scrapingcomponent';
import { Historialcomponent } from './components/historialcomponent/historialcomponent';
import { Reportecomponent } from './components/reportecomponent/reportecomponent';
import { Resultadocomponent } from './components/resultadocomponent/resultadocomponent';
import { Recuperacionpassword } from './components/recuperacionpassword/recuperacionpassword';
import { Navbaradministradorcomponent } from './components/navbaradministradorcomponent/navbaradministradorcomponent';
import { Navbarusuariocomponent } from './components/navbarusuariocomponent/navbarusuariocomponent';
import { Gestionusuarioscomponent } from './components/gestionusuarioscomponent/gestionusuarioscomponent';

/**
 * Módulo principal de la aplicación. Declara todos los componentes, importa los formularios,
 * las rutas y las notificaciones (Toastr), y provee el cliente HTTP.
 */
@NgModule({
  declarations: [
    App,
    Logincomponent,
    Iniciousuariocomponent,
    Inicioadministradorcomponent,
    Registrocomponent,
    Navbarcomponent,
    Footercomponent,
    Scrapingcomponent,
    Historialcomponent,
    Reportecomponent,
    Resultadocomponent,
    Recuperacionpassword,
    Navbaradministradorcomponent,
    Navbarusuariocomponent,
    Gestionusuarioscomponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    ToastrModule.forRoot({
      timeOut: 6000,
      positionClass: 'toast-bottom-right',
      preventDuplicates: true,
      closeButton: true,
      progressBar: true,
      newestOnTop: true,
    }),
  ],
  providers: [provideBrowserGlobalErrorListeners(), provideHttpClient()],
  bootstrap: [App],
})
export class AppModule {}
