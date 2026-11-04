import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Footercomponent } from './components/footercomponent/footercomponent';
import { Navbarcomponent } from './components/navbarcomponent/navbarcomponent';
import { Iniciocomponent } from './components/iniciocomponent/iniciocomponent';

@NgModule({
  declarations: [App, Footercomponent, Navbarcomponent, Iniciocomponent],
  imports: [BrowserModule, AppRoutingModule],
  providers: [provideBrowserGlobalErrorListeners()],
  bootstrap: [App],
})
export class AppModule {}
