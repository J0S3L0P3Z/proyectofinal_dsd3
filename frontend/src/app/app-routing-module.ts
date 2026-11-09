import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Iniciocomponent } from './components/iniciocomponent/iniciocomponent';
import {Crearcuentacomponent} from './components/crearcuentacomponent/crearcuentacomponent';


const routes: Routes = [

  { path: '', redirectTo: 'inicio', pathMatch: 'full' },
  { path: 'inicio', component: Iniciocomponent },
  { path: 'crearcuentacomponent', component: Crearcuentacomponent },



];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
