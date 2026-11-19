import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Adminhistorialcomponent } from './adminhistorialcomponent';

describe('Adminhistorialcomponent', () => {
  let component: Adminhistorialcomponent;
  let fixture: ComponentFixture<Adminhistorialcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Adminhistorialcomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Adminhistorialcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
