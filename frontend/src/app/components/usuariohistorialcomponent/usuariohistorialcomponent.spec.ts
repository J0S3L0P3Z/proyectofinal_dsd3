import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppModule } from '../../app-module';

import { Usuariohistorialcomponent } from './usuariohistorialcomponent';

describe('Usuariohistorialcomponent', () => {
  let component: Usuariohistorialcomponent;
  let fixture: ComponentFixture<Usuariohistorialcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppModule],
    }).compileComponents();

    fixture = TestBed.createComponent(Usuariohistorialcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('debe filtrar solo los análisis fallidos', () => {
    component.filtro.set('fallido');
    expect(component.lista().every(a => a.estado === 'fallido')).toBe(true);
  });
});
