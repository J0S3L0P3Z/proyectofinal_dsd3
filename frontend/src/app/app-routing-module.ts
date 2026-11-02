import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Iniciocomponent } from './components/iniciocomponent/iniciocomponent';


const routes: Routes = [

  { path: '', redirectTo: 'inicio', pathMatch: 'full' },
  { path: 'inicio', component: Iniciocomponent },



];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
