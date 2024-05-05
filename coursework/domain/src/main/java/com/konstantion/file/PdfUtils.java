package com.konstantion.file;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.konstantion.equipment.Equipment;
import com.konstantion.expedition.Expedition;
import com.konstantion.gear.Gear;
import com.konstantion.guest.Guest;
import com.konstantion.log.Log;
import com.konstantion.user.User;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.itextpdf.text.BaseColor.WHITE;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;

public class PdfUtils {
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font CELL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

    private static final BaseColor LIGHT_GRAY = new BaseColor(0xC0, 0xC0, 0xC0);

    private PdfUtils() {
    }

    public static void fillBillDocumentPdf(
            Log log,
            Equipment equipment,
            Map<Gear, Long> products,
            Expedition table,
            User waiter,
            Guest guest,
            Document document
    ) throws DocumentException {
        // Add title
        Paragraph title = new Paragraph("BILL", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Add subtitle
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Paragraph subtitle = new Paragraph(format("OPENED AT: %s", formatter.format(equipment.getCreatedAt())), HEADER_FONT);
        subtitle.setAlignment(Element.ALIGN_LEFT);
        document.add(subtitle);

        // Add bill closed time
        Paragraph closedAtParagraph = new Paragraph(format("PRINTED AT: %s", formatter.format(now())), HEADER_FONT);
        closedAtParagraph.setAlignment(Element.ALIGN_LEFT);
        document.add(closedAtParagraph);

        // Add table number, waiter name, and guest name
        String waiterName = waiter != null ? String.join(" ", waiter.getLastName(), waiter.getFirstName()) : "";
        String tableName = table != null ? table.getName() : "";
        String guestName = guest != null ? guest.getName() : "";
        Paragraph tableInfo = new Paragraph(format("TABLE: %s%nWAITER: %s%nGUEST: %s%n",
                tableName,
                waiterName,
                guestName), HEADER_FONT);
        tableInfo.setAlignment(Element.ALIGN_LEFT);
        document.add(tableInfo);

        //Add products title
        Paragraph productTitle = new Paragraph(format("PRODUCTS:%n"), SUBTITLE_FONT);
        productTitle.setSpacingAfter(10f);
        productTitle.setAlignment(Element.ALIGN_LEFT);
        document.add(productTitle);

        // Add products table
        PdfPTable productsTable = new PdfPTable(new float[]{4, 4, 2, 2});
        productsTable.setWidthPercentage(100);
        productsTable.setHeaderRows(1);

        // Add table headers
        productsTable.addCell(createCell("PRODUCT", Element.ALIGN_CENTER, LIGHT_GRAY));
        productsTable.addCell(createCell("QUANTITY", Element.ALIGN_CENTER, LIGHT_GRAY));
        productsTable.addCell(createCell("PRICE", Element.ALIGN_CENTER, LIGHT_GRAY));
        productsTable.addCell(createCell("TOTAL", Element.ALIGN_CENTER, LIGHT_GRAY));

        products.forEach((product, quantity) -> {
            productsTable.addCell(createCell(product.getName()));
            productsTable.addCell(createCell(String.valueOf(quantity), Element.ALIGN_CENTER));
            productsTable.addCell(createCell(format("%.2f", product.getPrice()), Element.ALIGN_RIGHT));
            productsTable.addCell(createCell(format("%.2f", product.getPrice() * quantity), Element.ALIGN_RIGHT));
        });

        // Add products total
        productsTable.addCell(createCell("TOTAL", 3, Element.ALIGN_RIGHT, WHITE));
        productsTable.addCell(createCell(format("%.2f", log.getPrice()), Element.ALIGN_RIGHT, WHITE));

        // Add discount
        productsTable.addCell(createCell("DISCOUNT", 3, Element.ALIGN_RIGHT, WHITE));
        String discount = guest != null ? guest.getDiscountPercent() + "%" : "";
        productsTable.addCell(createCell(format("%s", discount), Element.ALIGN_RIGHT, WHITE));

        // Add total with discount
        productsTable.addCell(createCell("TOTAL WITH DISCOUNT", 3, Element.ALIGN_RIGHT, WHITE));
        productsTable.addCell(createCell(format("%.2f", log.getPriceWithDiscount()), Element.ALIGN_RIGHT, WHITE));

        document.add(productsTable);
    }

    private static PdfPCell createCell(String text, int colspan, int alignment, BaseColor background) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, HEADER_FONT));
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(background);
        cell.setPadding(5);
        return cell;
    }

    private static PdfPCell createCell(String text, int alignment, BaseColor background) {
        PdfPCell cell = new PdfPCell(new Phrase(text, CELL_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(background);
        cell.setPadding(5);
        return cell;
    }

    private static PdfPCell createCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, CELL_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        return cell;
    }

    private static PdfPCell createCell(String text) {
        return createCell(text, Element.ALIGN_LEFT, CELL_FONT);
    }

    private static PdfPCell createCell(String text, int alignment, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        return cell;
    }
}
