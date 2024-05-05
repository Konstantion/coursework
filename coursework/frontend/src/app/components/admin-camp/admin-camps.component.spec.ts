import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AdminCampsComponent} from './admin-camps.component';

describe('AdminTablesComponent', () => {
  let component: AdminCampsComponent;
  let fixture: ComponentFixture<AdminCampsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminCampsComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(AdminCampsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
