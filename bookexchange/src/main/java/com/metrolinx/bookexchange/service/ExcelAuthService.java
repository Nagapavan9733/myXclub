package com.metrolinx.bookexchange.service;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelAuthService {

	//path to the actual Excel file location
    private static final String EXCEL_FILE_PATH = "C:\\Users\\naga.pavan.marisetty\\Downloads\\HC-3 Oct.xlsx";
    
    public boolean isEmailAuthorized(String email) {
        try {
            List<String> authorizedEmails = getAllAuthorizedEmails();
            String normalized = email.toLowerCase().replace("@accenture.com", "").trim();
            return authorizedEmails.contains(normalized);
        } catch (Exception e) {
            throw new RuntimeException("Error validating email against member list", e);
        }
    }
    
    private List<String> getAllAuthorizedEmails() throws IOException {
        List<String> emails = new ArrayList<>();
        
        try (FileInputStream file = new FileInputStream(EXCEL_FILE_PATH);
             Workbook workbook = new XSSFWorkbook(file)) {
            
            Sheet sheet = workbook.getSheetAt(0); // First sheet
            
            // Find email column index
            Row headerRow = sheet.getRow(0);
            int emailColumnIndex = -1;
            
            for (Cell cell : headerRow) {
                if ("E-mail Address".equalsIgnoreCase(cell.getStringCellValue().trim())) {
                    emailColumnIndex = cell.getColumnIndex();
                    break;
                }
            }
            
            if (emailColumnIndex == -1) {
                throw new RuntimeException("Email column not found in Excel file");
            }
            
            // Read all emails from the column
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell emailCell = row.getCell(emailColumnIndex);
                    if (emailCell != null) {
                        String email = emailCell.getStringCellValue().trim().toLowerCase();
                        if (!email.isEmpty()) {
                            emails.add(email);
                        }
                    }
                }
            }
        }
        
        return emails;
    }
}
