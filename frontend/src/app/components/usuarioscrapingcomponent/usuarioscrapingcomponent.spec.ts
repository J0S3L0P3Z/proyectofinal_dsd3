import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppModule } from '../../app-module';

import { Usuarioscrapingcomponent } from './usuarioscrapingcomponent';

describe('Usuarioscrapingcomponent', () => {
  let component: Usuarioscrapingcomponent;
  let fixture: ComponentFixture<Usuarioscrapingcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppModule],
    }).compileComponents();

    fixture = TestBed.createComponent(Usuarioscrapingcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('debe mostrar un error si la URL no es válida', () => {
    component.url = 'pagina-sin-protocolo';
    component.iniciarScraping();
    expect(component.errorUrl).not.toBe('');
    expect(component.estado).toBe('inactivo');
  });
});
