package ru.j2dev.gameserver.data.xml.parser;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractDirParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.TeleportLocation;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.entity.residence.ResidenceFunction;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.item.support.MerchantGuard;
import ru.j2dev.gameserver.utils.Location;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ResidenceParser extends AbstractDirParser<ResidenceHolder> {

    private ResidenceParser() {
        super(ResidenceHolder.getInstance());
    }

    public static ResidenceParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLDir() {
        return new File(Config.DATAPACK_ROOT, "data/xml/residences/");
    }

    @Override
    public boolean isIgnored(final File f) {
        return false;
    }

    @Override
    protected void readData(final ResidenceHolder holder, final Element rootElement) {
        final String impl = rootElement.getAttributeValue("impl");
        Class<?> clazz;
        final StatsSet set = new StatsSet();
        rootElement.getAttributes().forEach(element -> set.set(element.getName(), element.getValue()));
        Residence residence;
        try {
            clazz = Class.forName("ru.j2dev.gameserver.model.entity.residence." + impl);
            final Constructor constructor = clazz.getConstructor(StatsSet.class);
            residence = (Residence) constructor.newInstance(set);
            holder.addResidence(residence);
        } catch (Exception e) {
            error("fail to init: " + getCurrentFileName(), e);
            return;
        }
        for (final Element element2 : rootElement.getChildren()) {
            final String nodeName = element2.getName();
            final int level = (element2.getAttributeValue("level") == null) ? 0 : Integer.parseInt(element2.getAttributeValue("level"));
            final int lease = (int) (((element2.getAttributeValue("lease") == null) ? 0 : Integer.parseInt(element2.getAttributeValue("lease"))) * Config.RESIDENCE_LEASE_FUNC_MULTIPLIER);
            final int npcId = (element2.getAttributeValue("npcId") == null) ? 0 : Integer.parseInt(element2.getAttributeValue("npcId"));
            final int listId = (element2.getAttributeValue("listId") == null) ? 0 : Integer.parseInt(element2.getAttributeValue("listId"));
            ResidenceFunction function = null;
            if ("teleport".equalsIgnoreCase(nodeName)) {
                function = checkAndGetFunction(residence, 1);
                final List<TeleportLocation> targets = new ArrayList<>();
                element2.getChildren().stream().filter(teleportElement -> "target".equalsIgnoreCase(teleportElement.getName())).forEach(teleportElement -> {
                    final String name = teleportElement.getAttributeValue("name");
                    final long price = Long.parseLong(teleportElement.getAttributeValue("price"));
                    final int itemId = (teleportElement.getAttributeValue("item") == null) ? 57 : Integer.parseInt(teleportElement.getAttributeValue("item"));
                    final TeleportLocation loc = new TeleportLocation(itemId, price, name, 0);
                    loc.set(Location.parseLoc(teleportElement.getAttributeValue("loc")));
                    targets.add(loc);
                });
                function.addTeleports(level, targets.toArray(new TeleportLocation[0]));
            } else if ("support".equalsIgnoreCase(nodeName)) {
                if (level > 9 && !Config.ALT_CH_ALLOW_1H_BUFFS) {
                    continue;
                }
                function = checkAndGetFunction(residence, 6);
                function.addBuffs(level);
            } else if ("item_create".equalsIgnoreCase(nodeName)) {
                function = checkAndGetFunction(residence, 2);
                function.addBuylist(level, new int[]{npcId, listId});
            } else if ("curtain".equalsIgnoreCase(nodeName)) {
                function = checkAndGetFunction(residence, 7);
            } else if ("platform".equalsIgnoreCase(nodeName)) {
                function = checkAndGetFunction(residence, 8);
            } else if ("restore_exp".equalsIgnoreCase(nodeName)) {
                function = checkAndGetFunction(residence, 5);
            } else if ("restore_hp".equalsIgnoreCase(nodeName)) {
                function = checkAndGetFunction(residence, 3);
            } else if ("restore_mp".equalsIgnoreCase(nodeName)) {
                function = checkAndGetFunction(residence, 4);
            } else if ("skills".equalsIgnoreCase(nodeName)) {
                element2.getChildren().forEach(nextElement -> {
                    final int id2 = Integer.parseInt(nextElement.getAttributeValue("id"));
                    final int level2 = Integer.parseInt(nextElement.getAttributeValue("level"));
                    final Skill skill = SkillTable.getInstance().getInfo(id2, level2);
                    if (skill != null) {
                        residence.addSkill(skill);
                    }
                });
            } else if ("banish_points".equalsIgnoreCase(nodeName)) {
                element2.getChildren().stream().map(Location::parse).forEach(residence::addBanishPoint);
            } else if ("owner_restart_points".equalsIgnoreCase(nodeName)) {
                element2.getChildren().stream().map(Location::parse).forEach(residence::addOwnerRestartPoint);
            } else if ("other_restart_points".equalsIgnoreCase(nodeName)) {
                element2.getChildren().stream().map(Location::parse).forEach(residence::addOtherRestartPoint);
            } else if ("chaos_restart_points".equalsIgnoreCase(nodeName)) {
                element2.getChildren().stream().map(Location::parse).forEach(residence::addChaosRestartPoint);
            } else if ("merchant_guards".equalsIgnoreCase(nodeName)) {
                element2.getChildren().forEach(subElement -> {
                    final int itemId2 = Integer.parseInt(subElement.getAttributeValue("item_id"));
                    final int npcId2 = Integer.parseInt(subElement.getAttributeValue("npc_id"));
                    final int maxGuard = Integer.parseInt(subElement.getAttributeValue("max"));
                    final TIntSet intSet = new TIntHashSet(3);
                    final String[] ssq = subElement.getAttributeValue("ssq").split(";");
                    Arrays.stream(ssq).forEach(q -> {
                        if ("cabal_null".equalsIgnoreCase(q)) {
                            intSet.add(0);
                        } else if ("cabal_dusk".equalsIgnoreCase(q)) {
                            intSet.add(1);
                        } else if ("cabal_dawn".equalsIgnoreCase(q)) {
                            intSet.add(2);
                        } else {
                            error("Unknown ssq type: " + q + "; file: " + getCurrentFileName());
                        }
                    });
                    ((Castle) residence).addMerchantGuard(new MerchantGuard(itemId2, npcId2, maxGuard, intSet));
                });
            }
            if (function != null) {
                function.addLease(level, lease);
            }
        }
    }

    private ResidenceFunction checkAndGetFunction(final Residence residence, final int type) {
        ResidenceFunction function = residence.getFunction(type);
        if (function == null) {
            function = new ResidenceFunction(residence.getId(), type);
            residence.addFunction(function);
        }
        return function;
    }

    private static class LazyHolder {
        protected static final ResidenceParser INSTANCE = new ResidenceParser();
    }
}
