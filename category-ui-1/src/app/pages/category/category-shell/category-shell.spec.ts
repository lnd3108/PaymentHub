import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategoryShell } from './category-shell';

describe('CategoryShell', () => {
  let component: CategoryShell;
  let fixture: ComponentFixture<CategoryShell>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoryShell],
    }).compileComponents();

    fixture = TestBed.createComponent(CategoryShell);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
