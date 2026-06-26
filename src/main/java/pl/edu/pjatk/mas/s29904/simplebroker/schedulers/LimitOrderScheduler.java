package pl.edu.pjatk.mas.s29904.simplebroker.schedulers;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pjatk.mas.s29904.simplebroker.services.OrderService;

@Component
@RequiredArgsConstructor
public class LimitOrderScheduler {
    private final OrderService orderService;

    @Transactional
    @Scheduled(fixedRate = 1000)
    public void tick(){
        orderService.processPendingLimitedOrders();
    }
}
