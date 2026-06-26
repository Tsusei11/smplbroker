package pl.edu.pjatk.mas.s29904.simplebroker.models.clients;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;
import org.springframework.util.Assert;

@Entity
@Getter
@DiscriminatorValue("COMPANY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends Client {

    @NotBlank(message = "NIP cannot be empty")
    @Pattern(regexp = "^\\d{10}$", message = "NIP must consist of 10 digits")
    @Column(unique = true)
    private String nip;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    private Company(String email, String nip, String name) {
        super(email);

        this.nip = nip;
        this.name = name;
    }

    public static Company create(String email, String nip, String name) {
        Assert.hasText(email, "Email cannot be empty");
        Assert.hasText(nip, "NIP Cannot be empty");
        Assert.isTrue(nip.matches("^\\d{10}$"), "NIP must consist of 10 digits");
        Assert.hasText(name, "Name cannot be empty");

        return new Company(email, nip, name);
    }

    // BUSINESS METHODS

    @Override
    public void verifyIdentity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // SETTERS

    public void setNip(String nip){
        Assert.hasText(nip, "NIP Cannot be empty");
        Assert.isTrue(nip.matches("^\\d{10}$"), "NIP must consist of 10 digits");

        this.nip = nip;
    }

    public void setName(String name){
        Assert.hasText(name, "Name cannot be empty");
        this.name = name;
    }
}
