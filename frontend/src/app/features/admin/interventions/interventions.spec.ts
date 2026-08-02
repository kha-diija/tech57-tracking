import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Interventions } from './interventions';

describe('Interventions', () => {
  let component: Interventions;
  let fixture: ComponentFixture<Interventions>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Interventions],
    }).compileComponents();

    fixture = TestBed.createComponent(Interventions);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
