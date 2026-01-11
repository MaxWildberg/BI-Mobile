package bimobile.service;

import bimobile.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Geschäftslogik zur Erstellung der Mail, die die PDF enthält.
 * Verantwortlichkeiten:
 * - Setzen der Mailversand-Parameter
 * - Wiedergabe der Fahrzeugdaten
 * - Attachment des PDF-Dokuments
 * @author Leonard Köchling
 */

@Service
public class MailService {

    private final JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String mailForm;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvoiceMail(Invoice invoice, String to, byte[] pdfData, String filename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(mailForm);
            helper.setTo(to);
            helper.setSubject("Ihre BI-Mobile Rechnung");
            String htmlText =
                    "Guten Tag,<br><br>" +
                            "anbei finden Sie Ihre Rechnung für das Fahrzeug:<br>" +
                            "<b>" + invoice.getVehicle().getBrand() + " "
                            + invoice.getVehicle().getModel() + " ("
                            + invoice.getVehicle().getLicensePlate() + ")</b><br><br>" +
                            "Mit freundlichen Grüßen,<br>" +
                            "BI-Mobile";
            helper.setText(htmlText, true);

            helper.addAttachment(filename, new ByteArrayResource(pdfData));

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Email-Versand fehlgeschlagen", e);
        }
    }

}


