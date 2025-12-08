package bimobile.views.customer;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.function.Consumer;

public class CustomerTypeSelectionDialog extends Dialog {

    public enum CustomerType { PRIVATE, BUSINESS }

    public CustomerTypeSelectionDialog(Consumer<CustomerType> onTypeSelected) {
        setHeaderTitle("Kundentyp auswählen");

        ComboBox<CustomerType> typeComboBox = new ComboBox<>("Kundentyp");
        typeComboBox.setItems(CustomerType.values());
        typeComboBox.setPlaceholder("Bitte wählen...");
        typeComboBox.setAllowCustomValue(false);

        // Callback auslösen, wenn eine Auswahl getroffen wird
        typeComboBox.addValueChangeListener(event -> {
            CustomerType selected = event.getValue();
            System.out.println("Selected type: " + selected);
            if (selected != null) {
                onTypeSelected.accept(selected);
                close(); // Dialog schließen
            }
        });

        VerticalLayout layout = new VerticalLayout(typeComboBox);
        layout.setPadding(true);
        add(layout);
    }
}
