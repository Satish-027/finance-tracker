export interface DashboardSummary {
  totalIncome: number;
  totalExpense: number;
  netSavings: number;
  month: number;
  year: number;
}

export interface CategoryBreakdown {
  category: string;
  totalAmount: number;
  percentage: number;
}

export interface MonthlyTrend {
  month: number;
  year: number;
  income: number;
  expense: number;
}