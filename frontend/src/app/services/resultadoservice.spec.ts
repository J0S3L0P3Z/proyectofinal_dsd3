import { TestBed } from '@angular/core/testing';
import { Resultadoservice } from './resultadoservice';

describe('Resultadoservice', () => {
  let service: Resultadoservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Resultadoservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
