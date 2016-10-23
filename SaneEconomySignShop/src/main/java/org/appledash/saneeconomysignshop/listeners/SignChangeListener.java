package org.appledash.saneeconomysignshop.listeners;

import com.google.common.base.Strings;
import net.md_5.bungee.api.ChatColor;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.appledash.saneeconomysignshop.SaneEconomySignShop;
import org.appledash.saneeconomysignshop.signshop.SignShop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by appledash on 10/2/16.
 * Blackjack is still best pony.
 */
public class SignChangeListener implements Listener {
    private SaneEconomySignShop plugin;

    public SignChangeListener(SaneEconomySignShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent evt) {
        if (!evt.getPlayer().hasPermission("saneeconomy.signshop.create.admin")) {
            return;
        }

        ParsedSignShop pss = parseSignShop(evt);

        if (pss.error != null) {
            MessageUtils.sendMessage(evt.getPlayer(), String.format("Cannot create shop: %s", pss.error));
            return;
        }

        if (pss.shop == null) {
            return;
        }

        SignShop signShop = pss.shop;
        plugin.getSignShopManager().addSignShop(signShop);
        evt.setLine(0, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("admin-shop-title")));
        MessageUtils.sendMessage(evt.getPlayer(), "Sign shop created!");
        MessageUtils.sendMessage(evt.getPlayer(), String.format("Item: %d x %s", signShop.getQuantity(), signShop.getItem()));

        if (signShop.canBuy()) {
            MessageUtils.sendMessage(evt.getPlayer(), String.format("Will buy from players for %s.",
                    plugin.getSaneEconomy().getEconomyManager().getCurrency().formatAmount(signShop.getBuyPrice())
            ));
        }

        if (signShop.canSell()) {
            MessageUtils.sendMessage(evt.getPlayer(), String.format("Will sell to players for %s.",
                    plugin.getSaneEconomy().getEconomyManager().getCurrency().formatAmount(signShop.getSellPrice())
            ));
        }
    }

    private ParsedSignShop parseSignShop(SignChangeEvent evt) {
        String[] lines = evt.getLines();
        Player player = evt.getPlayer();
        Location location = evt.getBlock().getLocation();

        if ((lines[0] == null) || !lines[0].equalsIgnoreCase(plugin.getConfig().getString("admin-shop-trigger"))) { // First line must contain the trigger
            return new ParsedSignShop();
        }

        if (Strings.isNullOrEmpty(lines[1])) { // Second line must contain an item name
            return new ParsedSignShop("No item name specified.");
        }

        if (Strings.isNullOrEmpty(lines[2])) { // Second line must contain buy/sell prices
            return new ParsedSignShop("No buy/sell price(s) specified.");
        }

        if (Strings.isNullOrEmpty(lines[3])) { // Third line must contain item amount.
            return new ParsedSignShop("No item amount specified.");
        }

        String itemName = lines[1];
        String buySellRaw = lines[2];
        String amountRaw = lines[3];

        Material mat = Material.getMaterial(itemName.toUpperCase().replace(" ", "_"));

        if (mat == null) {
            // Invalid material.
            return new ParsedSignShop("Invalid item name specified.");
        }

        Matcher m = Pattern.compile("(B:(?<buy>[0-9.]+))?[ ]*(S:(?<sell>[0-9.]+))?").matcher(buySellRaw.trim());

        if (!m.matches()) {
            return new ParsedSignShop("Invalid buy/sell prices specified.");
        }

        double buy = Strings.isNullOrEmpty(m.group("buy")) ? -1.0 : Double.valueOf(m.group("buy"));
        double sell = Strings.isNullOrEmpty(m.group("sell")) ? -1.0 : Double.valueOf(m.group("sell"));

        if ((buy == -1) && (sell == -1)) {
            return new ParsedSignShop("Buy and sell amounts for this shop are both invalid.");
        }

        int itemAmount;

        try {
            itemAmount = Integer.valueOf(amountRaw);

            if (itemAmount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            return new ParsedSignShop("Item amount is not a positive integer.");
        }

        return new ParsedSignShop(new SignShop(player.getUniqueId(), location, mat, itemAmount, buy, sell));
    }

    private class ParsedSignShop {
        private SignShop shop;
        private String error;

        private ParsedSignShop(String error) {
            this.error = error;
        }

        private ParsedSignShop() {

        }

        private ParsedSignShop(SignShop shop) {
            this.shop = shop;
        }
    }
}