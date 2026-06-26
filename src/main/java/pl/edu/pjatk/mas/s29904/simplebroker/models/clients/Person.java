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
@DiscriminatorValue("PERSON")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Person extends Client {

    @NotBlank(message = "PESEL cannot be empty")
    @Pattern(regexp = "^\\d{11}$", message = "PESEL must consist of 11 digits")
    @Column(unique = true)
    private String pesel;

    @NotBlank(message = "First name cannot be empty")
    private String firstName;

    @NotBlank(message = "Last name cannot be empty")
    private String lastName;

    private Person(String email, String pesel, String firstName, String lastName) {
        super(email);

        this.pesel = pesel;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static Person create(String email, String pesel, String firstName, String lastName) {
        Assert.hasText(email, "Email cannot be empty");
        Assert.hasText(pesel, "PESEL cannot be empty");
        Assert.isTrue(pesel.matches("\\d{11}"), "PESEL must consist of 11 digits");
        Assert.hasText(firstName, "First name cannot be empty");
        Assert.hasText(lastName, "Last name cannot be empty");

        return new Person(email, pesel, firstName, lastName);
    }

    // BUSINESS METHODS

    @Override
    public void verifyIdentity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // SETTERS

    public void setPesel(String pesel) {
        Assert.hasText(pesel, "PESEL cannot be empty");
        Assert.isTrue(pesel.matches("\\d{11}"), "PESEL must consist of 11 digits");
        this.pesel = pesel;
    }

    public void setFirstName(String firstName) {
        Assert.hasText(firstName, "First name cannot be empty");
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        Assert.hasText(lastName, "Last name cannot be empty");
        this.lastName = lastName;
    }
}
