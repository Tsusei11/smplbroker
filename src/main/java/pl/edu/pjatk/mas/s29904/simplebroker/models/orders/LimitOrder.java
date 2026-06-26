package pl.edu.pjatk.mas.s29904.simplebroker.models.orders;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.util.Assert;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.TradePair;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.Wallet;
import pl.edu.pjatk.mas.s29904.simplebroker.models.enums.OrderDirection;
import pl.edu.pjatk.mas.s29904.simplebroker.models.enums.OrderStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Getter
@DiscriminatorValue("LIMIT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LimitOrder extends Order {

    private static final BigDecimal SERVICE_FEE = new BigDecimal("0.028");

    @NotNull(message = "Price limit cannot be null")
    @DecimalMin(value = "0.0", message = "Price limit must be positive")
    private BigDecimal priceLimit;

    @NotNull(message = "Expiration datetime cannot be null")
    private LocalDateTime expirationDateTime;

    private LimitOrder(
            OrderDirection direction,
            BigDecimal amount,
            BigDecimal priceLimit,
            LocalDateTime expirationDateTime,
            Wallet wallet,
            TradePair pair
    ) {
        super(direction, amount, wallet, pair);

        this.priceLimit = priceLimit;
        this.expirationDateTime = expirationDateTime;
    }

    public static LimitOrder create(
            OrderDirection direction,
            BigDecimal amount,
            BigDecimal priceLimit,
            LocalDateTime expirationDateTime,
            Wallet wallet,
            TradePair pair
    ) {
        Assert.notNull(direction, "Direction cannot be null");
        Assert.notNull(amount, "Amount cannot be null");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Amount must be positive");
        Assert.notNull(priceLimit, "Price limit cannot be null");
        Assert.isTrue(priceLimit.compareTo(BigDecimal.ZERO) > 0, "Price limit must be positive");
        Assert.notNull(expirationDateTime, "Expiration datetime cannot be null");
        Assert.notNull(wallet, "Wallet cannot be null");
        Assert.notNull(pair, "TradePair cannot be null");

        return new LimitOrder(direction, amount, priceLimit, expirationDateTime, wallet, pair);
    }

    private void blockRequiredFunds() {
        if (getDirection() == OrderDirection.BUY) {
            BigDecimal totalValue = getAmount().multiply(priceLimit);
            BigDecimal fee = totalValue.multiply(SERVICE_FEE);
            getWallet().blockBalance(totalValue.add(fee));
        } else {
            getWallet().blockAssetAmount(getPair().getAsset(), getAmount());
        }
    }

    private void unblockRequiredFunds() {
        if (getDirection() == OrderDirection.BUY) {
            BigDecimal totalValue = getAmount().multiply(priceLimit);
            BigDecimal fee = totalValue.multiply(SERVICE_FEE);
            getWallet().unblockBalance(totalValue.add(fee));
        } else {
            getWallet().unblockAssetAmount(getPair().getAsset(), getAmount());
        }
    }

    // BUSINESS METHODS

    @Override
    public void execute() {
        if (status == OrderStatus.CANCELED || status == OrderStatus.EXPIRED || status == OrderStatus.EXECUTED) {
            throw new IllegalStateException("Cannot process finished order");
        }

        if (status == OrderStatus.NEW) {
            blockRequiredFunds();
            status = OrderStatus.PENDING;
        }

        if (expirationDateTime != null && expirationDateTime.isBefore(LocalDateTime.now())) {
            unblockRequiredFunds();
            status = OrderStatus.EXPIRED;
            return;
        }

        BigDecimal currentRate = getPair().getCurrentRate();
        boolean conditionsMet = false;

        if (getDirection() == OrderDirection.BUY && currentRate.compareTo(priceLimit) <= 0) {
            conditionsMet = true;
        } else if (getDirection() == OrderDirection.SELL && currentRate.compareTo(priceLimit) >= 0) {
            conditionsMet = true;
        }

        if (conditionsMet) {
            unblockRequiredFunds();

            calculate(currentRate, SERVICE_FEE);

            status = OrderStatus.EXECUTED;
        }
    }

    public void cancel() {
        if (status == OrderStatus.CANCELED || status == OrderStatus.EXPIRED || status == OrderStatus.EXECUTED) {
            throw new IllegalStateException("Cannot cancel finished order");
        }

        if (status == OrderStatus.PENDING) {
            unblockRequiredFunds();
        }
        status = OrderStatus.CANCELED;
    }

    // GETTERS

    public static BigDecimal getServiceFee(){
        return SERVICE_FEE;
    }

    public static BigDecimal calculateEstimatedPrice(BigDecimal amount, BigDecimal priceLimit, OrderDirection direction){
        BigDecimal estimatedPrice = amount.multiply(priceLimit);
        estimatedPrice = direction == OrderDirection.BUY ?
                estimatedPrice.add(estimatedPrice.multiply(SERVICE_FEE)) :
                estimatedPrice.subtract(estimatedPrice.multiply(SERVICE_FEE));

        return estimatedPrice.setScale(2, RoundingMode.HALF_UP);
    }

    // SETTERS

    public void setPriceLimit(BigDecimal priceLimit) {
        if (status != OrderStatus.NEW){
            throw new UnsupportedOperationException("Operation supported only for orders with status NEW");
        }

        Assert.notNull(priceLimit, "Price limit cannot be null");
        Assert.isTrue(priceLimit.compareTo(BigDecimal.ZERO) > 0, "Price limit must be positive");

        this.priceLimit = priceLimit;
    }

    public void setExpirationDateTime(LocalDateTime expirationDateTime) {
        if (status != OrderStatus.NEW){
            throw new UnsupportedOperationException("Operation supported only for orders with status NEW");
        }

        Assert.notNull(expirationDateTime, "Expiration datetime cannot be null");

        this.expirationDateTime = expirationDateTime;
    }
}
