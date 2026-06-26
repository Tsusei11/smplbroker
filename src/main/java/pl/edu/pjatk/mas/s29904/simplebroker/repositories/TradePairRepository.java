package pl.edu.pjatk.mas.s29904.simplebroker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.TradePair;

import java.util.Optional;

@Repository
public interface TradePairRepository extends JpaRepository<TradePair, Long> {
    Optional<TradePair> findByAssetIdAndCurrencyId(Long assetId, Long currencyId);
}
