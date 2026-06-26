package pl.edu.pjatk.mas.s29904.simplebroker.bootstrap;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.Currency;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.TradePair;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.Wallet;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.WalletPosition;
import pl.edu.pjatk.mas.s29904.simplebroker.models.assets.ETF;
import pl.edu.pjatk.mas.s29904.simplebroker.models.assets.Stock;
import pl.edu.pjatk.mas.s29904.simplebroker.models.clients.Person;
import pl.edu.pjatk.mas.s29904.simplebroker.repositories.*;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final ClientRepository clientRepository;
    private final CurrencyRepository currencyRepository;
    private final WalletRepository walletRepository;
    private final AssetRepository assetRepository;
    private final TradePairRepository tradePairRepository;
    private final WalletPositionRepository walletPositionRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (clientRepository.count() == 0) {
            Currency currency = Currency.create("USD", "Dolar Amerykański");
            currencyRepository.save(currency);

            Person client = Person.create("s29904@pjwstk.edu.pl", "12345678901", "Aliaksei", "Sihinevich");
            client.activateAccount();
            clientRepository.save(client);

            Stock aapl = Stock.create("AAPL", "Apple Inc.", true, "USA", "BigTech");
            Stock msft = Stock.create("MSFT", "Microsoft Inc.", true, "USA", "BigTech");
            Stock lvmh = Stock.create("LVMH", "LVMH Moët Hennessy — Louis Vuitton", true, "France", "Fashion");
            Stock shel = Stock.create("SHEL", "Shell PLC", true, "UK", "Energetics");

            ETF spy = ETF.create("SPY", "State Street S&P 500", true, "State Street", "Stocks");
            ETF voo = ETF.create("VOO", "Vanguard S&P 500", true, "Vanguard", "Stocks");
            ETF ivv = ETF.create("IVV", "iShares Core S&P 500", true, "iShares", "Stocks");
            ETF iau = ETF.create("IAU", "iShares Gold Trust", true, "iShares", "Commodities");
            assetRepository.saveAll(List.of(aapl, msft, lvmh, shel, spy, voo, ivv, iau));

            TradePair aaplUsd = TradePair.create(new BigDecimal("185.5"), currency, aapl);
            TradePair msftUsd = TradePair.create(new BigDecimal("113.43"), currency, msft);
            TradePair lvmhUsd = TradePair.create(new BigDecimal("94.25"), currency, lvmh);
            TradePair shelUsd = TradePair.create(new BigDecimal("105.4"), currency, shel);
            TradePair spyUsd = TradePair.create(new BigDecimal("52.25"), currency, spy);
            TradePair vooUsd = TradePair.create(new BigDecimal("51.99"), currency, voo);
            TradePair ivvUsd = TradePair.create(new BigDecimal("53.05"), currency, ivv);
            TradePair iauUsd = TradePair.create(new BigDecimal("122.34"), currency, iau);
            tradePairRepository.saveAll(List.of(aaplUsd, msftUsd, lvmhUsd, shelUsd, spyUsd, vooUsd, ivvUsd, iauUsd));

            Wallet wallet = Wallet.create("Portfel inwestycyjny", client, currency);
            wallet.topUpBalance(new BigDecimal("10000"));
            Wallet wallet2 = Wallet.create("Eksperymentalny", client, currency);
            wallet2.topUpBalance(new BigDecimal("1500"));
            walletRepository.saveAll(List.of(wallet, wallet2));

            WalletPosition position1 = WalletPosition.create(
                    new BigDecimal("1.1"),
                    wallet,
                    aapl
            );
            WalletPosition position2 = WalletPosition.create(
                    new BigDecimal("3.4"),
                    wallet,
                    spy
            );
            WalletPosition position3 = WalletPosition.create(
                    new BigDecimal("10.5"),
                    wallet,
                    iau
            );
            walletPositionRepository.saveAll(List.of(position1, position2, position3));

            System.out.println("Successfully initialized system data");
        }
    }
}
