import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppModule } from '../../app-module';

import { Usuarioperfilcomponent } from './usuarioperfilcomponent';

describe('Usuarioperfilcomponent', () => {
  let component: Usuarioperfilcomponent;
  let fixture: ComponentFixture<Usuarioperfilcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppModule],
    }).compileComponents();

    fixture = TestBed.createComponent(Usuarioperfilcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('debe mostrar un error si las contraseñas no coinciden', () => {
    component.clave = { actual: 'Actual123', nueva: 'NuevaClave1', confirmar: 'OtraClave1' };
    component.cambiarClave();
    expect(component.errorClave).toBe('Las contraseñas no coinciden.');
  });
});
