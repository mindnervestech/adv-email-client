package utility;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import play.i18n.Lang;
import play.i18n.Messages;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import controllers.Application.AllDailyReport;
import controllers.Application.DailyReport;
import controllers.Application.MonthReport;
import controllers.Application.TodayReport;
import controllers.Application.UnprocessTodayReport;

public class DailyReportPDF {

	// step 1
//	public static void main(String args[]) {
	public static void generateDailyReportPdf(AllDailyReport report, String pdfpath){
		Document document = new Document();
		document.setPageSize(PageSize.A4);
		String outPath = pdfpath;
		System.out.println(outPath);
		// step 2
		try {
			PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream(outPath));
		} catch (FileNotFoundException | DocumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// step 3
		document.open();

		// step 4
		// Production environment
		try {
			generateSalaryPDF("hello",report, document);
		} catch (IOException | DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// step 5
		document.close();

	}

	private static void generateSalaryPDF(String msg, AllDailyReport report, Document document)
			throws IOException, DocumentException {

		MonthReport monthReport = report.monthReport;
		TodayReport todayReport = report.todayReport;
		UnprocessTodayReport unprocessTodayReport = report.unprocessTodayReport;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat df1 = new SimpleDateFormat("HH:mm");

		Font fontChinese;
		Font heading;
		Font tableHeading;
		Font pageHeading;
		String lang = null;

		fontChinese = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
		heading = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
		tableHeading = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
		pageHeading = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
		lang = "en";

		document.newPage();
		Paragraph paragraph;
		PdfPCell cell;
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100);
		float[] columnWidthsTop = { 1f, 1f };
		table.setWidths(columnWidthsTop);

		
		cell = new PdfPCell(new Phrase(
				"Last 30 Days Mail Count By Domain Name", heading));
		cell.setBorderWidthTop(0);
		cell.setBorderColorLeft(BaseColor.WHITE);
		cell.setBorderColorRight(BaseColor.WHITE);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		cell.setColspan(3);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase((Messages.get(
				new Lang(Lang.forCode("en")), "Domain Name")), tableHeading));
		cell.setPaddingLeft(5);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase("Mail Count", tableHeading));
		cell.setVerticalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);
		for (DailyReport dr: monthReport.dailyReports) {
			cell = new PdfPCell(new Phrase((Messages.get(
					new Lang(Lang.forCode("en")), dr.domain)),
					fontChinese));
			cell.setPaddingLeft(5);
			cell.setVerticalAlignment(Element.ALIGN_LEFT);
			table.addCell(cell);

			cell = new PdfPCell(new Phrase(String.valueOf(dr.count), fontChinese));
			cell.setVerticalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
		}

		cell = new PdfPCell(new Phrase("Today's Received Mail Count", heading));
		cell.setBorderWidthTop(0);
		cell.setBorderColorLeft(BaseColor.WHITE);
		cell.setBorderColorRight(BaseColor.WHITE);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		cell.setColspan(3);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase((Messages.get(
				new Lang(Lang.forCode("en")), "Domain Name")), tableHeading));
		cell.setPaddingLeft(5);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase("Mail Count", tableHeading));
		cell.setVerticalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);
		for (DailyReport dr : todayReport.dailyReports) {
			cell = new PdfPCell(new Phrase((Messages.get(
					new Lang(Lang.forCode("en")), dr.domain)),
					fontChinese));
			cell.setPaddingLeft(5);
			cell.setVerticalAlignment(Element.ALIGN_LEFT);
			table.addCell(cell);

			cell = new PdfPCell(new Phrase(String.valueOf(dr.count), fontChinese));
			cell.setVerticalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
		}
		
		cell = new PdfPCell(new Phrase("Today's UnProcessed Mail Count", heading));
		cell.setBorderWidthTop(0);
		cell.setBorderColorLeft(BaseColor.WHITE);
		cell.setBorderColorRight(BaseColor.WHITE);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		cell.setColspan(3);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase((Messages.get(
				new Lang(Lang.forCode("en")), "Domain Name")), tableHeading));
		cell.setPaddingLeft(5);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase("Mail Count", tableHeading));
		cell.setVerticalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);
		List<DailyReport> dailyReports = new ArrayList<DailyReport>();
		for (DailyReport dr : dailyReports) {
			cell = new PdfPCell(new Phrase((Messages.get(
					new Lang(Lang.forCode("en")), dr.domain)),
					fontChinese));
			cell.setPaddingLeft(5);
			cell.setVerticalAlignment(Element.ALIGN_LEFT);
			table.addCell(cell);

			cell = new PdfPCell(new Phrase(String.valueOf(dr.count), fontChinese));
			cell.setVerticalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
		}
		document.add(table);
		paragraph = new Paragraph(" ");
		paragraph.setAlignment(Element.ALIGN_CENTER);
		document.add(paragraph);

	}

}
