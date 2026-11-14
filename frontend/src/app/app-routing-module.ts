import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Iniciocomponent } from './components/iniciocomponent/iniciocomponent';
// Apartado Usuario
import { Usuariolayoutcomponent } from './components/usuariolayoutcomponent/usuariolayoutcomponent';
import { Usuarioiniciocomponent } from './components/usuarioiniciocomponent/usuarioiniciocomponent';
import { Usuarioscrapingcomponent } from './components/usuarioscrapingcomponent/usuarioscrapingcomponent';
import { Usuarioresultadoscomponent } from './components/usuarioresultadoscomponent/usuarioresultadoscomponent';
import { Usuariohistorialcomponent } from './components/usuariohistorialcomponent/usuariohistorialcomponent';
import { Usuarioreportescomponent } from './components/usuarioreportescomponent/usuarioreportescomponent';
import { Usuarioperfilcomponent } from './components/usuarioperfilcomponent/usuarioperfilcomponent';

const routes: Routes = [
  { path: '', redirectTo: 'inicio', pathMatch: 'full' },
  { path: 'inicio', component: Iniciocomponent },

  // Apartado Usuario
  {
    path: 'usuario',
    component: Usuariolayoutcomponent,
    children: [
      { path: '', redirectTo: 'inicio', pathMatch: 'full' },
      { path: 'inicio', component: Usuarioiniciocomponent },
      { path: 'scraping', component: Usuarioscrapingcomponent },
      { path: 'resultados/:id', component: Usuarioresultadoscomponent },
      { path: 'historial', component: Usuariohistorialcomponent },
      { path: 'reportes', component: Usuarioreportescomponent },
      { path: 'perfil', component: Usuarioperfilcomponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
