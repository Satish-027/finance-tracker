import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ExportService {
  private readonly apiUrl = `${environment.apiUrl}/export`;

  constructor(private http: HttpClient) {}

  downloadCsv(month: number, year: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/csv`, {
      params: { month, year },
      responseType: 'blob'
    });
  }

  downloadPdf(month: number, year: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/pdf`, {
      params: { month, year },
      responseType: 'blob'
    });
  }
}