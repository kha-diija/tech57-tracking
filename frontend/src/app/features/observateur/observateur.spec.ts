import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ObservateurPermissions } from './observateur';

describe('ObservateurPermissions', () => {
  let component: ObservateurPermissions;
  let fixture: ComponentFixture<ObservateurPermissions>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ObservateurPermissions],
    }).compileComponents();

    fixture = TestBed.createComponent(ObservateurPermissions);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});