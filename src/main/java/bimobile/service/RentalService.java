package bimobile.service;

import bimobile.model.Customer;
import bimobile.model.Facility;
import bimobile.model.Rental;
import bimobile.model.Vehicle;
import bimobile.repository.RentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Service-Klasse für den Ausleihprozess.
 *
 * Enthält die komplette Geschäftslogik:
 * - Verfügbarkeitsprüfung
 * - Preisberechnung
 * - Anlegen einer Ausleihe
 * - Erzeugen einer Rechnung (InvoiceService)
 * - Versand per E-Mail (EmailService)
 *
 * Entwickelt von Ben Berlin für das Projekt BI-Mobile.
 */
@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final InvoiceService invoiceService;
    private final EmailService emailService;

    public RentalService(RentalRepository rentalRepository,
                         InvoiceService invoiceService,
                         EmailService emailService) {
        this.rentalRepository = rentalRepository;
        this.invoiceService = invoiceService;
        this.emailService = emailService;
    }

    public boolean isVehicleAvailable(Vehicle vehicle, LocalDate start, LocalDate end) {
        return rentalRepository
                .findByVehicleAndEndDateAfterAndStartDateBefore(vehicle, start, end)
                .isEmpty();
    }

    public double calculateTotalPrice(Vehicle vehicle, LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);
        return Math.max(days, 1) * vehicle.getDailyRate();
    }

    @Transactional
    public Rental createRental(Customer customer,
                               Vehicle vehicle,
                               Facility facility,
                               LocalDate start,
                               LocalDate end) {

        if (!isVehicleAvailable(vehicle, start, end)) {
            throw new IllegalArgumentException("Fahrzeug ist im gewählten Zeitraum nicht verfügbar.");
        }

        double totalPrice = calculateTotalPrice(vehicle, start, end);

        Rental rental = new Rental(
                customer,
                vehicle,
                facility,
                start,
                end,
                vehicle.getDailyRate(),
                totalPrice
        );

        rental = rentalRepository.save(rental);

        // Rechnung erzeugen & per Mail senden
        byte[] pdf = invoiceService.generateInvoice(rental);
        emailService.sendInvoice(customer.getEmail(), pdf);

        return rental;
    }
}
