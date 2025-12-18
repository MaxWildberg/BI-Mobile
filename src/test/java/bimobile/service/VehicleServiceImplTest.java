package bimobile.service;

import bimobile.dao.VehicleHistoryRepository;
import bimobile.dao.VehicleRepository;
import bimobile.model.PriceCategory;
import bimobile.model.Vehicle;
import bimobile.model.VehicleHistoryEntry;
import bimobile.model.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleHistoryRepository historyRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    /**
     * Positiver Test: Gültiger Statuswechsel.
     */
    @Test
    void testChangeStatus_ValidTransition_ToAvailable() {
        // ARRANGE
        Long vehicleId = 1L;

        Vehicle mockVehicle = new Vehicle("BI-TEST 1", "Audi", "A4", PriceCategory.SALOON);

        mockVehicle.setStatus(VehicleStatus.IN_MAINTENANCE);

        // Bedingung: HU in der Zukunft
        mockVehicle.setNextInspectionDate(LocalDate.now().plusYears(1));
        // Bedingung: Keine aktive Wartung
        mockVehicle.setMaintenanceActive(false);

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        Vehicle result = vehicleService.changeStatus(vehicleId, VehicleStatus.AVAILABLE, "Wartung abgeschlossen");

        // ASSERT
        assertNotNull(result);
        assertEquals(VehicleStatus.AVAILABLE, result.getStatus());

        verify(vehicleRepository, times(1)).save(mockVehicle);
        verify(historyRepository, times(1)).save(any(VehicleHistoryEntry.class));
    }

    /**
     * Negativer Test: Ungültiger Statuswechsel (HU abgelaufen).
     */
    @Test
    void testChangeStatus_InvalidTransition_OverdueInspection() {
        // ARRANGE
        Long vehicleId = 2L;

        Vehicle mockVehicle = new Vehicle("BI-OLD 99", "Opel", "Corsa", PriceCategory.SALOON);

        mockVehicle.setStatus(VehicleStatus.IN_MAINTENANCE);

        // HU ist abgelaufen
        mockVehicle.setNextInspectionDate(LocalDate.now().minusDays(1));
        mockVehicle.setMaintenanceActive(false);

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));

        // ACT & ASSERT
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            vehicleService.changeStatus(vehicleId, VehicleStatus.AVAILABLE, "Sollte fehlschlagen");
        });

        String msg = exception.getMessage();
        assertTrue(msg.contains("HU") || msg.contains("fällig"),
                "Fehlermeldung sollte auf fällige HU hinweisen.");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }
}