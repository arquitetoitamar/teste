package com.estapar.parking.repository;

import com.estapar.parking.model.ParkingSpot;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/sql/init.sql")
class ParkingSpotRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    private ParkingSpot spot1;
    private ParkingSpot spot2;
    private static final String LICENSE_PLATE_1 = "SETUP1234";
    private static final String LICENSE_PLATE_2 = "SETUP5678";

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        entityManager.getEntityManager().createQuery("DELETE FROM ParkingSpot").executeUpdate();
        entityManager.flush();
        
        // Create test parking spots
        spot1 = new ParkingSpot();
        spot1.setSectorId("A1");
        spot1.setLatitude(-23.550520);
        spot1.setLongitude(-46.633308);
        spot1.setLicensePlate(LICENSE_PLATE_1);
        spot1.setEntryTime(LocalDateTime.now());
        spot1.setOccupied(true);
        entityManager.persist(spot1);

        spot2 = new ParkingSpot();
        spot2.setSectorId("A1");
        spot2.setLatitude(-23.550521);
        spot2.setLongitude(-46.633309);
        spot2.setOccupied(false);
        entityManager.persist(spot2);

        entityManager.flush();
    }
    

    @Test
    void findByLicensePlate_ShouldReturnSpotForLicensePlate() {
        Optional<ParkingSpot> spot = parkingSpotRepository.findByLicensePlateAndOccupiedTrue(LICENSE_PLATE_1);
        
        assertThat(spot).isPresent();
        assertThat(spot.get().getId()).isEqualTo(spot1.getId());
    }

    @Test
    void findByLatitudeAndLongitude_ShouldReturnCorrectSpot() {
        Optional<ParkingSpot> spot = parkingSpotRepository.findByLatitudeAndLongitude(
            spot1.getLatitude(), spot1.getLongitude());
        
        assertThat(spot).isPresent();
        assertThat(spot.get().getId()).isEqualTo(spot1.getId());
    }

    @Test
    void findByLicensePlate_ShouldReturnEmptyForNonExistentLicensePlate() {
        Optional<ParkingSpot> spot = parkingSpotRepository.findByLicensePlateAndOccupiedTrue("NONEXISTENT");
        
        assertThat(spot).isEmpty();
    }

    @Test
    void findByLatitudeAndLongitude_ShouldReturnEmptyForNonExistentCoordinates() {
        Optional<ParkingSpot> spot = parkingSpotRepository.findByLatitudeAndLongitude(0.0, 0.0);
        
        assertThat(spot).isEmpty();
    }

    @Test
    void shouldNotAllowTwoVehiclesInSameSpotAtSameTime() {
        // Setup
        String sectorId = "A1";
        entityManager.getEntityManager().createQuery("DELETE FROM ParkingSpot").executeUpdate();
        entityManager.flush();
        
        // Create two spots
        ParkingSpot spot1 = createParkingSpot(sectorId, false, "A1-001");
        ParkingSpot spot2 = createParkingSpot(sectorId, false, "A1-002");
        
        LocalDateTime now = LocalDateTime.now();
        
        entityManager.persist(spot1);
        entityManager.persist(spot2);
        entityManager.flush();
        
        // First vehicle parks
        spot1.setLicensePlate("TEST1234");
        spot1.setOccupied(true);
        spot1.setEntryTime(now);
        entityManager.persist(spot1);
        entityManager.flush();
        
        // Try to park second vehicle at the same time in the same spot
        spot2.setLicensePlate("TEST5678");
        spot2.setOccupied(true);
        spot2.setEntryTime(now);
        spot2.setLatitude(spot1.getLatitude());
        spot2.setLongitude(spot1.getLongitude());
        
        // Verify that an exception is thrown
        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(spot2);
            entityManager.flush();
        });
    }

    @Test
    void shouldAllowTwoVehiclesInSameSpotAtDifferentTimes() {
        // Setup
        String sectorId = "A1";
        entityManager.getEntityManager().createQuery("DELETE FROM ParkingSpot").executeUpdate();
        entityManager.flush();
        
        // Create a spot
        ParkingSpot spot = createParkingSpot(sectorId, false, "A1-001");
        
        LocalDateTime time1 = LocalDateTime.now();
        LocalDateTime time2 = time1.plusHours(1);
        
        entityManager.persist(spot);
        entityManager.flush();
        
        // First vehicle parks
        spot.setLicensePlate("TEST1234");
        spot.setOccupied(true);
        spot.setEntryTime(time1);
        entityManager.persist(spot);
        entityManager.flush();
        
        // First vehicle leaves
        spot.setLicensePlate(null);
        spot.setOccupied(false);
        spot.setEntryTime(null);
        entityManager.persist(spot);
        entityManager.flush();
        
        // Second vehicle parks at a different time
        spot.setLicensePlate("TEST5678");
        spot.setOccupied(true);
        spot.setEntryTime(time2);
        entityManager.persist(spot);
        entityManager.flush();
        
        // Verify that the second vehicle was parked successfully
        ParkingSpot updatedSpot = entityManager.find(ParkingSpot.class, spot.getId());
        assertNotNull(updatedSpot);
        assertEquals("TEST5678", updatedSpot.getLicensePlate());
        assertEquals(time2, updatedSpot.getEntryTime());
    }

    private ParkingSpot createParkingSpot(String sectorId, boolean occupied, String licensePlate) {
        ParkingSpot spot = new ParkingSpot();
        spot.setSectorId(sectorId);
        spot.setOccupied(occupied);
        spot.setLicensePlate(licensePlate);
        spot.setLatitude(-23.561684);
        spot.setLongitude(-46.655981);
        return parkingSpotRepository.save(spot);
    }

    @Test
    void findByLicensePlateAndOccupiedTrue_ShouldReturnSpotForLicensePlate() {
        Optional<ParkingSpot> spot = parkingSpotRepository.findByLicensePlateAndOccupiedTrue(LICENSE_PLATE_1);
        
        assertThat(spot).isPresent();
        assertThat(spot.get().getId()).isEqualTo(spot1.getId());
    }

    @Test
    void findByLicensePlateAndOccupiedTrue_ShouldReturnEmptyForNonExistentLicensePlate() {
        Optional<ParkingSpot> spot = parkingSpotRepository.findByLicensePlateAndOccupiedTrue("NONEXISTENT");
        
        assertThat(spot).isEmpty();
    }
} 