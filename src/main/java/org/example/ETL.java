package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ETL {
    public static String extractKeywords(String title, String searchKeyword) {
        List<String> stopWords = Arrays.asList("dan", "di", "dari", "yang", "dengan", "untuk", "dalam", searchKeyword.toLowerCase());
        List<String> words = Arrays.asList(title.toLowerCase().split(" "));

        return words.stream()
                .filter(word -> !stopWords.contains(word) && word.length() > 3)
                .distinct()
                .limit(5)
                .collect(Collectors.joining(", "));
    }

    public static String convertRelativeDate(String relativeDate) {
        LocalDate today = LocalDate.now();
        LocalDate convertedDate = today;

        Pattern pattern = Pattern.compile("(\\d+)\\s*(hari|minggu|bulan|tahun|jam) lalu");
        Matcher matcher = pattern.matcher(relativeDate.toLowerCase());

        if (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            convertedDate = switch (matcher.group(2)) {
                case "hari" -> today.minusDays(amount);
                case "minggu" -> today.minusWeeks(amount);
                case "bulan" -> today.minusMonths(amount);
                case "tahun" -> today.minusYears(amount);
                default -> convertedDate;
            };
        }
        return convertedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    }

    public static void saveToExcel(List<String[]> data, String fileName) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("News Data");
        String[] headers = {"Judul", "Sumber", "Tanggal", "Link", "Keywords"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(getHeaderCellStyle(workbook));
        }

        int rowNum = 1;
        for (String[] rowData : data) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < rowData.length; i++) {
                row.createCell(i).setCellValue(rowData[i]);
            }
        }

        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static CellStyle getHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    public static String getNextFileName(String baseName) {
        int count = 1;
        String fileName;
        do {
            fileName = baseName + count + ".xlsx";
            count++;
        } while (new File(fileName).exists());
        return fileName;
    }
}
