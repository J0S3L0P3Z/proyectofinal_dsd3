import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Inicioadmincomponent } from './inicioadmincomponent';

describe('Inicioadmincomponent', () => {
  let component: Inicioadmincomponent;
  let fixture: ComponentFixture<Inicioadmincomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Inicioadmincomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Inicioadmincomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
