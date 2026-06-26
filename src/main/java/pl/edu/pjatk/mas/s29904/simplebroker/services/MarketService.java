package pl.edu.pjatk.mas.s29904.simplebroker.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.TradePair;
import pl.edu.pjatk.mas.s29904.simplebroker.models.assets.Asset;
import pl.edu.pjatk.mas.s29904.simplebroker.models.enums.OrderDirection;
import pl.edu.pjatk.mas.s29904.simplebroker.models.orders.MarketOrder;
import pl.edu.pjatk.mas.s29904.simplebroker.repositories.AssetRepository;
import pl.edu.pjatk.mas.s29904.simplebroker.repositories.TradePairRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarketService {
    private final AssetRepository assetRepository;
    private final TradePairRepository tradePairRepository;

    public List<Asset> getAvailableAssets(String searchQuery){
        if (searchQuery != null && !searchQuery.trim().isEmpty()){
            String pattern = "%" + searchQuery.trim().toLowerCase() + "%";
            return assetRepository.searchAvailableAssets(pattern);
        }

        return assetRepository.findAllAvailableAssets();
    }

    public Asset getAssetDetails(Long assetId){
        return assetRepository.findById(assetId).orElseThrow(
                () -> new IllegalArgumentException("Asset with id " + assetId + " not found")
        );
    }

    public Map<Long, BigDecimal> getAssetPrices(List<Asset> assets){
        List<TradePair> tradePairs = tradePairRepository.findAll();
        Map<Long, BigDecimal> assetPrices = new HashMap<>();

        for (Asset asset : assets){
            tradePairs.stream()
                    .filter(p -> p.getAsset().getId().equals(asset.getId()))
                    .findFirst()
                    .ifPresent(p -> assetPrices.put(asset.getId(), p.getCurrentRate()));
        }

        return assetPrices;
    }

    public BigDecimal getCurrentRateForAsset(Long assetId) {
        return tradePairRepository.findAll().stream()
                .filter(p -> p.getAsset().getId().equals(assetId))
                .map(TradePair::getCurrentRate)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getEstimatedPriceForAssetAmount(Long assetId, BigDecimal amount, OrderDirection direction) {
        BigDecimal currentRate = getCurrentRateForAsset(assetId);
        BigDecimal estimatedPrice = amount.multiply(currentRate);
        BigDecimal fee = amount.multiply(currentRate).multiply(MarketOrder.getServiceFee());
        if (direction == OrderDirection.SELL) {
            estimatedPrice = estimatedPrice.subtract(fee);
        } else {
            estimatedPrice = estimatedPrice.add(fee);
        }

        return estimatedPrice.setScale(2, RoundingMode.HALF_UP);
    }
}
