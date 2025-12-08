package bimobile.service;

import bimobile.dao.InvoiceRepository;
import bimobile.dao.RentalRepository;
import bimobile.model.Invoice;
import bimobile.model.Rental;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Geschäftslogik zur Erstellung der Rechnung nach Abschluss der Ausleihe.
 * Verantwortlichkeiten:
 * - Berechnung der Preise und befüllen der Invoice Attribute
 * - Übergabe der aus PdfGeneratorService erstellten PDF in Variable pdf der Klasse byte[]
 * - Übergabe der pdf an die Mail-Methode
 * - Erstellung des PDF-Namens
 * - Aktivierung des MailService
 * @author Leonard Köchling
 */

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final MailService mailService;
    private final RentalRepository rentalRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          PdfGeneratorService pdfGeneratorService,
                          MailService mailService,
                          RentalRepository rentalRepository) {
        this.invoiceRepository = invoiceRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.mailService = mailService;
        this.rentalRepository = rentalRepository;
    }

    /**
     * @param rental aus confirmCarReturn und der Rückgabemethode aus der View
     * Speichert die Invoice in der Datenbank
     * @return invoice
     */
    @Transactional
    public Invoice createInvoiceForRental(Rental rental) {

        Rental loaded = rentalRepository.findByIdWithAllAttributes(rental.getId());
        Invoice invoice = new Invoice();
        invoice.setRental(loaded);
        invoice.setInvoiceDate(LocalDateTime.now());

        double netto = loaded.getDailyRate() *
                (loaded.getStartDate().until(loaded.getEndDate()).getDays());

        double tax = netto * 0.19;
        double gross = netto + tax;

        invoice.setNetAmount(netto);
        invoice.setTaxAmount(tax);
        invoice.setGrossAmount(gross);
        invoice.setVehicle(loaded.getVehicle());

        invoiceRepository.save(invoice);

        // PDF erzeugen
        byte[] pdf = pdfGeneratorService.generateInvoicePdf(invoice);

        // Mail verschicken
        mailService.sendInvoiceMail(
                invoice,
                loaded.getCustomer().getContactInfo().getMail(),
                pdf,
                "Rechnung-" + invoice.getId() + ".pdf"
        );

        return invoice;
    }


    /**
     * Kreiert Rental-Daten für die createInvocieForRental-Methode
     *
     * @param rentalId übergebene Rental, die gerade abgeschlossen wurde
     */
    @Transactional
    public void confirmCarReturn(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow();
        rentalRepository.save(rental);
        createInvoiceForRental(rental);
    }

}
