package bimobile.views;

import bimobile.model.Customer;
import bimobile.model.Facility;
import bimobile.model.Vehicle;
import bimobile.service.CustomerService;
import bimobile.service.FacilityService;
import bimobile.service.RentalService;
import bimobile.service.VehicleService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.function.Consumer;

public class RentalCreateDialog extends Dialog {

    public RentalCreateDialog(CustomerService customerService,
                              VehicleService vehicleService,
                              FacilityService facilityService,
                              RentalService rentalService,
                              Runnable refreshCallback) {

        setWidth("550px");
        setDraggable(true);

        H3 title = new H3("Neue Ausleihe erstellen");

        ComboBox<Customer> customerBox = new ComboBox<>("Kunde");
        customerBox.setItems(customerService.findAll());
        customerBox.setItemLabelGenerator(Customer::getFullName);

        ComboBox<Vehicle> vehicleBox = new ComboBox<>("Fahrzeug");
        vehicleBox.setItems(vehicleService.findAll());
        vehicleBox.setItemLabelGenerator(v -> v.getLicensePlate() + " — " + v.getModel());

        ComboBox<Facility> facilityBox = new ComboBox<>("Standort");
        facilityBox.setItems(facilityService.findAll());
        facilityBox.setItemLabelGenerator(Facility::getName);

        DatePicker start = new DatePicker("Startdatum");
        DatePicker end = new DatePicker("Enddatum");

        Button create = new Button("Speichern", e -> {
            try {
                rentalService.createRental(
                        customerBox.getValue(),
                        vehicleBox.getValue(),
                        facilityBox.getValue(),
                        start.getValue(),
                        end.getValue()
                );

                Notification.show("Ausleihe erfolgreich erstellt!");
                refreshCallback.run();
                close();

            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage());
            }
        });

        Button cancel = new Button("Abbrechen", e -> close());

        HorizontalLayout buttons = new HorizontalLayout(create, cancel);

        FormLayout form = new FormLayout(
                customerBox, vehicleBox, facilityBox, start, end
        );

        VerticalLayout layout = new VerticalLayout(title, form, buttons);
        add(layout);
    }
}
