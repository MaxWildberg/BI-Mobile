package bimobile.service;

import bimobile.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;


@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvoiceMail(Invoice invoice, String to, byte[] pdfData, String filename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("leonard.koechling@hsbi.de");
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

    /* nur zu Testzwecken
    private String buildMailBody(Invoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sehr geehrte/r ").append(invoice.getRental().getCustomer().getSalutation()).append(",\n\n");
        sb.append("anbei erhalten Sie die Rechnung Nr. ").append(invoice.getId()).append(" für Ihre Fahrzeugmiete.\n\n");
        sb.append("Rechnungsbetrag (brutto): ").append(String.format("%.2f", invoice.getGrossAmount())).append(" EUR\n\n");
        sb.append("Mit freundlichen Grüßen\nBI-Mobile");
        return sb.toString();
    } */

    /* @PostConstruct
    public void testMail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("leonard.koechling@hsbi.de");
            message.setTo("leonard.koechling@hotmail.com");
            message.setSubject("Test – HSBI SMTP");
            message.setText("Der Mailserver funzt.");
            mailSender.send(message);
            System.out.println("Mail wurde verschickt!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    } */
}


