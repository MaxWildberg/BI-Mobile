package bimobile.views.rentals.dialogs;

import bimobile.model.Customer;
import bimobile.model.Facility;
import bimobile.model.Rental;
import bimobile.model.Vehicle;
import bimobile.model.customer.BusinessCustomer;
import bimobile.model.customer.PrivateCustomer;
import bimobile.service.CompanyService;
import bimobile.service.CustomerService;
import bimobile.service.FacilityService;
import bimobile.service.RentalService;
import bimobile.service.VehicleService;
import bimobile.views.customer.CustomerTypeSelectionDialog;
import bimobile.views.customer.EditCreateCustomerDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dialog zum Anlegen einer neuen Ausleihe.
 * <p>
 * Ich halte hier alle Schritte zusammen, die man für eine neue Ausleihe braucht,
 * damit der Überblick im UI klar bleibt. Als Ben Berlin (Student) ist mir wichtig,
 * dass die Eingaben klar geführt werden und sofort Rückmeldungen kommen.
 *
 * @author Ben Berlin
 */
public class RentalCreateDialog extends Dialog {

    private final RentalService rentalService;
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final FacilityService facilityService;
    private final CompanyService companyService;
    private final Facility currentFacility;
    private final Runnable onCustomerSaved;
    private final Consumer<Rental> onRentalSaved;

    /**
     * Baut den Dialog inklusive Formfeldern und Logik auf.
     *
     * @param rentalService    Service zum Anlegen der Ausleihe
     * @param customerService  Service für Kundendaten
     * @param vehicleService   Service für Fahrzeugzugriffe
     * @param facilityService  Service für Standorte
     * @param companyService   Service für Firmenkunden
     * @param currentFacility  aktueller Standort des Users (kann null sein)
     * @param onCustomerSaved  Callback nach dem Speichern eines Kunden
     * @param onRentalSaved    Callback nach dem Speichern einer Ausleihe
     */
    public RentalCreateDialog(RentalService rentalService,
                              CustomerService customerService,
                              VehicleService vehicleService,
                              FacilityService facilityService,
                              CompanyService companyService,
                              Facility currentFacility,
                              Runnable onCustomerSaved,
                              Consumer<Rental> onRentalSaved) {
        this.rentalService = rentalService;
        this.customerService = customerService;
        this.vehicleService = vehicleService;
        this.facilityService = facilityService;
        this.companyService = companyService;
        this.currentFacility = currentFacility;
        this.onCustomerSaved = onCustomerSaved;
        this.onRentalSaved = onRentalSaved;

        setWidth("600px");
        setModal(true);
        setDraggable(true);

        buildLayout();
    }

