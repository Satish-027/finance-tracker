export interface Budget {
  id: number;
  category: string;
  limitAmount: number;
  spentAmount: number;
  remainingAmount: number;
  overBudget: boolean;
  percentUsed: number;
  month: number;
  year: number;
}

export interface BudgetRequest {
  category: string;
  limitAmount: number;
  month: number;
  year: number;
}