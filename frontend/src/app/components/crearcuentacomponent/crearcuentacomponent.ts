import {AfterViewInit, Component} from '@angular/core';

declare var bootstrap: any;

@Component({
  selector: 'app-crearcuentacomponent',
  standalone: false,
  styleUrl: './crearcuentacomponent.css',
  templateUrl: './crearcuentacomponent.html',
})


export class Crearcuentacomponent  {

  fuerza: number = 0;
  textoFuerza: string = '';

  verificarFuerza(event: any) {
    const valor = event.target.value;
    let score = 0;

    if (valor.length === 0) {
      this.fuerza = 0;
      this.textoFuerza = '';
      return;
    }

    if (valor.length >= 6) score++;
    if (valor.length >= 10) score++;
    if (/[A-Z]/.test(valor) && /[0-9]/.test(valor)) score++;
    if (/[^A-Za-z0-9]/.test(valor)) score++;

    this.fuerza = score;

    if (score <= 1) this.textoFuerza = 'Contraseña muy débil';
    else if (score === 2) this.textoFuerza = 'Contraseña moderada';
    else if (score === 3) this.textoFuerza = 'Buena contraseña';
    else this.textoFuerza = 'Contraseña excelente';
  }
}

