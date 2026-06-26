package pl.edu.pjatk.mas.s29904.simplebroker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.Currency;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {}
