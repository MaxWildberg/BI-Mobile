package bimobile.dao;

import bimobile.model.Rental;
import bimobile.model.RentalChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalChangeLogRepository extends JpaRepository<RentalChangeLog, Long> {

    List<RentalChangeLog> findAllByOrderByTimestampDesc();

    List<RentalChangeLog> findByRental(Rental rental);
}
