package pl.edu.pjatk.mas.s29904.simplebroker.models.assets;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.util.Assert;

@Entity
@Getter
@DiscriminatorValue("STOCK")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends Asset {
    @NotBlank(message = "Origin country cannot be empty")
    private String originCountry;

    @NotBlank(message = "Branch cannot be empty")
    private String branch;

    private Stock(String ticker, String name, boolean isAvailable, String originCountry, String branch) {
        super(ticker, name, isAvailable);

        this.originCountry = originCountry;
        this.branch = branch;
    }

    public static Stock create(String ticker, String name, boolean isAvailable, String originCountry, String branch) {
        Assert.hasText(ticker, "Ticker cannot be empty");
        Assert.hasText(name, "Name cannot be empty");
        Assert.hasText(originCountry, "Origin country cannot be empty");
        Assert.hasText(branch, "Branch cannot be empty");

        return new Stock(ticker, name, isAvailable, originCountry, branch);
    }

    // SETTERS

    public void setOriginCountry(String originCountry) {
        Assert.hasText(originCountry, "Origin country cannot be empty");
        this.originCountry = originCountry;
    }

    public void setBranch(String branch) {
        Assert.hasText(branch, "Branch cannot be empty");
        this.branch = branch;
    }
}
