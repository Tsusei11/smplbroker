package pl.edu.pjatk.mas.s29904.simplebroker.models.assets;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.util.Assert;

@Entity
@Getter
@DiscriminatorValue("ETF")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ETF extends Asset {
    @NotBlank(message = "Emittent cannot be empty")
    private String emittent;

    @NotBlank(message = "Fund type cannot be empty")
    private String fundType;

    private ETF(String ticker, String name, boolean isAvailable, String emittent, String fundType) {
        super(ticker, name, isAvailable);

        this.emittent = emittent;
        this.fundType = fundType;
    }

    public static ETF create(String ticker, String name, boolean isAvailable, String emittent, String fundType) {
        Assert.hasText(ticker, "Ticker cannot be empty");
        Assert.hasText(name, "Name cannot be empty");
        Assert.hasText(emittent, "Emittent cannot be empty");
        Assert.hasText(fundType, "Fund type cannot be empty");

        return new ETF(ticker, name, isAvailable, emittent, fundType);
    }

    // SETTERS

    public void setEmittent(String emittent) {
        Assert.hasText(emittent, "Emittent cannot be empty");
        this.emittent = emittent;
    }

    public void setFundType(String fundType) {
        Assert.hasText(fundType, "Fund type cannot be empty");
        this.fundType = fundType;
    }
}
