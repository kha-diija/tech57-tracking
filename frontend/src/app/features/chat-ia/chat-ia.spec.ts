import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChatIa } from './chat-ia';

describe('ChatIa', () => {
  let component: ChatIa;
  let fixture: ComponentFixture<ChatIa>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChatIa],
    }).compileComponents();

    fixture = TestBed.createComponent(ChatIa);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
