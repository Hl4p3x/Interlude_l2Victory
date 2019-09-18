package ru.j2dev.gameserver.templates.shadowtrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by JunkyFunky
 * on 26.01.2018 9:38
 * group j2dev
 */
public class ShadowTradeGroup {
    private int chance;
    private String name;
    private List<ShadowTradeItem> tradeItemList = Collections.emptyList();

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ShadowTradeItem> getTradeItemList() {
        return tradeItemList;
    }

    public void addShadowTradeItem(ShadowTradeItem tradeItem) {
        if (tradeItemList.isEmpty()) {
            tradeItemList = new ArrayList<>();
        }
        tradeItemList.add(tradeItem);
    }
}
