package bimobile.service;

import bimobile.model.Invoice;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/**
 * Geschäftslogik zur Konfiguration dem PDF.

 * @author Leonard Köchling
 */

@Service
public class PdfGeneratorService {

	/** Befüllt das PDF mit den Daten als Stream-Objekt
	 *
	 * @param invoice zum Übergeben der in Invoice-Service befüllten Daten an diese Methode
	 * @return konvertiert den Stream-Objekt-Buffer in ein Byte-Array, damit es attached werden kann
	 */

	public byte[] generateInvoicePdf(Invoice invoice) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

			Document document = new Document();
			PdfWriter.getInstance(document, baos);

			document.open();

			document.add(new Paragraph("Rechnung Nr: " + invoice.getId()));
			document.add(new Paragraph("Datum: " + invoice.getInvoiceDate()));
			document.add(new Paragraph("----------------------------"));
			document.add(new Paragraph("Kunde: " + invoice.getRental().getCustomer().getPersonalData().getFullname()));
			document.add(new Paragraph("Email: " + invoice.getRental().getCustomer().getContactInfo().getMail()));

			document.add(new Paragraph("----------------------------"));
			document.add(new Paragraph("Fahrzeug:" + invoice.getVehicle().getBrand() + " " + invoice.getVehicle().getModel()));
			document.add(new Paragraph("kennzeichen: " + invoice.getVehicle().getLicensePlate()));

			document.add(new Paragraph("----------------------------"));
			document.add(new Paragraph("Leihdauer: "
					+ invoice.getRental().getStartDate() + " bis " + invoice.getRental().getEndDate()));
			document.add(new Paragraph("Tagespreis: " + invoice.getRental().pullDailyRateFromVehicle() + " €"));
			document.add(new Paragraph("gefahrene Kilometer: " + (invoice.getKilometersAfter() - invoice.getKilometersBefore()) + " km "));

			document.add(new Paragraph("----------------------------"));
			document.add(new Paragraph("Netto: " + invoice.getNetAmount() + " €"));
			document.add(new Paragraph("MwSt:  " + invoice.getTaxAmount() + " €"));
			document.add(new Paragraph("Brutto: " + invoice.getGrossAmount() + " €"));

			document.close();

			return baos.toByteArray();

		} catch (Exception e) {
			throw new RuntimeException("Error generating PDF", e);
		}
	}
}
