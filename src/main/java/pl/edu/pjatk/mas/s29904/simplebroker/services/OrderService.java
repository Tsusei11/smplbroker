package pl.edu.pjatk.mas.s29904.simplebroker.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.TradePair;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.Wallet;
import pl.edu.pjatk.mas.s29904.simplebroker.models.enums.OrderDirection;
import pl.edu.pjatk.mas.s29904.simplebroker.models.orders.LimitOrder;
import pl.edu.pjatk.mas.s29904.simplebroker.models.orders.MarketOrder;
import pl.edu.pjatk.mas.s29904.simplebroker.models.orders.Order;
import pl.edu.pjatk.mas.s29904.simplebroker.repositories.OrderRepository;
import pl.edu.pjatk.mas.s29904.simplebroker.repositories.TradePairRepository;
import pl.edu.pjatk.mas.s29904.simplebroker.repositories.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final WalletRepository walletRepository;
    private final TradePairRepository tradePairRepository;

    @Transactional(readOnly = true)
    public List<Order> getClientMarketOrders(Long clientId) {
        return orderRepository.findMarketOrdersByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public List<Order> getClientLimitOrders(Long clientId) {
        return orderRepository.findLimitOrdersByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public Order getOrderDetails(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(
                () -> new IllegalArgumentException("Order " + orderId + " not found")
        );
    }

    @Transactional
    public void placeMarketOrder(Long walletId, Long assetId, OrderDirection orderDirection, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(
                () -> new IllegalArgumentException("Wallet with id " + walletId + " not found")
        );

        TradePair tradePair = tradePairRepository.findByAssetIdAndCurrencyId(assetId, wallet.getCurrency().getId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Trade pair is not supported")
                );

        MarketOrder marketOrder = MarketOrder.create(orderDirection, amount, wallet, tradePair);

        marketOrder.execute();

        orderRepository.save(marketOrder);
    }

    @Transactional
    public void placeLimitOrder(
            Long walletId,
            Long assetId,
            OrderDirection orderDirection,
            BigDecimal amount,
            BigDecimal priceLimit,
            LocalDateTime expirationDateTime
    ) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(
                () -> new IllegalArgumentException("Wallet with id " + walletId + " not found")
        );

        TradePair tradePair = tradePairRepository.findByAssetIdAndCurrencyId(assetId, wallet.getCurrency().getId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Trade pair is not supported")
                );

        LimitOrder limitOrder = LimitOrder.create(orderDirection, amount, priceLimit, expirationDateTime, wallet, tradePair);

        limitOrder.execute();

        orderRepository.save(limitOrder);
    }

    @Transactional
    public void processPendingLimitedOrders(){
        List<Order> orders = orderRepository.findAllPendingLimitOrders();

        for (Order order : orders){
            try {
                order.execute();
            } catch (Exception e) {
                System.err.println("Failed to process order ID " + order.getNumber() + ": " + e.getMessage());
            }
        }
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new IllegalArgumentException("Order with id " + orderId + " not found")
        );

        if (!(order instanceof LimitOrder limitOrder)) {
            throw new IllegalArgumentException("Order " + orderId + " is not a limit order");
        }

        limitOrder.cancel();

        orderRepository.save(limitOrder);
    }
}
