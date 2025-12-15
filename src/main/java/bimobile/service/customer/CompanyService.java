package bimobile.service.customer;

import bimobile.dao.CompanyRepository;
import bimobile.enums.RentalStatus;
import bimobile.model.customer.Company;
import bimobile.model.customer.Customer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyService {
    CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompanyById(Long companyId) {
        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("Ungültige Firmen-ID");
        }
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));
    }

    public Company saveCompany(Company company) {

        String name = company.getName();

        if (name != null && companyRepository.existsByName(name)) {
            throw new DuplicateCompanyException(name);
        }

        return companyRepository.save(company);
    }

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

}
