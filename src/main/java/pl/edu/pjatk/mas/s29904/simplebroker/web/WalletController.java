package pl.edu.pjatk.mas.s29904.simplebroker.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.Wallet;
import pl.edu.pjatk.mas.s29904.simplebroker.models.orders.Order;
import pl.edu.pjatk.mas.s29904.simplebroker.services.WalletService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    private static final Long CURRENT_CLIENT_ID = 1L;

    @GetMapping
    public String showClientWallets(Model model) {
        List<Wallet> wallets = walletService.getClientWallets(CURRENT_CLIENT_ID);

        model.addAttribute("wallets", wallets);

        return "wallets/list";
    }

    @GetMapping("/{wallet_id}")
    public String showWalletDetails(@PathVariable("wallet_id") Long walletId, Model model) {
        Wallet wallet = walletService.getWalletDetails(walletId);

        List<Order> allOrders = new ArrayList<>(wallet.getOrders());

        List<Order> marketOrders = allOrders.stream()
                .filter(o -> o.getClass().getSimpleName().equals("MarketOrder"))
                .sorted(java.util.Comparator.comparing(Order::getCreatedAt).reversed())
                .toList();

        List<Order> limitOrders = allOrders.stream()
                .filter(o -> o.getClass().getSimpleName().equals("LimitOrder"))
                .sorted(java.util.Comparator.comparing(Order::getCreatedAt).reversed())
                .toList();

        model.addAttribute("wallet", wallet);
        model.addAttribute("positions", new ArrayList<>(wallet.getPositions()));
        model.addAttribute("marketOrders", marketOrders);
        model.addAttribute("limitOrders", limitOrders);

        return "wallets/details";
    }
}
