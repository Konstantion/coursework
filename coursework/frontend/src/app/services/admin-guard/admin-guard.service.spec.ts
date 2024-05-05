import {TestBed} from '@angular/core/testing';

import {AdminAccessGuardService} from './admin-access-guard.service';

describe('AdminGuardService', () => {
  let service: AdminAccessGuardService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AdminAccessGuardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
