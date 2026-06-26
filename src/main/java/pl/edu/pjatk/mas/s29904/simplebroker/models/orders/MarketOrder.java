package pl.edu.pjatk.mas.s29904.simplebroker.models.orders;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import org.springframework.util.Assert;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.TradePair;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.Wallet;
import pl.edu.pjatk.mas.s29904.simplebroker.models.enums.OrderDirection;
import pl.edu.pjatk.mas.s29904.simplebroker.models.enums.OrderStatus;

import java.math.BigDecimal;

@Entity
@Getter
@DiscriminatorValue("MARKET")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketOrder extends Order {

    private static final BigDecimal SERVICE_FEE = new BigDecimal("0.015");

    private MarketOrder(
            OrderDirection direction,
            BigDecimal amount,
            Wallet wallet,
            TradePair pair
    ) {
        super(direction, amount, wallet, pair);
    }

    public static MarketOrder create(
            OrderDirection direction,
            BigDecimal amount,
            Wallet wallet,
            TradePair pair
    ) {
        Assert.notNull(direction, "Direction cannot be null");
        Assert.notNull(amount, "Amount cannot be null");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Amount must be positive");
        Assert.notNull(wallet, "Wallet cannot be null");
        Assert.notNull(pair, "TradePair cannot be null");

        return new MarketOrder(direction, amount, wallet, pair);
    }

    // BUSINESS METHODS

    @Override
    public void execute() {
        if (status != OrderStatus.NEW) {
            throw new IllegalStateException("Market order can only be executed from NEW state");
        }

        calculate(getPair().getCurrentRate(), SERVICE_FEE);

        status = OrderStatus.EXECUTED;
    }

    // GETTERS

    public static BigDecimal getServiceFee(){
        return SERVICE_FEE;
    }
}
