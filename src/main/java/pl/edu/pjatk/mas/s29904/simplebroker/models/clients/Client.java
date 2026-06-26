package pl.edu.pjatk.mas.s29904.simplebroker.models.clients;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.util.Assert;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.Wallet;

import javax.naming.OperationNotSupportedException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "client_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Wrong email format")
    @Column(nullable = false)
    private String email;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDate registrationDate;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(
            mappedBy = "client",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private Set<Wallet> wallets = new HashSet<>();

    protected Client(String email) {
        this.email = email;
        this.active = false;
    }

    public void remove(){
        if (wallets != null && !wallets.isEmpty()){
            Set<Wallet> tempWallets = new HashSet<>(wallets);
            for (Wallet wallet : tempWallets){
                wallet.remove();
            }
        }
    }

    // RELATIONSHIP METHODS

    public void addWallet(Wallet wallet) {
        Assert.notNull(wallet, "Wallet cannot be null");
        Assert.isTrue(
                wallet.getClient() != null &&
                wallet.getClient().getId().equals(this.id),
                "Wallet is assigned to another client"
        );

        wallets.add(wallet);
    }

    public void removeWallet(Wallet wallet) {
        Assert.notNull(wallet, "Wallet cannot be null");

        if (wallets.contains(wallet)) {
            wallets.remove(wallet);
            wallet.remove();
        }
    }

    // BUSINESS METHODS

    public abstract void verifyIdentity() throws OperationNotSupportedException;

    public void activateAccount(){
        if (active){
            throw new IllegalStateException("Account is already activated");
        }

        active = true;
    }

    // GETTERS

    public Set<Wallet> getWallets() {
        return Collections.unmodifiableSet(wallets);
    }

    // SETTERS

    public void setEmail(String email) {
        Assert.hasText(email, "Email cannot be empty");
        this.email = email;
    }
}
