package pl.edu.pjatk.mas.s29904.simplebroker.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.Wallet;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.WalletPosition;
import pl.edu.pjatk.mas.s29904.simplebroker.models.clients.Client;
import pl.edu.pjatk.mas.s29904.simplebroker.models.orders.Order;
import pl.edu.pjatk.mas.s29904.simplebroker.repositories.ClientRepository;
import pl.edu.pjatk.mas.s29904.simplebroker.repositories.WalletRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService {
    private final ClientRepository clientRepository;
    private final WalletRepository walletRepository;

    public List<Wallet> getClientWallets(Long clientId){
        Client client = clientRepository.findById(clientId).orElseThrow(
                () -> new IllegalArgumentException("Client with id " + clientId + " not found")
        );

        return new ArrayList<>(client.getWallets());
    }

    public Wallet getWalletDetails(Long walletId){
        return walletRepository.findWithPositionById(walletId).orElseThrow(
                () -> new IllegalArgumentException("Wallet with id " + walletId + " not found")
        );
    }
}
