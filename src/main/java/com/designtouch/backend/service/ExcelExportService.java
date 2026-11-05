package com.designtouch.backend.service;

import com.designtouch.backend.dto.AdminChatLeadSummaryDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * High-performance, dependency-free Microsoft Excel (.xlsx) OpenXML workbook generator.
 * Produces genuine, standard-compliant .xlsx spreadsheets directly via ZipOutputStream.
 */
@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generateChatLeadsWorkbook(List<AdminChatLeadSummaryDto> leads) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            // 1. [Content_Types].xml
            addZipEntry(zos, "[Content_Types].xml", getContentTypesXml());

            // 2. _rels/.rels
            addZipEntry(zos, "_rels/.rels", getRootRelsXml());

            // 3. xl/_rels/workbook.xml.rels
            addZipEntry(zos, "xl/_rels/workbook.xml.rels", getWorkbookRelsXml());

            // 4. xl/workbook.xml
            addZipEntry(zos, "xl/workbook.xml", getWorkbookXml());

            // 5. xl/styles.xml
            addZipEntry(zos, "xl/styles.xml", getStylesXml());

            // 6. xl/worksheets/sheet1.xml
            addZipEntry(zos, "xl/worksheets/sheet1.xml", getSheetXml(leads));
        }
        return baos.toByteArray();
    }

    private void addZipEntry(ZipOutputStream zos, String path, String content) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        zos.putNextEntry(entry);
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private String getContentTypesXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                  <Default Extension="xml" ContentType="application/xml"/>
                  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
                </Types>
                """;
    }

    private String getRootRelsXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                </Relationships>
                """;
    }

    private String getWorkbookRelsXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
                </Relationships>
                """;
    }

    private String getWorkbookXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                  <sheets>
                    <sheet name="Chat Leads" sheetId="1" r:id="rId1"/>
                  </sheets>
                </workbook>
                """;
    }

    private String getStylesXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                  <fonts count="2">
                    <font><name val="Calibri"/><sz val="11"/></font>
                    <font><b/><name val="Calibri"/><sz val="11"/><color rgb="FFFFFFFF"/></font>
                  </fonts>
                  <fills count="3">
                    <fill><patternFill patternType="none"/></fill>
                    <fill><patternFill patternType="gray125"/></fill>
                    <fill><patternFill patternType="solid"><fgColor rgb="FF141414"/><bgColor indexed="64"/></patternFill></fill>
                  </fills>
                  <borders count="1">
                    <border><left/><right/><top/><bottom/><diagonal/></border>
                  </borders>
                  <cellStyleXfs count="1">
                    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
                  </cellStyleXfs>
                  <cellXfs count="2">
                    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
                    <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
                  </cellXfs>
                </styleSheet>
                """;
    }

    private String getSheetXml(List<AdminChatLeadSummaryDto> leads) {
        StringBuilder sb = new StringBuilder(1024 + (leads != null ? leads.size() * 200 : 0));
        sb.append("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                  <cols>
                    <col min="1" max="1" width="38" customWidth="1"/>
                    <col min="2" max="2" width="26" customWidth="1"/>
                    <col min="3" max="3" width="34" customWidth="1"/>
                    <col min="4" max="4" width="24" customWidth="1"/>
                    <col min="5" max="5" width="20" customWidth="1"/>
                  </cols>
                  <sheetData>
                """);

        // Header Row (r=1, s=1 for dark bold header style)
        sb.append("""
                    <row r="1">
                      <c r="A1" t="inlineStr" s="1"><is><t>Lead ID</t></is></c>
                      <c r="B1" t="inlineStr" s="1"><is><t>Visitor Name</t></is></c>
                      <c r="C1" t="inlineStr" s="1"><is><t>Email</t></is></c>
                      <c r="D1" t="inlineStr" s="1"><is><t>Captured Date</t></is></c>
                      <c r="E1" t="inlineStr" s="1"><is><t>Status</t></is></c>
                    </row>
                """);

        if (leads != null) {
            int rowNum = 2;
            for (AdminChatLeadSummaryDto lead : leads) {
                String idStr = lead.id() != null ? lead.id().toString() : "";
                String nameStr = escapeXml(lead.name());
                String emailStr = escapeXml(lead.email());
                String createdStr = lead.createdAt() != null ? lead.createdAt().format(DATE_FORMATTER) : "";
                String statusStr = escapeXml(lead.conversationStatus() != null ? lead.conversationStatus() : "ACTIVE");

                sb.append("<row r=\"").append(rowNum).append("\">");
                sb.append("<c r=\"A").append(rowNum).append("\" t=\"inlineStr\"><is><t>").append(idStr).append("</t></is></c>");
                sb.append("<c r=\"B").append(rowNum).append("\" t=\"inlineStr\"><is><t>").append(nameStr).append("</t></is></c>");
                sb.append("<c r=\"C").append(rowNum).append("\" t=\"inlineStr\"><is><t>").append(emailStr).append("</t></is></c>");
                sb.append("<c r=\"D").append(rowNum).append("\" t=\"inlineStr\"><is><t>").append(createdStr).append("</t></is></c>");
                sb.append("<c r=\"E").append(rowNum).append("\" t=\"inlineStr\"><is><t>").append(statusStr).append("</t></is></c>");
                sb.append("</row>");
                rowNum++;
            }
        }

        sb.append("""
                  </sheetData>
                </worksheet>
                """);
        return sb.toString();
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}

