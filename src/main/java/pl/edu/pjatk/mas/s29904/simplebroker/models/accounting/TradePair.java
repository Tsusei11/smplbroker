package pl.edu.pjatk.mas.s29904.simplebroker.models.accounting;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.util.Assert;
import pl.edu.pjatk.mas.s29904.simplebroker.models.assets.Asset;
import pl.edu.pjatk.mas.s29904.simplebroker.models.orders.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradePair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "Current rate cannot be null")
    @DecimalMin(value = "0.0", message = "Current rate must be positive")
    @Column(nullable = false)
    private BigDecimal currentRate;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false, updatable = false)
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false, updatable = false)
    private Asset asset;

    @OneToMany(
            mappedBy = "pair",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Order> orders = new HashSet<>();

    private TradePair(BigDecimal currentRate, Currency currency, Asset asset) {
        this.currentRate = currentRate;

        this.currency = currency;
        currency.addPair(this);

        this.asset = asset;
        asset.addPair(this);
    }

    public static TradePair create(BigDecimal currentRate, Currency currency, Asset asset) {
        Assert.notNull(currentRate, "Current rate cannot be null");
        Assert.isTrue(currentRate.compareTo(BigDecimal.ZERO) > 0, "Current rate must be positive");
        Assert.notNull(currency, "Currency cannot be null");
        Assert.notNull(asset, "Asset cannot be null");

        return new TradePair(currentRate, currency, asset);
    }

    public void remove(){
        if (currency != null){
            Currency tempCurrency = currency;
            currency = null;
            tempCurrency.removePair(this);
        }

        if (asset != null){
            Asset tempAsset = asset;
            asset = null;
            tempAsset.removePair(this);
        }

        if (orders != null && !orders.isEmpty()){
            Set<Order> tempOrders = new HashSet<>(orders);
            for (Order order : tempOrders){
                order.remove();
            }
        }
    }

    // RELATIONSHIP METHODS

    public void addOrder(Order order){
        Assert.notNull(order, "Order cannot be null");
        Assert.isTrue(order.getPair() == this, "Order is assigned to another pair");

        orders.add(order);
    }

    public void removeOrder(Order order){
        Assert.notNull(order, "Order cannot be null");

        if (orders.contains(order)) {
            orders.remove(order);
            order.remove();
        }
    }

    // GETTERS

    public Set<Order> getOrders() {
        return Collections.unmodifiableSet(orders);
    }

    // SETTERS

    public void setCurrentRate(BigDecimal currentRate) {
        Assert.notNull(currentRate, "Current rate cannot be null");
        this.currentRate = currentRate;
        lastUpdatedAt = LocalDateTime.now();
    }
}
