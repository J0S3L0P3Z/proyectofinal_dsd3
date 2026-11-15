import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppModule } from '../../app-module';

import { Usuariolayoutcomponent } from './usuariolayoutcomponent';

describe('Usuariolayoutcomponent', () => {
  let component: Usuariolayoutcomponent;
  let fixture: ComponentFixture<Usuariolayoutcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppModule],
    }).compileComponents();

    fixture = TestBed.createComponent(Usuariolayoutcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('debe mostrar las iniciales del usuario', () => {
    expect(component.iniciales).toBe('DM');
  });
});
