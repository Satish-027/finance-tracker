package com.Project.finance_tracker.service;

import com.Project.finance_tracker.entity.Transaction;
import com.Project.finance_tracker.entity.User;
import com.Project.finance_tracker.repository.TransactionRepository;
import com.Project.finance_tracker.security.AuthUtil;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final TransactionRepository transactionRepository;
    private final AuthUtil authUtil;

    private List<Transaction> getMonthTransactions(int month, int year) {
        User user = authUtil.getCurrentUser();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        return transactionRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(user.getId(), start, end);
    }

    public byte[] generateCsv(int month, int year) {
        List<Transaction> transactions = getMonthTransactions(month, year);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {

            writer.writeNext(new String[]{"Date", "Type", "Category", "Amount", "Description"});

            for (Transaction t : transactions) {
                writer.writeNext(new String[]{
                        t.getTransactionDate().format(dateFormatter),
                        t.getType().name(),
                        t.getCategory(),
                        t.getAmount().toString(),
                        t.getDescription() == null ? "" : t.getDescription()
                });
            }

            writer.flush();
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV export", e);
        }
    }

    public byte[] generatePdf(int month, int year) {
        List<Transaction> transactions = getMonthTransactions(month, year);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType().name().equals("INCOME"))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType().name().equals("EXPENSE"))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Expense Report - " + YearMonth.of(year, month))
                    .setBold().setFontSize(16));

            document.add(new Paragraph("Total Income: " + totalIncome));
            document.add(new Paragraph("Total Expense: " + totalExpense));
            document.add(new Paragraph("Net Savings: " + totalIncome.subtract(totalExpense)));
            document.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 3, 2, 4}))
                    .useAllAvailableWidth();

            table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Type").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Category").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));

            for (Transaction t : transactions) {
                table.addCell(t.getTransactionDate().format(dateFormatter));
                table.addCell(t.getType().name());
                table.addCell(t.getCategory());
                table.addCell(t.getAmount().toString());
                table.addCell(t.getDescription() == null ? "-" : t.getDescription());
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF export", e);
        }
    }
}