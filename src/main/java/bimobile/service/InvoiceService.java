package bimobile.service;

import bimobile.dao.InvoiceRepository;
import bimobile.dao.RentalRepository;
import bimobile.model.Invoice;
import bimobile.model.Rental;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    @Transactional
    public Invoice createInvoiceForRental(Rental rental) {

        Invoice invoice = new Invoice();
        invoice.setRental(rental);
        invoice.setInvoiceDate(LocalDateTime.now());

        double netto = rental.getDailyRate() *
                (rental.getStartDate().until(rental.getEndDate()).getDays());

        double kmExtra = Math.max(0, rental.getKilometersAfter() - rental.getKilometersBefore() - 100);
        double kmFee = kmExtra * 0.20;

        double nettoTotal = netto + kmFee;
        double tax = nettoTotal * 0.19;
        double gross = nettoTotal + tax;

        invoice.setNetAmount(nettoTotal);
        invoice.setTaxAmount(tax);
        invoice.setGrossAmount(gross);
        invoice.setVehicle(rental.getVehicle());

        invoiceRepository.save(invoice);

        // PDF erzeugen
        byte[] pdf = pdfGeneratorService.generateInvoicePdf(invoice);

        // Mail verschicken
        mailService.sendInvoiceMail(
                invoice,
                rental.getCustomer().getEmail(),
                pdf,
                "Rechnung-" + invoice.getId() + ".pdf"
        );

        return invoice;
    }


    public void confirmCarReturn(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow();
        rental.setReturned(true);
        rentalRepository.save(rental);
        createInvoiceForRental(rental);
    }

}
