package pl.edu.pjatk.mas.s29904.simplebroker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.pjatk.mas.s29904.simplebroker.models.orders.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.wallet w " +
            "JOIN FETCH o.pair p " +
            "WHERE o.status = pl.edu.pjatk.mas.s29904.simplebroker.models.enums.OrderStatus.PENDING " +
            "AND TYPE(o) = LimitOrder")
    List<Order> findAllPendingLimitOrders();

    @Query("SELECT o FROM Order o WHERE TYPE(o) = MarketOrder AND o.wallet.client.id = :clientId ORDER BY o.number DESC")
    List<Order> findMarketOrdersByClientId(@Param("clientId") Long clientId);

    @Query("SELECT o FROM Order o WHERE TYPE(o) = LimitOrder AND o.wallet.client.id = :clientId ORDER BY o.number DESC")
    List<Order> findLimitOrdersByClientId(@Param("clientId") Long clientId);

    @Query("SELECT o FROM Order o WHERE TYPE(o) = MarketOrder AND o.wallet.client.id = :clientId AND o.pair.asset.id = :assetId ORDER BY o.createdAt DESC FETCH FIRST 3 ROWS ONLY")
    List<Order> findTop3MarketOrdersForAsset(@Param("clientId") Long clientId, @Param("assetId") Long assetId);

    @Query("SELECT o FROM Order o WHERE TYPE(o) = LimitOrder AND o.wallet.client.id = :clientId AND o.pair.asset.id = :assetId ORDER BY o.createdAt DESC FETCH FIRST 3 ROWS ONLY")
    List<Order> findTop3LimitOrdersForAsset(@Param("clientId") Long clientId, @Param("assetId") Long assetId);
}
