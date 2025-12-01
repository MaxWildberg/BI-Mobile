package bimobile.service;

import bimobile.model.Rental;
import org.springframework.stereotype.Service;

/**
 * PDF-Erstellung für Rechnungen.
 * In diesem Prototypen lediglich als Platzhalter implementiert.
 */
@Service
public class InvoiceService {

    public byte[] generateInvoice(Rental rental) {
        return ("Rechnung für Ausleihe #" + rental.getId()).getBytes();
    }
}
