import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppModule } from '../../app-module';

import { Usuarioreportescomponent } from './usuarioreportescomponent';

describe('Usuarioreportescomponent', () => {
  let component: Usuarioreportescomponent;
  let fixture: ComponentFixture<Usuarioreportescomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppModule],
    }).compileComponents();

    fixture = TestBed.createComponent(Usuarioreportescomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('debe listar solo análisis con reporte PDF', () => {
    expect(component.conReporte().every(a => a.reporte !== null)).toBe(true);
  });
});
