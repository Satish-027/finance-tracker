import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CategoryBreakdown, DashboardSummary, MonthlyTrend } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  getSummary(month: number, year: number): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.apiUrl}/summary`, {
      params: { month, year }
    });
  }

  getCategoryBreakdown(month: number, year: number): Observable<CategoryBreakdown[]> {
    return this.http.get<CategoryBreakdown[]>(`${this.apiUrl}/category-breakdown`, {
      params: { month, year }
    });
  }

  getMonthlyTrend(monthsBack: number = 6): Observable<MonthlyTrend[]> {
    return this.http.get<MonthlyTrend[]>(`${this.apiUrl}/monthly-trend`, {
      params: { monthsBack }
    });
  }
}