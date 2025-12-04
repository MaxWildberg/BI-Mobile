import bimobile.dao.EmployeeDAO;
import bimobile.dao.FacilityDAO;
import bimobile.model.Employee;
import bimobile.model.Facility;

public class Main {
    public static void main(String[] args) {
        EmployeeDAO employeeDAO = new EmployeeDAO();
        FacilityDAO facilityDAO = new FacilityDAO();

        // Create a facility
        Facility bielefeld = new Facility("Unter den Linden 1", "bielefeld@mail", "010122");
        facilityDAO.addFacility(bielefeld);

        // Create employees and assign them to the facility
        Employee max = new Employee("Max", "Mustermann", "01.01.2000");
        bielefeld.addEmployee(max);

        Employee marianne = new Employee("Marianne", "Musterfrau", "01.01.2000");
        bielefeld.addEmployee(marianne);

        // Persist employees (Facility cascades should save them automatically)
        facilityDAO.updateFacility(bielefeld);

        // Fetch all employees and print their facility info
        employeeDAO.getAllEmployees().forEach(e ->
                System.out.println(
                        e.getName() + " " + e.getLastname() + " - " + e.getBirthday() +
                                " | Facility: " + (e.getFacility() != null ? e.getFacility().getAddress() : "None")
                )
        );

        // Close DAOs
        employeeDAO.close();
    }
}
