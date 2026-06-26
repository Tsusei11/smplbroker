package pl.edu.pjatk.mas.s29904.simplebroker.models.accounting;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.util.Assert;
import pl.edu.pjatk.mas.s29904.simplebroker.models.assets.Asset;
import pl.edu.pjatk.mas.s29904.simplebroker.models.clients.Client;
import pl.edu.pjatk.mas.s29904.simplebroker.models.orders.Order;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Available balance cannot be null")
    @DecimalMin(value = "0.0", message = "Available balance must be positive")
    @Column(nullable = false)
    private BigDecimal availableBalance;

    @NotNull(message = "Blocked balance cannot be null")
    @DecimalMin(value = "0.0", message = "Blocked balance must be positive")
    @Column(nullable = false)
    private BigDecimal blockedBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, updatable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false, updatable = false)
    private Currency currency;

    @OneToMany(
            mappedBy = "wallet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Order> orders = new HashSet<>();

    @OneToMany(
            mappedBy = "wallet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private Set<WalletPosition> positions = new HashSet<>();

    @OneToMany(
            mappedBy = "wallet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Deposit> deposits = new HashSet<>();

    @OneToMany(
            mappedBy = "wallet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Withdrawal> withdrawals = new HashSet<>();

    private Wallet(String name, Client client, Currency currency) {
        this.name = name;
        this.availableBalance = BigDecimal.ZERO;
        this.blockedBalance = BigDecimal.ZERO;

        this.client = client;
        client.addWallet(this);

        this.currency = currency;
        currency.addWallet(this);
    }

    public static Wallet create(String name, Client client, Currency currency) {
        Assert.hasText(name, "Name cannot be empty");
        Assert.notNull(client, "Client cannot be null");
        Assert.notNull(currency, "Currency cannot be null");

        return new Wallet(name, client, currency);
    }

    public void remove(){
        if (client != null){
            Client tempClient = client;
            client = null;
            tempClient.removeWallet(this);
        }

        if (currency != null){
            Currency tempCurrency = currency;
            currency = null;
            tempCurrency.removeWallet(this);
        }

        if (orders != null && !orders.isEmpty()){
            Set<Order> tempOrders = new HashSet<>(orders);
            for (Order order : tempOrders){
                order.remove();
            }
        }

        if (positions != null && !positions.isEmpty()){
            Set<WalletPosition> tempPositions = new HashSet<>(positions);
            for (WalletPosition position : tempPositions){
                position.remove();
            }
        }

        if (deposits != null && !deposits.isEmpty()){
            Set<Deposit> tempDeposits = new HashSet<>(deposits);
            for (Deposit deposit : tempDeposits){
                deposit.remove();
            }
        }

        if (withdrawals != null && !withdrawals.isEmpty()){
            Set<Withdrawal> tempWithdrawals = new HashSet<>(withdrawals);
            for (Withdrawal withdrawal : tempWithdrawals){
                withdrawal.remove();
            }
        }
    }

    // RELATIONSHIP METHODS

    public void addOrder(Order order){
        Assert.notNull(order, "Order cannot be null");
        Assert.isTrue(
                order.getWallet() != null &&
                order.getWallet().getId().equals(this.id),
                "Order is assigned to another wallet"
        );

        orders.add(order);
    }

    public void removeOrder(Order order){
        Assert.notNull(order, "Order cannot be null");

        if (orders.contains(order)) {
            orders.remove(order);
            order.remove();
        }
    }

    public void addPosition(WalletPosition position) {
        Assert.notNull(position, "Position cannot be null");
        Assert.isTrue(
                position.getWallet() != null &&
                position.getWallet().getId().equals(this.id),
                "Position is assigned to another wallet"
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

    public void addDeposit(Deposit deposit) {
        Assert.notNull(deposit, "Deposit cannot be null");
        Assert.isTrue(
                deposit.getWallet() != null &&
                deposit.getWallet().getId().equals(this.id),
                "Deposit is assigned to another wallet"
        );

        deposits.add(deposit);
    }

    public void removeDeposit(Deposit deposit) {
        Assert.notNull(deposit, "Deposit cannot be null");

        if (deposits.contains(deposit)) {
            deposits.remove(deposit);
            deposit.remove();
        }
    }

    public void addWithdrawal(Withdrawal withdrawal) {
        Assert.notNull(withdrawal, "Withdrawal cannot be null");
        Assert.isTrue(
                withdrawal.getWallet() != null &&
                withdrawal.getWallet().getId().equals(this.id),
                "Withdrawal is assigned to another wallet"
        );

        withdrawals.add(withdrawal);
    }

    public void removeWithdrawal(Withdrawal withdrawal) {
        Assert.notNull(withdrawal, "Withdrawal cannot be null");

        if (withdrawals.contains(withdrawal)) {
            withdrawals.remove(withdrawal);
            withdrawal.remove();
        }
    }

    // BUSINESS METHODS

    public void blockBalance(BigDecimal amount){
        Assert.isTrue(availableBalance.compareTo(amount)>=0, "Available balance is less than given amount");

        availableBalance = availableBalance.subtract(amount);
        blockedBalance = blockedBalance.add(amount);
    }

    public void unblockBalance(BigDecimal amount){
        Assert.isTrue(blockedBalance.compareTo(amount)>=0, "Blocked balance is less than given amount");

        availableBalance = availableBalance.add(amount);
        blockedBalance = blockedBalance.subtract(amount);
    }

    public void topUpBalance(BigDecimal amount){
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO)>0, "Given amount is less than or equal to zero");

        availableBalance = availableBalance.add(amount);
    }

    public void withdrawBalance(BigDecimal amount){
        Assert.isTrue(availableBalance.compareTo(amount)>=0, "Available balance is less than given amount");

        availableBalance = availableBalance.subtract(amount);
    }

    public void addAssetAmount(Asset asset, BigDecimal amount){
        Assert.notNull(asset, "Asset cannot be null");
        Assert.notNull(amount, "Amount cannot be null");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO)>0, "Amount must be positive");

        positions.stream().filter(position -> position.getAsset().equals(asset))
                .findFirst()
                .ifPresentOrElse(
                        position -> position.addAmount(amount),
                        () -> WalletPosition.create(amount,this, asset)
        );
    }

    public void subtractAssetAmount(Asset asset, BigDecimal amount){
        Assert.notNull(asset, "Asset cannot be null");
        Assert.notNull(amount, "Amount cannot be null");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO)>0, "Amount must be positive");
        Assert.isTrue(
                positions.stream().anyMatch(position -> position.getAsset().equals(asset)),
                "Wallet does not contain this asset"
        );

        positions.stream().filter(position -> position.getAsset().equals(asset))
                .findFirst()
                .ifPresent(
                        position -> position.subtractAmount(amount)
                );
    }

    public void blockAssetAmount(Asset asset, BigDecimal amount){
        Assert.notNull(asset, "Asset cannot be null");
        Assert.notNull(amount, "Amount cannot be null");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO)>0, "Amount must be positive");
        Assert.isTrue(
                positions.stream().anyMatch(position -> position.getAsset().equals(asset)),
                "Wallet does not contain this asset"
        );

        positions.stream().filter(position -> position.getAsset().equals(asset))
                .findFirst()
                .ifPresent(
                        position -> position.blockAmount(amount)
                );
    }

    public void unblockAssetAmount(Asset asset, BigDecimal amount){
        Assert.notNull(asset, "Asset cannot be null");
        Assert.notNull(amount, "Amount cannot be null");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO)>0, "Amount must be positive");

        Assert.isTrue(
                positions.stream().anyMatch(position -> position.getAsset().equals(asset)),
                "Wallet does not contain this asset"
        );

        positions.stream().filter(position -> position.getAsset().equals(asset))
                .findFirst()
                .ifPresent(
                        position -> position.unblockAmount(amount)
                );
    }

    // GETTERS

    public Set<Order> getOrders() {
        return Collections.unmodifiableSet(orders);
    }

    public Set<WalletPosition> getPositions(){
        return Collections.unmodifiableSet(positions);
    }

    public Set<Deposit> getDeposits(){
        return Collections.unmodifiableSet(deposits);
    }

    public Set<Withdrawal> getWithdrawals(){
        return Collections.unmodifiableSet(withdrawals);
    }

    // SETTERS

    public void setName(String name) {
        Assert.hasText(name, "Name cannot be empty");
        this.name = name;
    }
}
