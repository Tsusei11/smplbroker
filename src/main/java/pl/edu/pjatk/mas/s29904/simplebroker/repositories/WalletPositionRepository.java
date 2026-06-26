package pl.edu.pjatk.mas.s29904.simplebroker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.WalletPosition;

@Repository
public interface WalletPositionRepository extends JpaRepository<WalletPosition, Long> {}
