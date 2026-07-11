import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ExportService } from '../../core/services/export';

@Component({
  selector: 'app-export',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './export.html'
})
export class Export {
  private exportService = inject(ExportService);

  selectedMonth = new Date().getMonth() + 1;
  selectedYear = new Date().getFullYear();
  downloading = signal<'csv' | 'pdf' | null>(null);
  errorMessage = '';

  months = [
    { value: 1, label: 'January' }, { value: 2, label: 'February' },
    { value: 3, label: 'March' }, { value: 4, label: 'April' },
    { value: 5, label: 'May' }, { value: 6, label: 'June' },
    { value: 7, label: 'July' }, { value: 8, label: 'August' },
    { value: 9, label: 'September' }, { value: 10, label: 'October' },
    { value: 11, label: 'November' }, { value: 12, label: 'December' }
  ];

  years = this.buildYearRange();

  downloadCsv() {
    this.downloading.set('csv');
    this.errorMessage = '';

    this.exportService.downloadCsv(this.selectedMonth, this.selectedYear).subscribe({
      next: (blob) => {
        this.triggerDownload(blob, `expense-report-${this.selectedYear}-${this.selectedMonth}.csv`);
        this.downloading.set(null);
      },
      error: () => {
        this.errorMessage = 'Failed to download CSV. Please try again.';
        this.downloading.set(null);
      }
    });
  }

  downloadPdf() {
    this.downloading.set('pdf');
    this.errorMessage = '';

    this.exportService.downloadPdf(this.selectedMonth, this.selectedYear).subscribe({
      next: (blob) => {
        this.triggerDownload(blob, `expense-report-${this.selectedYear}-${this.selectedMonth}.pdf`);
        this.downloading.set(null);
      },
      error: () => {
        this.errorMessage = 'Failed to download PDF. Please try again.';
        this.downloading.set(null);
      }
    });
  }

  private triggerDownload(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  private buildYearRange(): number[] {
    const current = new Date().getFullYear();
    return [current - 1, current, current + 1];
  }
}