package pl.edu.pjatk.mas.s29904.simplebroker.models.orders;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.util.Assert;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.TradePair;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.Wallet;
import pl.edu.pjatk.mas.s29904.simplebroker.models.enums.OrderDirection;
import pl.edu.pjatk.mas.s29904.simplebroker.models.enums.OrderStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "order_type", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long number;

    @NotNull(message = "Order direction cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderDirection direction;

    @NotNull(message = "Order amount cannot be null")
    @DecimalMin(value = "0.0", message = "Amount must be positive")
    @Column(nullable = false)
    private BigDecimal amount;

    @DecimalMin(value = "0.0", message = "Final price must be positive")
    @Column(precision = 19, scale = 4)
    private BigDecimal finalPrice;

    @NotNull(message = "Order status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    protected OrderStatus status;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false, updatable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pair_id", nullable = false, updatable = false)
    private TradePair pair;

    protected Order(OrderDirection direction, BigDecimal amount, Wallet wallet, TradePair pair) {
        this.direction = direction;
        this.amount = amount;
        status = OrderStatus.NEW;

        this.wallet = wallet;
        wallet.addOrder(this);

        this.pair = pair;
        pair.addOrder(this);
    }

    public void remove(){
        if (wallet != null){
            Wallet tempWallet = wallet;
            wallet = null;
            tempWallet.removeOrder(this);
        }

        if (pair != null){
            TradePair tempPair = pair;
            pair = null;
            tempPair.removeOrder(this);
        }
    }

    protected void calculate(BigDecimal currentRate, BigDecimal serviceFee){
        BigDecimal totalValue = getAmount().multiply(currentRate);
        BigDecimal fee = totalValue.multiply(serviceFee);

        if (getDirection() == OrderDirection.BUY) {
            BigDecimal totalCost = totalValue.add(fee).setScale(2, RoundingMode.HALF_UP);
            getWallet().withdrawBalance(totalCost);
            getWallet().addAssetAmount(getPair().getAsset(), getAmount());
            finalPrice = totalCost;
        } else {
            BigDecimal totalRevenue = totalValue.subtract(fee).setScale(2, RoundingMode.HALF_UP);
            getWallet().subtractAssetAmount(getPair().getAsset(), getAmount());
            getWallet().topUpBalance(totalRevenue);
            finalPrice = totalRevenue;
        }
    }

    // BUSINESS METHODS

    public abstract void execute();

    // SETTERS

    public void setDirection(OrderDirection direction) {
        if (status != OrderStatus.NEW) {
            throw new UnsupportedOperationException("Operation supported only for orders with status NEW");
        }

        Assert.notNull(direction, "Direction cannot be null");

        this.direction = direction;
    }

    public void setAmount(BigDecimal amount) {
        if (status != OrderStatus.NEW) {
            throw new UnsupportedOperationException("Operation supported only for orders with status NEW");
        }

        Assert.notNull(amount, "Amount cannot be null");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Amount must be positive");

        this.amount = amount;
    }
}

