export type TransactionType = 'INCOME' | 'EXPENSE';

export interface Transaction {
  id: number;
  type: TransactionType;
  category: string;
  amount: number;
  transactionDate: string;
  description: string | null;
}

export interface TransactionRequest {
  type: TransactionType;
  category: string;
  amount: number;
  transactionDate: string;
  description: string | null;
}