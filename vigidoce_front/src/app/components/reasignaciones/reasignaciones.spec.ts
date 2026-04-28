import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Reasignaciones } from './reasignaciones';

describe('Reasignaciones', () => {
  let component: Reasignaciones;
  let fixture: ComponentFixture<Reasignaciones>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Reasignaciones],
    }).compileComponents();

    fixture = TestBed.createComponent(Reasignaciones);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
