import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing-module';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { App } from './app';
import { Footercomponent } from './components/footercomponent/footercomponent';
import { Iniciocomponent } from './components/iniciocomponent/iniciocomponent';
import { Crearcuentacomponent } from './components/crearcuentacomponent/crearcuentacomponent';

@NgModule({
  declarations: [App, Iniciocomponent, Footercomponent, Crearcuentacomponent],
  imports: [BrowserModule, AppRoutingModule, CommonModule, FormsModule],
  providers: [provideBrowserGlobalErrorListeners()],
  bootstrap: [App],
})
export class AppModule {}