    private void buildLayout() {
        H3 dialogTitle = new H3("Neue Ausleihe anlegen");

        ComboBox<Customer> customerBox = new ComboBox<>("Kunde");
        customerBox.setItems(customerService.findAllCustomers());
        customerBox.setItemLabelGenerator(Customer::getFullName);

        Button createCustomerButton = new Button("Neuen Kunden anlegen", VaadinIcon.USER_CARD.create());
        createCustomerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        createCustomerButton.addClickListener(e -> openCustomerTypeDialog(customerBox));

        HorizontalLayout customerSelection = new HorizontalLayout(customerBox, createCustomerButton);
        customerSelection.setAlignItems(Alignment.END);

        ComboBox<Vehicle> vehicleBox = new ComboBox<>("Fahrzeug");
        // Als Student halte ich das Matching sauber: Standort beeinflusst Fahrzeugliste.
        if (currentFacility != null) {
            vehicleBox.setItems(vehicleService.getVehiclesByFacility(currentFacility.getId()));
        } else {
            vehicleBox.setItems(vehicleService.findAllVehicles());
        }
        vehicleBox.setItemLabelGenerator(v -> v.getLicensePlate() + " – " + v.getModel());

        ComboBox<Facility> facilityBox = new ComboBox<>("Standort");
        facilityBox.setItemLabelGenerator(Facility::getAddress);

        if (currentFacility != null) {
            facilityBox.setItems(currentFacility);
            facilityBox.setValue(currentFacility);
            facilityBox.setReadOnly(true);
        } else {
            facilityBox.setItems(facilityService.getAllFacilities());
            facilityBox.setReadOnly(false);
        }

        if (currentFacility == null) {
            facilityBox.addValueChangeListener(event -> {
                Facility selectedFacility = event.getValue();
                Vehicle currentlySelectedVehicle = vehicleBox.getValue();

                if (selectedFacility != null) {
                    List<Vehicle> filteredVehicles = vehicleService.getVehiclesByFacility(selectedFacility.getId());
                    vehicleBox.setItems(filteredVehicles);

                    if (currentlySelectedVehicle != null
                            && currentlySelectedVehicle.getFacility() != null
                            && currentlySelectedVehicle.getFacility().getId().equals(selectedFacility.getId())) {
                        vehicleBox.setValue(currentlySelectedVehicle);
                    } else {
                        vehicleBox.clear();
                        if (currentlySelectedVehicle != null) {
                            Notification.show("Fahrzeugauswahl zurückgesetzt (passt nicht zum Standort).")
                                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                        }
                    }
                } else {
                    vehicleBox.setItems(vehicleService.findAllVehicles());
                    vehicleBox.setValue(currentlySelectedVehicle);
                }
            });

            vehicleBox.addValueChangeListener(event -> {
                Vehicle selectedVehicle = event.getValue();
                if (selectedVehicle != null && selectedVehicle.getFacility() != null) {
                    if (!selectedVehicle.getFacility().equals(facilityBox.getValue())) {
                        facilityBox.setValue(selectedVehicle.getFacility());
                    }
                }
            });
        }

        DatePicker startDate = new DatePicker("Startdatum");
        DatePicker endDate = new DatePicker("Enddatum");

        TextField totalRateField = new TextField("Gesamtrate (Vorschau) €");
        totalRateField.setReadOnly(true);

        Runnable recalcTotal = () -> {
            Vehicle v = vehicleBox.getValue();
            LocalDate start = startDate.getValue();
            LocalDate end = endDate.getValue();
            if (v != null && start != null && end != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                if (days <= 0) {
                    days = 1;
                }
                double dailyRate = v.getDailyRate();
                totalRateField.setValue(String.valueOf(dailyRate * days));
            } else {
                totalRateField.clear();
            }
        };

        vehicleBox.addValueChangeListener(e -> recalcTotal.run());
        startDate.addValueChangeListener(e -> recalcTotal.run());
        endDate.addValueChangeListener(e -> recalcTotal.run());

        enforceDateOrder(startDate, endDate);

        Button save = new Button("Speichern", e -> {
            try {
                if (customerBox.isEmpty() || vehicleBox.isEmpty() || startDate.isEmpty() || endDate.isEmpty()
                        || facilityBox.isEmpty()) {
                    Notification.show("Bitte mindestens Kunde, Fahrzeug, Standort, Start- und Enddatum angeben.")
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                Rental rental = rentalService.createRental(
                        customerBox.getValue(),
                        vehicleBox.getValue(),
                        facilityBox.getValue(),
                        startDate.getValue(),
                        endDate.getValue()
                );

                Notification.show("Ausleihe #" + rental.getId() + " erfolgreich erstellt.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                onRentalSaved.accept(rental);
                close();

            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Abbrechen", e -> close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        FormLayout form = new FormLayout(customerSelection, vehicleBox, facilityBox, startDate, endDate, totalRateField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        form.setColspan(customerSelection, 2);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout layout = new VerticalLayout(dialogTitle, form, actions);
        add(layout);
    }

    /**
     * Öffnet die Kundentyp-Auswahl und danach den passenden Kundendialog.
     * <p>
     * Ich halte die Auswahl bewusst schlank, damit neue Kunden schnell erfasst werden können.
     *
     * @param customerBox ComboBox, die nach dem Speichern gefüllt wird
     */
    private void openCustomerTypeDialog(ComboBox<Customer> customerBox) {
        CustomerTypeSelectionDialog typeSelectionDialog = new CustomerTypeSelectionDialog(type -> {
            Customer customer;
            switch (type) {
                case PRIVATE -> customer = new PrivateCustomer();
                case BUSINESS -> customer = new BusinessCustomer();
                default -> throw new IllegalStateException("Unexpected value: " + type);
            }

            EditCreateCustomerDialog dialog = new EditCreateCustomerDialog(
                    customer,
                    false,
                    customerService,
                    companyService,
                    onCustomerSaved,
                    savedCustomer -> {
                        customerBox.getListDataView().addItem(savedCustomer);
                        customerBox.setValue(savedCustomer);
                    }
            );
            dialog.open();
        });
        typeSelectionDialog.open();
    }

    /**
     * Hält die Datumsauswahl konsistent, damit Enddatum nie vor dem Start liegt.
     *
     * @param startDate Startdatum
     * @param endDate   Enddatum
     */
    private void enforceDateOrder(DatePicker startDate, DatePicker endDate) {
        endDate.setMin(startDate.getValue());
        startDate.addValueChangeListener(event -> {
            LocalDate start = event.getValue();
            endDate.setMin(start);
            if (start != null && endDate.getValue() != null && endDate.getValue().isBefore(start)) {
                endDate.clear();
            }
        });
    }
}
