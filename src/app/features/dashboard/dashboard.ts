import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { DashboardService } from '../../core/services/dashboard';
import { TransactionService } from '../../core/services/transaction';
import { CategoryBreakdown, DashboardSummary, MonthlyTrend } from '../../core/models/dashboard.model';
import { TransactionType } from '../../core/models/transaction.model';

const CATEGORY_COLORS = [
  '#3b82f6', '#ef4444', '#f59e0b', '#10b981', '#8b5cf6',
  '#ec4899', '#06b6d4', '#f97316', '#84cc16'
];

const EXPENSE_CATEGORIES = ['Food', 'Rent', 'Travel', 'Shopping', 'Utilities', 'Entertainment', 'Health', 'Other'];
const INCOME_CATEGORIES = ['Salary', 'Freelance', 'Investment', 'Gift', 'Refund', 'Other'];

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BaseChartDirective],
  templateUrl: './dashboard.html'
})
export class Dashboard implements OnInit {
  private dashboardService = inject(DashboardService);
  private transactionService = inject(TransactionService);
  private fb = inject(FormBuilder);

  summary = signal<DashboardSummary | null>(null);
  loading = signal(true);
  saving = signal(false);
  successMessage = signal('');
  errorMessage = signal('');

  currentMonth = new Date().getMonth() + 1;
  currentYear = new Date().getFullYear();

  quickAddForm = this.fb.group({
    type: ['EXPENSE' as TransactionType, Validators.required],
    category: ['', Validators.required],
    customCategory: [''],
    amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    transactionDate: [this.today(), Validators.required],
    description: ['']
  });

  pieChartData = signal<ChartData<'doughnut'>>({ labels: [], datasets: [{ data: [] }] });
  pieChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom', labels: { boxWidth: 10, font: { size: 11 } } } }
  };

  barChartData = signal<ChartData<'bar'>>({ labels: [], datasets: [] });
  barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom', labels: { boxWidth: 10, font: { size: 11 } } } },
    scales: { y: { beginAtZero: true } }
  };

  ngOnInit(): void {
    this.loadAll();

    // Reset category when switching type, so stale Expense category isn't left selected for Income etc.
    this.quickAddForm.get('type')?.valueChanges.subscribe(() => {
      this.quickAddForm.patchValue({ category: '', customCategory: '' });
    });
  }

  get categoryOptions(): string[] {
    return this.quickAddForm.value.type === 'INCOME' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
  }

  get isOtherSelected(): boolean {
    return this.quickAddForm.value.category === 'Other';
  }

  loadAll() {
    this.loadSummary();
    this.loadCategoryBreakdown();
    this.loadMonthlyTrend();
  }

  loadSummary() {
    this.dashboardService.getSummary(this.currentMonth, this.currentYear).subscribe({
      next: (data) => {
        this.summary.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  loadCategoryBreakdown() {
    this.dashboardService.getCategoryBreakdown(this.currentMonth, this.currentYear).subscribe({
      next: (data: CategoryBreakdown[]) => {
        this.pieChartData.set({
          labels: data.map(d => d.category),
          datasets: [{
            data: data.map(d => d.totalAmount),
            backgroundColor: CATEGORY_COLORS
          }]
        });
      }
    });
  }

  loadMonthlyTrend() {
    this.dashboardService.getMonthlyTrend(6).subscribe({
      next: (data: MonthlyTrend[]) => {
        const monthNames = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
        this.barChartData.set({
          labels: data.map(d => monthNames[d.month - 1]),
          datasets: [
            { label: 'Income', data: data.map(d => d.income), backgroundColor: '#10b981' },
            { label: 'Expense', data: data.map(d => d.expense), backgroundColor: '#ef4444' }
          ]
        });
      }
    });
  }

  submitQuickAdd() {
    if (this.quickAddForm.invalid) return;

    const raw = this.quickAddForm.value;
    const finalCategory = raw.category === 'Other' ? (raw.customCategory || '').trim() : raw.category;

    if (raw.category === 'Other' && !finalCategory) {
      this.errorMessage.set('Please enter a category name.');
      return;
    }

    this.saving.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const payload = {
      type: raw.type,
      category: finalCategory,
      amount: raw.amount,
      transactionDate: raw.transactionDate,
      description: raw.description
    };

    this.transactionService.create(payload as any).subscribe({
      next: () => {
        this.saving.set(false);
        this.successMessage.set('Transaction added!');
        this.quickAddForm.reset({
          type: 'EXPENSE',
          category: '',
          customCategory: '',
          amount: null,
          transactionDate: this.today(),
          description: ''
        });
        this.loadAll();
        setTimeout(() => this.successMessage.set(''), 3000);
      },
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to add transaction.');
      }
    });
  }

  private today(): string {
    return new Date().toISOString().split('T')[0];
  }
}