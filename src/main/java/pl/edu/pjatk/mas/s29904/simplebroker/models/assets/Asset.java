package pl.edu.pjatk.mas.s29904.simplebroker.models.assets;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;
import org.springframework.util.Assert;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.TradePair;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.WalletPosition;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "asset_type", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor
public abstract class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @NotBlank(message = "Ticker cannot be empty")
    @Column(unique = true, nullable = false)
    private String ticker;

    @NotBlank(message = "Name cannot be empty")
    @Column(unique = true, nullable = false)
    private String name;

    @Setter
    @NotNull(message = "isAvailable cannot be empty")
    @Column(nullable = false)
    private boolean available;

    @OneToMany(
            mappedBy = "asset",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<TradePair> pairs = new HashSet<>();

    @OneToMany(
            mappedBy = "asset",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<WalletPosition> positions = new HashSet<>();

    protected Asset(String ticker, String name, boolean isAvailable) {
        this.ticker = ticker;
        this.name = name;
        this.available = isAvailable;
    }

    public void remove(){
        if (pairs != null && !pairs.isEmpty()){
            Set<TradePair> tempPairs = new HashSet<>(pairs);
            for (TradePair pair : tempPairs){
                pair.remove();
            }
        }

        if (positions != null && !positions.isEmpty()){
            Set<WalletPosition> tempPositions = new HashSet<>(positions);
            for (WalletPosition position : tempPositions){
                position.remove();
            }
        }
    }

    // RELATIONSHIP METHODS

    public void addPosition(WalletPosition position) {
        Assert.notNull(position, "Position cannot be null");
        Assert.isTrue(
                position.getAsset() != null &&
                position.getAsset().getId().equals(this.id),
                "Position is assigned to another asset"
        );

        positions.add(position);
    }

    public void removePosition(WalletPosition position) {
        Assert.notNull(position, "Position cannot be null");

        if (positions.contains(position)) {
            positions.remove(position);
            position.remove();
        }
    }

    public void addPair(TradePair pair) {
        Assert.notNull(pair, "TradePair cannot be null");
        Assert.isTrue(pair.getAsset() == this, "Pair is assigned to another asset");

        pairs.add(pair);
    }

    public void removePair(TradePair pair) {
        Assert.notNull(pair, "TradePair cannot be null");

        if (pairs.contains(pair)){
            pairs.remove(pair);
            pair.remove();
        }
    }

    // GETTERS

    public Set<WalletPosition> getPositions(){
        return Collections.unmodifiableSet(positions);
    }

    // SETTERS

    public void setTicker(String ticker) {
        Assert.hasText(ticker, "Ticker cannot be empty");
        this.ticker = ticker;
    }

    public void setName(String name) {
        Assert.hasText(name, "Name cannot be empty");
        this.name = name;
    }

}
