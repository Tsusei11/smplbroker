package pl.edu.pjatk.mas.s29904.simplebroker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.pjatk.mas.s29904.simplebroker.models.assets.Asset;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    @Query("SELECT a FROM Asset a WHERE a.available = true ORDER BY a.ticker ASC")
    List<Asset> findAllAvailableAssets();

    @Query("SELECT a FROM Asset a WHERE a.available = true AND " +
            "(LOWER(a.ticker) LIKE :pattern OR LOWER(a.name) LIKE :pattern)")
    List<Asset> searchAvailableAssets(@Param("pattern") String pattern);
}
