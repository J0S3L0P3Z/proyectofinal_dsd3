import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing-module';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { App } from './app';
import { Footercomponent } from './components/footercomponent/footercomponent';
import { Iniciocomponent } from './components/iniciocomponent/iniciocomponent';
import { Crearcuentacomponent } from './components/crearcuentacomponent/crearcuentacomponent';
import { Inicioadmincomponent } from './components/inicioadmincomponent/inicioadmincomponent';
<<<<<<< Updated upstream
import {Navbarcomponent} from './components/navbarcomponent/navbarcomponent';

@NgModule({
  declarations: [App, Iniciocomponent, Footercomponent, Crearcuentacomponent, Inicioadmincomponent, Navbarcomponent],
=======
import { Navbarcomponent } from './components/navbarcomponent/navbarcomponent';
// Apartado Usuario
import { Usuariolayoutcomponent } from './components/usuariolayoutcomponent/usuariolayoutcomponent';
import { Usuarioiniciocomponent } from './components/usuarioiniciocomponent/usuarioiniciocomponent';
import { Usuarioscrapingcomponent } from './components/usuarioscrapingcomponent/usuarioscrapingcomponent';
import { Usuarioresultadoscomponent } from './components/usuarioresultadoscomponent/usuarioresultadoscomponent';
import { Usuariohistorialcomponent } from './components/usuariohistorialcomponent/usuariohistorialcomponent';
import { Usuarioreportescomponent } from './components/usuarioreportescomponent/usuarioreportescomponent';
import { Usuarioperfilcomponent } from './components/usuarioperfilcomponent/usuarioperfilcomponent';
import { Gestionusuarioscomponent } from './components/admingestionusuarioscomponent/gestionusuarioscomponent';
import { Adminscrapingcomponent } from './components/adminscrapingcomponent/adminscrapingcomponent';
import { Adminperfilcomponent } from './components/adminperfilcomponent/adminperfilcomponent';
import { Adminestadisticascomponent } from './components/adminestadisticascomponent/adminestadisticascomponent';
import { Adminhistorialcomponent } from './components/adminhistorialcomponent/adminhistorialcomponent';

@NgModule({
  declarations: [
    App,
    Iniciocomponent,
    Footercomponent,
    Crearcuentacomponent,
    Inicioadmincomponent,
    Navbarcomponent,
    Usuariolayoutcomponent,
    Usuarioiniciocomponent,
    Usuarioscrapingcomponent,
    Usuarioresultadoscomponent,
    Usuariohistorialcomponent,
    Usuarioreportescomponent,
    Usuarioperfilcomponent,
    Gestionusuarioscomponent,
    Adminscrapingcomponent,
    Adminperfilcomponent,
    Adminestadisticascomponent,
    Adminhistorialcomponent,
  ],
>>>>>>> Stashed changes
  imports: [BrowserModule, AppRoutingModule, CommonModule, FormsModule],
  providers: [provideBrowserGlobalErrorListeners()],
  bootstrap: [App],
})
export class AppModule {}
