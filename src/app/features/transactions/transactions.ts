import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TransactionService } from '../../core/services/transaction';
import { Transaction, TransactionType } from '../../core/models/transaction.model';

const DEFAULT_CATEGORIES = ['Food', 'Rent', 'Travel', 'Salary', 'Shopping', 'Utilities', 'Entertainment', 'Health', 'Other'];

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './transactions.html'
})
export class Transactions implements OnInit {
  transactions = signal<Transaction[]>([]);
  loading = signal(false);
  showForm = signal(false);
  editingId = signal<number | null>(null);
  categories = DEFAULT_CATEGORIES;
  errorMessage = '';

  filterType = signal<'ALL' | TransactionType>('ALL');

  form: FormGroup;

  constructor(private fb: FormBuilder, private transactionService: TransactionService) {
    this.form = this.fb.group({
      type: ['EXPENSE' as TransactionType, Validators.required],
      category: ['', Validators.required],
      amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
      transactionDate: [this.today(), Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions() {
    this.loading.set(true);
    this.transactionService.getAll().subscribe({
      next: (data) => {
        this.transactions.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  filteredTransactions() {
    const filter = this.filterType();
    if (filter === 'ALL') return this.transactions();
    return this.transactions().filter(t => t.type === filter);
  }

  openAddForm() {
    this.editingId.set(null);
    this.form.reset({
      type: 'EXPENSE',
      category: '',
      amount: null,
      transactionDate: this.today(),
      description: ''
    });
    this.showForm.set(true);
  }

  openEditForm(t: Transaction) {
    this.editingId.set(t.id);
    this.form.patchValue({
      type: t.type,
      category: t.category,
      amount: t.amount,
      transactionDate: t.transactionDate,
      description: t.description || ''
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
      ? this.transactionService.update(id, payload)
      : this.transactionService.create(payload);

    request$.subscribe({
      next: () => {
        this.closeForm();
        this.loadTransactions();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Something went wrong.';
      }
    });
  }

  deleteTransaction(id: number) {
    if (!confirm('Delete this transaction?')) return;

    this.transactionService.delete(id).subscribe({
      next: () => this.loadTransactions()
    });
  }

  private today(): string {
    return new Date().toISOString().split('T')[0];
  }
}