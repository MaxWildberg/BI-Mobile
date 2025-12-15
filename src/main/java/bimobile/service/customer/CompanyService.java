package bimobile.service.customer;

import bimobile.dao.CompanyRepository;
import bimobile.model.customer.Company;
import org.springframework.stereotype.Service;

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
}
