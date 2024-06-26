import {ComponentFixture, TestBed} from '@angular/core/testing';

import {LogCardComponent} from './log-card.component';

describe('BillCardComponent', () => {
  let component: LogCardComponent;
  let fixture: ComponentFixture<LogCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LogCardComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(LogCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
