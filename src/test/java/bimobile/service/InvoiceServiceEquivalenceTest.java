package bimobile.sevice;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import bimobile.model.Invoice;

class InvoiceServiceEquivalenceTest {


    //Äquivalenzklasse 1: gefahrene Kilometer wie in der Rechnungserstellung -> gültig
    @Test
    void testKilometerValidClass() {
        Invoice invoice = new Invoice();
        invoice.setKilometersBefore(1000);
        invoice.setKilometersAfter(1200);

        int diff = invoice.getKilometersAfter() - invoice.getKilometersBefore();
        assertEquals(200, diff);
    }

    //Äquivalenzklasse 2: Bruttobetrag entspricht dem 1,19fachen des Nettobetrags -> gültig
    @Test
    void testPriceValidClass() {
        Invoice invoice = new Invoice();
        invoice.setNetAmount(100.0);
        invoice.setTaxAmount(19.0);
        invoice.setGrossAmount(119.0);

        assertEquals(119.0, invoice.getGrossAmount(), 0.01);
    }
}