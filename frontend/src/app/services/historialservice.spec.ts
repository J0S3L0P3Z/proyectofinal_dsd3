import { TestBed } from '@angular/core/testing';
import { Historialservice } from './historialservice';

describe('Historialservice', () => {
  let service: Historialservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Historialservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
