package bimobile.service;

import bimobile.dao.FacilityDAO;
import bimobile.model.Facility;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class FacilityService {

    private final FacilityDAO facilityDAO;

	public FacilityService(FacilityDAO facilityDAO){
		this.facilityDAO = facilityDAO;
	}

    public void addFacility(Facility facility){
        facilityDAO.addFacility(facility);
    }

    public List<Facility> getAllFacilities(){
        return facilityDAO.getAllFacilities();
    }

}