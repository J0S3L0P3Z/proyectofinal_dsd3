import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Gestionusuarioscomponent } from './gestionusuarioscomponent';

describe('Gestionusuarioscomponent', () => {
  let component: Gestionusuarioscomponent;
  let fixture: ComponentFixture<Gestionusuarioscomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Gestionusuarioscomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Gestionusuarioscomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
