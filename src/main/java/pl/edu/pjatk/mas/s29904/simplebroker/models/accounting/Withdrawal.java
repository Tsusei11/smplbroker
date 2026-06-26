package pl.edu.pjatk.mas.s29904.simplebroker.models.accounting;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.util.Assert;
import pl.edu.pjatk.mas.s29904.simplebroker.models.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Withdrawal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Destination address cannot be empty")
    @Column(nullable = false)
    private String destinationAddress;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", message = "Amount must be positive")
    @Column(nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDate date;

    @NotNull(message = "Payment status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false, updatable = false)
    private Wallet wallet;

    private Withdrawal(String destinationAddress, BigDecimal amount, Wallet wallet) {
        this.destinationAddress = destinationAddress;
        this.amount = amount;
        paymentStatus = PaymentStatus.PENDING;
        
        this.wallet = wallet;
        wallet.addWithdrawal(this);
    }

    public static Withdrawal create(String destinationAddress, BigDecimal amount, Wallet wallet) {
        Assert.hasText(destinationAddress, "Destination address cannot be empty");
        Assert.notNull(amount, "Amount cannot be null");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Amount must be positive");
        Assert.notNull(wallet, "Wallet cannot be null");

        return new Withdrawal(destinationAddress, amount, wallet);
    }

    public void remove(){
        if (wallet != null){
            Wallet tempWallet = wallet;
            wallet = null;
            tempWallet.removeWithdrawal(this);
        }
    }

    // BUSINESS METHODS

    public void execute(){
        paymentStatus = PaymentStatus.COMPLETED;
    }

    // SETTERS

    public void setDestinationAddress(String destinationAddress) {
        Assert.hasText(destinationAddress, "Destination address cannot be empty");
        this.destinationAddress = destinationAddress;
    }

    public void setAmount(BigDecimal amount) {
        Assert.notNull(amount, "Amount cannot be null");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Amount must be positive");

        this.amount = amount;
    }
}
