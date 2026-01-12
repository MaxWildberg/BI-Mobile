package bimobile.service.customer;

import bimobile.dao.CompanyRepository;
import bimobile.model.customer.Company;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service zur Verwaltung von Firmenobjekten.
 * Bietet Funktionen zum Abrufen, Speichern und Löschen von Firmen.
 * Stellt sicher, dass Duplikate und ungültige Operationen abgefangen werden.
 *
 * @author Max Wildberg
 */
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * Liefert alle Firmen aus der Datenbank zurück.
     * @return Liste aller Firmen
     */
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    /**
     * Liefert eine Firma anhand ihrer ID.
     * @param companyId ID der Firma
     * @return Gefundene Firma
     * @throws IllegalArgumentException bei null oder ungültiger ID
     * @throws CompanyNotFoundException wenn keine Firma mit dieser ID existiert
     */
    public Company getCompanyById(Long companyId) {
        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("Ungültige Firmen-ID");
        }
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));
    }

    /**
     * Speichert eine neue Firma in der Datenbank.
     * Prüft auf Namensduplikate.
     * @param company Zu speichernde Firma
     * @return Gespeicherte Firma
     * @throws DuplicateCompanyException wenn der Firmenname bereits existiert
     */
    public Company saveCompany(Company company) {
        String name = company.getName();

        if (name != null && companyRepository.existsByName(name)) {
            Notification.show("Firma mit dem Namen " + name + " existiert bereits");
            throw new DuplicateCompanyException(name);
        }

        return companyRepository.save(company);
    }

    /**
     * Löscht eine Firma anhand der ID.
     * Es dürfen keine Mitarbeiter mehr der Firma zugeordnet sein.
     * @param companyId ID der zu löschenden Firma
     * @throws IllegalArgumentException bei null oder ungültiger ID
     * @throws CompanyNotFoundException wenn die Firma nicht existiert
     * @throws IllegalStateException wenn der Firma noch Mitarbeiter zugeordnet sind
     */
    @Transactional
    public void deleteCompany(Long companyId) {
        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("Ungültige Firmen-ID");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));

        if (!company.getEmployees().isEmpty()) {
            throw new IllegalStateException(
                    "Firma kann nicht gelöscht werden: Es sind noch Mitarbeiter zugeordnet"
            );
        }

        companyRepository.delete(company);
    }

    public Company updateCompany(Company updated) {
        if (updated == null) {
            throw new InvalidDataException("Zu aktualisierende Firma darf nicht null sein");
        }
        if (updated.getCompanyId() == null) {
            throw new InvalidDataException("Firmen-ID fehlt für update");
        }

        Company existing = companyRepository.findById(updated.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException(updated.getCompanyId()));

        existing.setName(updated.getName());
        existing.setAddress(updated.getAddress());

        companyRepository.save(existing);
        return existing;
    }
}
