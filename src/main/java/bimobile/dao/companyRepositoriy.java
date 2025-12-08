package bimobile.dao;

import bimobile.model.customer.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface companyRepositoriy extends JpaRepository<Company, Long> {

    Company getCompanyByCompanyId(Long companyId);

    boolean existsByName(String name);
}
