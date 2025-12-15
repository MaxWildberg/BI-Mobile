package bimobile.sevice;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import bimobile.service.PdfGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;
import bimobile.service.MailService;
import bimobile.model.Invoice;

class MailServiceTest {

    //JUnit-Test 1: Mailversand geschieht funktionskonform mit allen Parametern
    @Test
    void testSendInvoiceMail() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage message = new MimeMessage((jakarta.mail.Session) null);
        when(mailSender.createMimeMessage()).thenReturn(message);

        MailService mailService = new MailService(mailSender);

        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setVehicle(null); // Vehicle könnte hier gemockt werden

        assertDoesNotThrow(() ->
                mailService.sendInvoiceMail(invoice, "test@example.com", new byte[]{1,2,3}, "Rechnung.pdf")
        );
    }

    //JUnit-Test 2: PDF wird ordnungsmäßig erstellt
    void testGeneratePdfNotEmpty() {
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setNetAmount(100.0);
        invoice.setTaxAmount(19.0);
        invoice.setGrossAmount(119.0);

        PdfGeneratorService pdfService = new PdfGeneratorService();
        byte[] pdf = pdfService.generateInvoicePdf(invoice);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}