package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.shadowtrade.ShadowTradeGroup;
import ru.j2dev.gameserver.templates.shadowtrade.ShadowTradeItem;
import ru.j2dev.gameserver.templates.shadowtrade.ShadowTradeLoc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JunkyFunky
 * on 18.01.2018 21:58
 * group j2dev
 */
public class ShadowTradeHolder extends AbstractHolder {
    private final List<ShadowTradeGroup> groups = new ArrayList<>();
    private final List<ShadowTradeLoc> nightLocations = new ArrayList<>();
    private final List<ShadowTradeLoc> dayLocations = new ArrayList<>();

    public static ShadowTradeHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addNightLoc(ShadowTradeLoc location) {
        nightLocations.add(location);
    }

    public void addDayLoc(ShadowTradeLoc location) {
        dayLocations.add(location);
    }

    public ShadowTradeLoc getRndNightLoc() {
        return Rnd.get(nightLocations);
    }

    public ShadowTradeLoc getRndDayLoc() {
        return Rnd.get(dayLocations);
    }

    public void addShadowItem(ShadowTradeGroup shadowTradeItem) {
        groups.add(shadowTradeItem);
    }

    public ShadowTradeItem getRndTradeItems() {
        ShadowTradeGroup tradeGroup = null;
        boolean rndGeted = false;
        int chance = 0;
        for (ShadowTradeGroup group : groups) {
            if (!rndGeted) {
                chance += group.getChance();
                if (Rnd.chance(group.getChance() + chance)) {
                    tradeGroup = group;
                    rndGeted = true;
                }
            }
        }
        return Rnd.get(tradeGroup.getTradeItemList());
    }

    public NpcHtmlMessage generateHtmlMassege(final int objId, final ShadowTradeItem item, final int enchant) {
        NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(objId);
        npcHtmlMessage.setFile("shadowtrader/welcome.htm");
        npcHtmlMessage.replace("%name%", Functions.getItemName(item.getItemId()));
        npcHtmlMessage.replace("%icon%", ItemTemplateHolder.getInstance().getTemplate(item.getItemId()).getIcon());
        npcHtmlMessage.replace("%count%", String.valueOf(item.getCount()));
        npcHtmlMessage.replace("%enchant%", enchant > 0 ? String.valueOf(enchant) : "");
        npcHtmlMessage.replace("%price_name%", Functions.getItemName(item.getPriceItemId()));
        npcHtmlMessage.replace("%price_count%", String.valueOf(item.getPriceCount()));
        return npcHtmlMessage;
    }

    @Override
    public int size() {
        info("Loaded : " + nightLocations.size() + " nightLocations");
        info("Loaded : " + dayLocations.size() + " dayLocations");
        groups.stream().map(tradeGroup -> "Group " + tradeGroup.getName() + " : " + tradeGroup.getTradeItemList().size() + " items").forEach(this::info);
        return groups.stream().mapToInt(groups -> groups.getTradeItemList().size()).sum();
    }

    @Override
    public void clear() {
        groups.clear();
        nightLocations.clear();
    }

    private static class LazyHolder {
        private static final ShadowTradeHolder INSTANCE = new ShadowTradeHolder();
    }
}
