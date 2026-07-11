import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { BudgetService } from '../../core/services/budget';
import { Budget } from '../../core/models/budget.model';

const DEFAULT_CATEGORIES = ['Food', 'Rent', 'Travel', 'Shopping', 'Utilities', 'Entertainment', 'Health', 'Other'];

@Component({
  selector: 'app-budgets',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './budgets.html'
})
export class Budgets implements OnInit {
  private fb = inject(FormBuilder);
  private budgetService = inject(BudgetService);

  budgets = signal<Budget[]>([]);
  loading = signal(false);
  showForm = signal(false);
  editingId = signal<number | null>(null);
  categories = DEFAULT_CATEGORIES;
  errorMessage = '';

  currentMonth = new Date().getMonth() + 1;
  currentYear = new Date().getFullYear();

  form = this.fb.group({
    category: ['', Validators.required],
    limitAmount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    month: [this.currentMonth, Validators.required],
    year: [this.currentYear, Validators.required]
  });

  ngOnInit(): void {
    this.loadBudgets();
  }


  loadBudgets() {
    this.loading.set(true);
    this.budgetService.getByMonth(this.currentMonth, this.currentYear).subscribe({
      next: (data) => {
        this.budgets.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  openAddForm() {
    this.editingId.set(null);
    this.form.reset({
      category: '',
      limitAmount: null,
      month: this.currentMonth,
      year: this.currentYear
    });
    this.showForm.set(true);
  }

  openEditForm(b: Budget) {
    this.editingId.set(b.id);
    this.form.patchValue({
      category: b.category,
      limitAmount: b.limitAmount,
      month: b.month,
      year: b.year
    });
    this.showForm.set(true);
  }

  closeForm() {
    this.showForm.set(false);
    this.errorMessage = '';
  }

  onSubmit() {
    if (this.form.invalid) return;

    const payload = this.form.value as any;
    const id = this.editingId();

    const request$ = id
      ? this.budgetService.update(id, payload)
      : this.budgetService.create(payload);

    request$.subscribe({
      next: () => {
        this.closeForm();
        this.loadBudgets();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Something went wrong.';
      }
    });
  }

  deleteBudget(id: number) {
    if (!confirm('Delete this budget?')) return;

    this.budgetService.delete(id).subscribe({
      next: () => this.loadBudgets()
    });
  }

  progressBarColor(b: Budget): string {
    if (b.overBudget) return 'bg-red-500';
    if (b.percentUsed >= 80) return 'bg-amber-500';
    return 'bg-green-500';
  }
}