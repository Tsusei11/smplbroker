package pl.edu.pjatk.mas.s29904.simplebroker.models.accounting;


import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.util.Assert;
import pl.edu.pjatk.mas.s29904.simplebroker.models.assets.Asset;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletPosition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "Available amount cannot be null")
    @DecimalMin(value = "0.0", message = "Available amount must be positive")
    @Column(nullable = false)
    private BigDecimal availableAmount;

    @NotNull(message = "Blocked amount cannot be null")
    @DecimalMin(value = "0.0", message = "Blocked amount must be positive")
    @Column(nullable = false)
    private BigDecimal blockedAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false, updatable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false, updatable = false)
    private Asset asset;

    private WalletPosition(BigDecimal availableAmount, Wallet wallet, Asset asset) {
        this.availableAmount = availableAmount;
        this.blockedAmount = BigDecimal.ZERO;

        this.wallet = wallet;
        wallet.addPosition(this);

        this.asset = asset;
        asset.addPosition(this);
    }

    public static WalletPosition create(BigDecimal availableAmount, Wallet wallet, Asset asset) {
        Assert.notNull(availableAmount, "Available amount cannot be null");
        Assert.isTrue(availableAmount.compareTo(BigDecimal.ZERO) > 0, "Available amount must be positive");
        Assert.notNull(wallet, "Wallet cannot be null");
        Assert.notNull(asset, "Asset cannot be null");

        return new WalletPosition(availableAmount, wallet, asset);
    }

    public void remove(){
        if (wallet != null) {
            Wallet tempWallet = wallet;
            wallet = null;
            tempWallet.removePosition(this);
        }

        if (asset != null) {
            Asset tempAsset = asset;
            asset = null;
            tempAsset.removePosition(this);
        }
    }

    // BUSINESS METHODS

    public void addAmount(BigDecimal amount) {
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Amount must be greater than zero");

        availableAmount = availableAmount.add(amount);
    }

    public void subtractAmount(BigDecimal amount) {
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Amount must be greater than zero");

        availableAmount = availableAmount.subtract(amount);

        if (availableAmount.compareTo(BigDecimal.ZERO) == 0 && blockedAmount.compareTo(BigDecimal.ZERO) == 0) {
            remove();
        }
    }

    public void blockAmount(BigDecimal amount) {
        Assert.isTrue(availableAmount.compareTo(amount) >= 0, "Available amount is less than given amount");

        availableAmount = availableAmount.subtract(amount);
        blockedAmount = blockedAmount.add(amount);
    }

    public void unblockAmount(BigDecimal amount) {
        Assert.isTrue(blockedAmount.compareTo(amount) >= 0, "Blocked amount is less than given amount");

        availableAmount = availableAmount.add(amount);
        blockedAmount = blockedAmount.subtract(amount);

        if (availableAmount.compareTo(BigDecimal.ZERO) == 0 && blockedAmount.compareTo(BigDecimal.ZERO) == 0) {
            remove();
        }
    }
}
