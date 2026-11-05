package com.designtouch.backend.service;

import com.designtouch.backend.dto.AdminChatLeadSummaryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class ExcelExportServiceTest {

    private final ExcelExportService service = new ExcelExportService();

    @Test
    @DisplayName("1. ExcelExportService generates valid .xlsx ZIP structure with OpenXML parts")
    void generateWorkbook_validLeads_returnsValidXlsx() throws IOException {
        AdminChatLeadSummaryDto lead1 = new AdminChatLeadSummaryDto(
                UUID.randomUUID(),
                "Alex Johnson",
                "alex@designco.com",
                "ACTIVE",
                "WEBSITE_CHATBOT",
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now()
        );

        AdminChatLeadSummaryDto lead2 = new AdminChatLeadSummaryDto(
                UUID.randomUUID(),
                "Sarah Connor",
                "sarah@resistance.org",
                "RESOLVED",
                "WEBSITE_CHATBOT",
                OffsetDateTime.now().minusHours(3),
                OffsetDateTime.now().minusHours(1)
        );

        byte[] xlsxBytes = service.generateChatLeadsWorkbook(List.of(lead1, lead2));

        assertNotNull(xlsxBytes);
        assertTrue(xlsxBytes.length > 500, "XLSX file must contain OpenXML data");

        // Verify ZIP entries and sheet XML content
        List<String> entryNames = new ArrayList<>();
        String sheetXmlContent = "";

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(xlsxBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryNames.add(entry.getName());
                if ("xl/worksheets/sheet1.xml".equals(entry.getName())) {
                    sheetXmlContent = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }

        assertTrue(entryNames.contains("[Content_Types].xml"));
        assertTrue(entryNames.contains("_rels/.rels"));
        assertTrue(entryNames.contains("xl/workbook.xml"));
        assertTrue(entryNames.contains("xl/styles.xml"));
        assertTrue(entryNames.contains("xl/worksheets/sheet1.xml"));

        // Verify lead content in sheet XML
        assertTrue(sheetXmlContent.contains("Alex Johnson"));
        assertTrue(sheetXmlContent.contains("alex@designco.com"));
        assertTrue(sheetXmlContent.contains("Sarah Connor"));
        assertTrue(sheetXmlContent.contains("sarah@resistance.org"));
        assertTrue(sheetXmlContent.contains("Lead ID"));
        assertTrue(sheetXmlContent.contains("Captured Date"));
        assertTrue(sheetXmlContent.contains("Status"));
    }

    @Test
    @DisplayName("2. ExcelExportService handles empty lead list gracefully")
    void generateWorkbook_emptyLeads_returnsValidEmptySheet() throws IOException {
        byte[] xlsxBytes = service.generateChatLeadsWorkbook(List.of());

        assertNotNull(xlsxBytes);
        assertTrue(xlsxBytes.length > 200);

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(xlsxBytes))) {
            ZipEntry entry;
            boolean foundSheet = false;
            while ((entry = zis.getNextEntry()) != null) {
                if ("xl/worksheets/sheet1.xml".equals(entry.getName())) {
                    foundSheet = true;
                    String sheetXml = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    assertTrue(sheetXml.contains("Lead ID"));
                }
            }
            assertTrue(foundSheet, "Should include empty sheet1.xml");
        }
    }
}
