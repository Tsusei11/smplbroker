package pl.edu.pjatk.mas.s29904.simplebroker.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.edu.pjatk.mas.s29904.simplebroker.models.assets.Asset;
import pl.edu.pjatk.mas.s29904.simplebroker.repositories.OrderRepository;
import pl.edu.pjatk.mas.s29904.simplebroker.services.MarketService;
import pl.edu.pjatk.mas.s29904.simplebroker.services.WalletService;

import java.util.List;

@Controller
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {
    private final MarketService marketService;
    private final WalletService walletService;
    private final OrderRepository orderRepository;

    private static final Long CURRENT_CLIENT_ID = 1L;

    @GetMapping
    public String showMarket(@RequestParam(value="q", required=false) String query, Model model) {
        List<Asset> assets = marketService.getAvailableAssets(query);

        model.addAttribute("assets", assets);
        model.addAttribute("prices", marketService.getAssetPrices(assets));
        model.addAttribute("query", query);

        return "market/list";
    }

    @GetMapping("/{asset_id}")
    public String showAssetDetails(
            @PathVariable("asset_id") Long assetId,
            @RequestParam(required = false) String returnUrl,
            Model model
    ) {
        Asset asset = marketService.getAssetDetails(assetId);

        model.addAttribute("asset", asset);
        model.addAttribute("wallets", walletService.getClientWallets(CURRENT_CLIENT_ID));

        model.addAttribute("marketOrders", orderRepository.findTop3MarketOrdersForAsset(CURRENT_CLIENT_ID, assetId));
        model.addAttribute("limitOrders", orderRepository.findTop3LimitOrdersForAsset(CURRENT_CLIENT_ID, assetId));

        model.addAttribute("returnUrl", returnUrl == null ? "/market" : returnUrl);

        return "market/details";
    }
}
