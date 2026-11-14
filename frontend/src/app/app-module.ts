import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Footercomponent } from './components/footercomponent/footercomponent';
import { Navbarcomponent } from './components/navbarcomponent/navbarcomponent';
import { Iniciocomponent } from './components/iniciocomponent/iniciocomponent';
// Apartado Usuario
import { Usuariolayoutcomponent } from './components/usuariolayoutcomponent/usuariolayoutcomponent';
import { Usuarioiniciocomponent } from './components/usuarioiniciocomponent/usuarioiniciocomponent';
import { Usuarioscrapingcomponent } from './components/usuarioscrapingcomponent/usuarioscrapingcomponent';
import { Usuarioresultadoscomponent } from './components/usuarioresultadoscomponent/usuarioresultadoscomponent';
import { Usuariohistorialcomponent } from './components/usuariohistorialcomponent/usuariohistorialcomponent';
import { Usuarioreportescomponent } from './components/usuarioreportescomponent/usuarioreportescomponent';
import { Usuarioperfilcomponent } from './components/usuarioperfilcomponent/usuarioperfilcomponent';

@NgModule({
  declarations: [App, Footercomponent, Navbarcomponent, Iniciocomponent,
    Usuariolayoutcomponent, Usuarioiniciocomponent, Usuarioscrapingcomponent, Usuarioresultadoscomponent,
    Usuariohistorialcomponent, Usuarioreportescomponent, Usuarioperfilcomponent],
  imports: [BrowserModule, AppRoutingModule, FormsModule],
  providers: [provideBrowserGlobalErrorListeners()],
  bootstrap: [App],
})
export class AppModule {}
