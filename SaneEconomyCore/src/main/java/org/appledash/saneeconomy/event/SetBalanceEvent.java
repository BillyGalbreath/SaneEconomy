package org.appledash.saneeconomy.event;

import org.appledash.saneeconomy.economy.economable.Economable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SetBalanceEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Economable target;
    private final double newAmount;

    public SetBalanceEvent(Economable target, double newAmount) {
        super();
        this.target = target;
        this.newAmount = newAmount;
    }

    public Economable getTarget() {
        return target;
    }

    public double getNewAmount() {
        return newAmount;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
