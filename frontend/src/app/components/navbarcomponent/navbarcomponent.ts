import { Component , OnInit, inject } from '@angular/core';
import { UsuarioDatosService } from '../usuariolayoutcomponent/usuario-datos.service';

@Component({
  selector: 'app-navbarcomponent',
  standalone: false,
  styleUrl: './navbarcomponent.css',
  templateUrl: './navbarcomponent.html',
})
export class Navbarcomponent implements OnInit {

  userRole: 'admin' | 'user' = 'admin';

  // Datos del usuario (nombre y rol) para el navbar del usuario
  readonly datosUsuario = inject(UsuarioDatosService);

  constructor() { }

  ngOnInit(): void {

  }
}
