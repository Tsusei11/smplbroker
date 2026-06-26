package pl.edu.pjatk.mas.s29904.simplebroker.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.Wallet;
import pl.edu.pjatk.mas.s29904.simplebroker.models.accounting.WalletPosition;
import pl.edu.pjatk.mas.s29904.simplebroker.models.assets.Asset;
import pl.edu.pjatk.mas.s29904.simplebroker.models.enums.OrderDirection;
import pl.edu.pjatk.mas.s29904.simplebroker.models.enums.OrderType;
import pl.edu.pjatk.mas.s29904.simplebroker.models.orders.LimitOrder;
import pl.edu.pjatk.mas.s29904.simplebroker.models.orders.MarketOrder;
import pl.edu.pjatk.mas.s29904.simplebroker.models.orders.Order;
import pl.edu.pjatk.mas.s29904.simplebroker.services.MarketService;
import pl.edu.pjatk.mas.s29904.simplebroker.services.OrderService;
import pl.edu.pjatk.mas.s29904.simplebroker.services.WalletService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final MarketService marketService;
    private final WalletService walletService;

    private static final Long CURRENT_CLIENT_ID = 1L;

    @GetMapping("/api/price")
    @ResponseBody
    public BigDecimal getAssetPrice(@RequestParam Long assetId) {
        return marketService.getCurrentRateForAsset(assetId);
    }

    @GetMapping(value = {"/create", "/create/{assetId}"})
    public String showCreateOrderForm(@PathVariable(required = false) Long assetId, Model model) {

        if (assetId != null) {
            model.addAttribute("selectedAsset", marketService.getAssetDetails(assetId));
        } else {
            model.addAttribute("assets", marketService.getAvailableAssets(null));
        }

        model.addAttribute("wallets", walletService.getClientWallets(CURRENT_CLIENT_ID));

        return "orders/create";
    }

    @PostMapping("/summary")
    public String showOrderSummary(
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) OrderDirection direction,
            @RequestParam(required = false) OrderType orderType,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) BigDecimal priceLimit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiration,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        String errorRedirectUrl = "redirect:/orders/create";

        Runnable saveFormState = () -> {
            redirectAttributes.addFlashAttribute("savedWalletId", walletId);
            redirectAttributes.addFlashAttribute("savedAssetId", assetId);
            redirectAttributes.addFlashAttribute("savedDirection", direction);
            redirectAttributes.addFlashAttribute("savedOrderType", orderType);
            redirectAttributes.addFlashAttribute("savedAmount", amount);
            redirectAttributes.addFlashAttribute("savedPriceLimit", priceLimit);
            redirectAttributes.addFlashAttribute("savedExpiration", expiration);
        };

        if (assetId == null){
            saveFormState.run();
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd! Pole Aktyw jest wymagane!");
            return errorRedirectUrl;
        }

        if (walletId == null){
            saveFormState.run();
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd! Pole Portfel jest wymagane!");
            return errorRedirectUrl;
        }

        if (amount == null) {
            saveFormState.run();
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd! Pole Kwota jest wymagane!");
            return errorRedirectUrl;
        }

        if (orderType == OrderType.LIMIT){
            if (priceLimit == null) {
                saveFormState.run();
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Błąd! Pole Cena docelowa jest wymagane!"
                );
                return errorRedirectUrl;
            }
            if (expiration == null) {
                saveFormState.run();
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Błąd! Pole Data wygaśnięcia jest wymagane!"
                );
                return errorRedirectUrl;
            }
        }

        Asset asset = marketService.getAssetDetails(assetId);
        Wallet wallet = walletService.getWalletDetails(walletId);

        BigDecimal estimatedPrice = orderType == OrderType.MARKET ?
                marketService.getEstimatedPriceForAssetAmount(assetId, amount, direction) :
                LimitOrder.calculateEstimatedPrice(amount, priceLimit, direction);

        if (direction == OrderDirection.BUY){
            if (wallet.getAvailableBalance().compareTo(estimatedPrice) < 0){
                saveFormState.run();
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Błąd! Niewystarczająca ilość środków w portfelu " + wallet.getName() + "!"
                );
                return errorRedirectUrl;
            }
        }
        else if (direction == OrderDirection.SELL){
            BigDecimal availableAssetAmount = wallet.getPositions().stream()
                    .filter(p -> p.getAsset().getId().equals(assetId))
                    .map(WalletPosition::getAvailableAmount)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

            if (availableAssetAmount.compareTo(amount) < 0){
                saveFormState.run();
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Błąd! Niewystarczająca ilość aktywa " + asset.getTicker() + " w porfelu " + wallet.getName() + "!"
                );
                return errorRedirectUrl;
            }
        }

        model.addAttribute("asset", asset);
        model.addAttribute("wallet", wallet);
        model.addAttribute("direction", direction);
        model.addAttribute("orderType", orderType);
        model.addAttribute("amount", amount);
        model.addAttribute("priceLimit", priceLimit);
        model.addAttribute("expiration", expiration);
        model.addAttribute("estimatedPrice", estimatedPrice);

        return "orders/summary";
    }

    @PostMapping("/confirm")
    public String confirmOrder(
            @RequestParam Long walletId,
            @RequestParam Long assetId,
            @RequestParam OrderDirection direction,
            @RequestParam OrderType orderType,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) BigDecimal priceLimit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiration
    ) {
        if (orderType == OrderType.MARKET) {
            orderService.placeMarketOrder(walletId, assetId, direction, amount);
        } else {
            orderService.placeLimitOrder(walletId, assetId, direction, amount, priceLimit, expiration);
        }

        return "redirect:/orders/success?orderType=" + orderType;
    }

    @GetMapping("/success")
    public String showSuccessPage(@RequestParam OrderType orderType, Model model) {
        model.addAttribute("orderType", orderType);

        return "orders/success";
    }

    @GetMapping("/history/market")
    public String showHistoryMarket(Model model) {
        List<Order> orders = orderService.getClientMarketOrders(CURRENT_CLIENT_ID);
        model.addAttribute("orders", orders);

        return "orders/history_market";
    }

    @GetMapping("/history/limit")
    public String showHistoryLimit(Model model) {
        List<Order> orders = orderService.getClientLimitOrders(CURRENT_CLIENT_ID);
        model.addAttribute("orders", orders);

        return "orders/history_limit";
    }

    @GetMapping("/{orderId}")
    public String showOrderDetails(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderDetails(orderId);

        model.addAttribute("order", order);

        return "orders/details";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Long orderId, RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Zlecenie zostało pomyślnie anulowane, a środki odblokowane.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd: " + e.getMessage());
        }

        return "redirect:/orders/" + orderId;
    }
}
