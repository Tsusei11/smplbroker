package pl.edu.pjatk.mas.s29904.simplebroker.models.accounting;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.NaturalId;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NaturalId
    @NotBlank(message = "ISO code cannot be empty")
    @Column(unique = true, nullable = false)
    private String isoCode;

    @NotBlank(message = "Name cannot be empty")
    @Column(nullable = false)
    private String name;

    @OneToMany(
            mappedBy = "currency",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Wallet> wallets = new HashSet<>();

    @OneToMany(
            mappedBy = "currency",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<TradePair> pairs = new HashSet<>();

    private Currency(String isoCode, String name) {
        this.isoCode = isoCode;
        this.name = name;
    }

    public static Currency create(String isoCode, String name) {
        Assert.hasText(isoCode, "ISO code cannot be empty");
        Assert.hasText(name, "Name cannot be empty");

        return new Currency(isoCode, name);
    }

    public void remove(){
        if (wallets != null && !wallets.isEmpty()) {
            Set<Wallet> tempWallets = new HashSet<>(wallets);
            for (Wallet wallet : tempWallets) {
                wallet.remove();
            }
        }

        if (pairs != null && !pairs.isEmpty()) {
            Set<TradePair> tempPairs = new HashSet<>(pairs);
            for (TradePair pair : tempPairs) {
                pair.remove();
            }
        }
    }

    // RELATIONSHIP METHODS

    public void addPair(TradePair pair) {
        Assert.notNull(pair, "TradePair cannot be null");
        Assert.isTrue(pair.getCurrency() == this, "TradePair is assigned to another currency");

        pairs.add(pair);
    }

    public void removePair(TradePair pair) {
        Assert.notNull(pair, "TradePair cannot be null");

        if (pairs.contains(pair)) {
            pairs.remove(pair);
            pair.remove();
        }
    }

    public void addWallet(Wallet wallet) {
        Assert.notNull(wallet, "Wallet cannot be null");
        Assert.isTrue(wallet.getCurrency() == this, "Wallet is assigned to another currency");

        wallets.add(wallet);
    }

    public void removeWallet(Wallet wallet) {
        Assert.notNull(wallet, "Wallet cannot be null");

        if (wallets.contains(wallet)) {
            wallets.remove(wallet);
            wallet.remove();
        }
    }

    // SETTERS

    public void setIsoCode(String isoCode) {
        Assert.hasText(isoCode, "ISO code cannot be empty");
        this.isoCode = isoCode;
    }

    public void setName(String name) {
        Assert.hasText(name, "Name cannot be empty");
        this.name = name;
    }
}
