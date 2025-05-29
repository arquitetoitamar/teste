package com.estapar.parking.repository;

import com.estapar.parking.model.GarageSector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class GarageSectorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GarageSectorRepository garageSectorRepository;

    private GarageSector sector1;
    private GarageSector sector2;

    @BeforeEach
    void setUp() {
        // Create test sectors
        sector1 = new GarageSector();
        sector1.setId("A1");
        sector1.setMaxCapacity(10);
        sector1.setCurrentOccupancy(5);
        sector1.setBasePrice(new BigDecimal("10.00"));
        sector1.setOpenHour(LocalTime.of(8, 0));
        sector1.setCloseHour(LocalTime.of(18, 0));
        sector1.setDurationLimitMinutes(120);
        entityManager.persist(sector1);

        sector2 = new GarageSector();
        sector2.setId("B1");
        sector2.setMaxCapacity(20);
        sector2.setCurrentOccupancy(0);
        sector2.setBasePrice(new BigDecimal("15.00"));
        sector2.setOpenHour(LocalTime.of(7, 0));
        sector2.setCloseHour(LocalTime.of(19, 0));
        sector2.setDurationLimitMinutes(180);
        entityManager.persist(sector2);

        entityManager.flush();
    }

    @Test
    void findById_ShouldReturnCorrectSector() {
        Optional<GarageSector> foundSector = garageSectorRepository.findById("A1");
        
        assertThat(foundSector).isPresent();
        assertThat(foundSector.get().getMaxCapacity()).isEqualTo(10);
        assertThat(foundSector.get().getCurrentOccupancy()).isEqualTo(5);
        assertThat(foundSector.get().getOpenHour()).isEqualTo(LocalTime.of(8, 0));
        assertThat(foundSector.get().getCloseHour()).isEqualTo(LocalTime.of(18, 0));
        assertThat(foundSector.get().getDurationLimitMinutes()).isEqualTo(120);
    }

    @Test
    void findById_ShouldReturnEmptyForNonExistentId() {
        Optional<GarageSector> foundSector = garageSectorRepository.findById("NONEXISTENT");
        
        assertThat(foundSector).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllSectors() {
        List<GarageSector> sectors = garageSectorRepository.findAll();
        
        assertThat(sectors).hasSize(2);
        assertThat(sectors).extracting("id").containsExactlyInAnyOrder("A1", "B1");
    }

    @Test
    void save_ShouldPersistNewSector() {
        GarageSector newSector = new GarageSector();
        newSector.setId("C1");
        newSector.setMaxCapacity(15);
        newSector.setCurrentOccupancy(0);
        newSector.setBasePrice(new BigDecimal("12.00"));
        newSector.setOpenHour(LocalTime.of(9, 0));
        newSector.setCloseHour(LocalTime.of(17, 0));
        newSector.setDurationLimitMinutes(150);

        GarageSector savedSector = garageSectorRepository.save(newSector);
        
        assertThat(savedSector.getId()).isEqualTo("C1");
        assertThat(garageSectorRepository.findById("C1")).isPresent();
    }

    @Test
    void save_ShouldUpdateExistingSector() {
        sector1.setCurrentOccupancy(6);
        sector1.setBasePrice(new BigDecimal("11.00"));
        sector1.setOpenHour(LocalTime.of(7, 30));
        sector1.setCloseHour(LocalTime.of(17, 30));
        sector1.setDurationLimitMinutes(90);

        GarageSector updatedSector = garageSectorRepository.save(sector1);
        
        assertThat(updatedSector.getCurrentOccupancy()).isEqualTo(6);
        assertThat(updatedSector.getBasePrice()).isEqualTo(new BigDecimal("11.00"));
        assertThat(updatedSector.getOpenHour()).isEqualTo(LocalTime.of(7, 30));
        assertThat(updatedSector.getCloseHour()).isEqualTo(LocalTime.of(17, 30));
        assertThat(updatedSector.getDurationLimitMinutes()).isEqualTo(90);
        
        Optional<GarageSector> foundSector = garageSectorRepository.findById("A1");
        assertThat(foundSector).isPresent();
        assertThat(foundSector.get().getCurrentOccupancy()).isEqualTo(6);
        assertThat(foundSector.get().getBasePrice()).isEqualTo(new BigDecimal("11.00"));
        assertThat(foundSector.get().getOpenHour()).isEqualTo(LocalTime.of(7, 30));
        assertThat(foundSector.get().getCloseHour()).isEqualTo(LocalTime.of(17, 30));
        assertThat(foundSector.get().getDurationLimitMinutes()).isEqualTo(90);
    }
} 