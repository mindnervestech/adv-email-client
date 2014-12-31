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
import controllers.Application.DomainList;
import controllers.Application.HostDomainList;
import controllers.Application.MailVariastion;
import controllers.Application.MonthReport;
import controllers.Application.MonthUnprocessReport;
import controllers.Application.RecentDomainList;
import controllers.Application.TodayReport;
import controllers.Application.TotalUnprocessReport;
import controllers.Application.VariationDetails;

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

		TotalUnprocessReport totalUnprocessReport = report.totalUnprocessReport;
		MonthUnprocessReport monthUnprocessReport = report.monthUnprocessReport;
		MonthReport monthReport = report.monthReport;
		TodayReport todayReport = report.todayReport;
		DomainList domainList = report.domainList;
		HostDomainList hostDomainList = report.hostDomainList;
		RecentDomainList recentDomainList = report.recentDomainList;
		MailVariastion mailVariastion = report.mailVariation;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat df1 = new SimpleDateFormat("HH:mm");

		Font fontChinese;
		Font heading;
		Font tableHeading;
		Font pageHeading;
		String lang = null;

		fontChinese = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
		heading = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
		tableHeading = new Font(Font.FontFamily.TIMES_ROMAN, 11, Font.BOLD);
		pageHeading = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
		lang = "en";

		document.newPage();
		Paragraph paragraph;
		PdfPCell cell;
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100);
		float[] columnWidthsTop = { 1f, 1f };
		table.setWidths(columnWidthsTop);

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
		
		cell = new PdfPCell(new Phrase(
				"Last 30 Day's Mail Count By Domain Name", heading));
		cell.setBorderWidthTop(0);
		cell.setBorderColorLeft(BaseColor.WHITE);
		cell.setBorderColorRight(BaseColor.WHITE);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		cell.setColspan(3);
		cell.setPaddingTop(20);
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

		
		
		cell = new PdfPCell(new Phrase("Last 30 Day's UnProcessed Mail Count", heading));
		cell.setBorderWidthTop(0);
		cell.setBorderColorLeft(BaseColor.WHITE);
		cell.setBorderColorRight(BaseColor.WHITE);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		cell.setColspan(3);
		cell.setPaddingTop(20);
		table.addCell(cell);
		for (DailyReport dr : monthUnprocessReport.dailyReports) {
			cell = new PdfPCell(new Phrase((Messages.get(
					new Lang(Lang.forCode("en")), "Count")),
					fontChinese));
			cell.setPaddingLeft(5);
			cell.setVerticalAlignment(Element.ALIGN_LEFT);
			table.addCell(cell);

			cell = new PdfPCell(new Phrase(String.valueOf(dr.count), fontChinese));
			cell.setVerticalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
		}

		
		cell = new PdfPCell(new Phrase("Total UnProcessed Mail Count", heading));
		cell.setBorderWidthTop(0);
		cell.setBorderColorLeft(BaseColor.WHITE);
		cell.setBorderColorRight(BaseColor.WHITE);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		cell.setColspan(3);
		cell.setPaddingTop(20);
		table.addCell(cell);
		for (DailyReport dr : totalUnprocessReport.dailyReports) {
			cell = new PdfPCell(new Phrase((Messages.get(
					new Lang(Lang.forCode("en")), "Count")),
					fontChinese));
			cell.setPaddingLeft(5);
			cell.setVerticalAlignment(Element.ALIGN_LEFT);
			table.addCell(cell);

			cell = new PdfPCell(new Phrase(String.valueOf(dr.count), fontChinese));
			cell.setVerticalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
		}
		cell = new PdfPCell(new Phrase("Domain List", heading));
		cell.setBorderWidthTop(0);
		cell.setBorderColorLeft(BaseColor.WHITE);
		cell.setBorderColorRight(BaseColor.WHITE);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		cell.setColspan(3);
		cell.setPaddingTop(20);
		table.addCell(cell);
		
		
		cell = new PdfPCell(new Phrase((Messages.get(
				new Lang(Lang.forCode("en")), "Agora-D Domain Name")), tableHeading));
		cell.setPaddingLeft(5);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase("Agora-E Domain Name", tableHeading));
		cell.setVerticalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);
		List<DailyReport> dailyReports = new ArrayList<DailyReport>(); 
		if(hostDomainList.dailyReports.size() >domainList.dailyReports.size()){
			dailyReports = hostDomainList.dailyReports;
		}else{
			dailyReports = domainList.dailyReports;
		}
		for (int i = 0; i<dailyReports.size(); i++) {
			try{
			if(hostDomainList.dailyReports  != null){
				cell = new PdfPCell(new Phrase((Messages.get(
						new Lang(Lang.forCode("en")), hostDomainList.dailyReports.get(i).domain)),
						fontChinese));
				cell.setPaddingLeft(5);
				cell.setVerticalAlignment(Element.ALIGN_LEFT);
				table.addCell(cell);
			}
				
			}catch (Exception e){
				cell = new PdfPCell(new Phrase((Messages.get(
						new Lang(Lang.forCode("en")), "")),
						fontChinese));
				cell.setPaddingLeft(5);
				cell.setVerticalAlignment(Element.ALIGN_LEFT);
				table.addCell(cell);
			}
			
			try{
				if(domainList.dailyReports != null){
					cell = new PdfPCell(new Phrase(domainList.dailyReports.get(i).domain, fontChinese));
					cell.setVerticalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);
				
				}
			}catch(Exception e){
				cell = new PdfPCell(new Phrase("", fontChinese));
				cell.setVerticalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cell);
			}
		}
		
		PdfPTable table2 = new PdfPTable(1);
		table2.setWidthPercentage(50);
		float[] _columnWidthsTop = {1f};
		table2.setWidths(_columnWidthsTop);
		table2.setHorizontalAlignment(Element.ALIGN_LEFT);
		
		
		cell = new PdfPCell(new Phrase("Recently Added Domain List", heading));
		cell.setBorderWidthTop(0);
		cell.setBorderColorLeft(BaseColor.WHITE);
		cell.setBorderColorRight(BaseColor.WHITE);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		cell.setColspan(3);
		cell.setPaddingTop(20);
		table2.addCell(cell);
		
		for (DailyReport dr : recentDomainList.dailyReports) {
			cell = new PdfPCell(new Phrase((Messages.get(
					new Lang(Lang.forCode("en")), dr.domain)),
					fontChinese));
			cell.setPaddingLeft(5);
			cell.setVerticalAlignment(Element.ALIGN_LEFT);
			table2.addCell(cell);
		}
		
		PdfPTable table3 = new PdfPTable(4);
		table3.setWidthPercentage(100);
		float[] columnWidthsTop3 = {2f, 1f, 1f, 1f};
		table3.setWidths(columnWidthsTop3);
		table3.setHorizontalAlignment(Element.ALIGN_LEFT);
		
		
		cell = new PdfPCell(new Phrase(
				"Mail Variations", heading));
		cell.setBorderWidthTop(0);
		cell.setBorderColorLeft(BaseColor.WHITE);
		cell.setBorderColorRight(BaseColor.WHITE);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		cell.setColspan(4);
		cell.setPaddingTop(20);
		table3.addCell(cell);

		
		cell = new PdfPCell(new Phrase((Messages.get(
				new Lang(Lang.forCode("en")), "Domain")), tableHeading));
		cell.setPaddingLeft(5);
		cell.setVerticalAlignment(Element.ALIGN_LEFT);
		table3.addCell(cell);

		cell = new PdfPCell(new Phrase("Last Month", tableHeading));
		cell.setVerticalAlignment(Element.ALIGN_RIGHT);
		table3.addCell(cell);
		
		
		cell = new PdfPCell(new Phrase("Current Month", tableHeading));
		cell.setVerticalAlignment(Element.ALIGN_RIGHT);
		table3.addCell(cell);
			
		cell = new PdfPCell(new Phrase("% loss", tableHeading));
		cell.setVerticalAlignment(Element.ALIGN_RIGHT);
		table3.addCell(cell);
		
		for (VariationDetails  vr: mailVariastion.variationDetails) {
			cell = new PdfPCell(new Phrase((Messages.get(
					new Lang(Lang.forCode("en")), vr.domain)),
					fontChinese));
			cell.setPaddingLeft(5);
			cell.setVerticalAlignment(Element.ALIGN_LEFT);
			table3.addCell(cell);

			cell = new PdfPCell(new Phrase(String.valueOf(vr.lastMonthCount), fontChinese));
			cell.setVerticalAlignment(Element.ALIGN_RIGHT);
			table3.addCell(cell);
			
			cell = new PdfPCell(new Phrase(String.valueOf(vr.currentMonthCount), fontChinese));
			cell.setVerticalAlignment(Element.ALIGN_RIGHT);
			table3.addCell(cell);
			
			cell = new PdfPCell(new Phrase(String.valueOf(vr.lossPercent), fontChinese));
			cell.setVerticalAlignment(Element.ALIGN_RIGHT);
			table3.addCell(cell);
		}

		document.add(table);
		document.add(table2);
		document.add(table3);
		paragraph = new Paragraph(" ");
		paragraph.setAlignment(Element.ALIGN_CENTER);
		document.add(paragraph);

	}

}
