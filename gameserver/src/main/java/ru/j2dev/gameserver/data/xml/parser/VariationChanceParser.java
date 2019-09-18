package ru.j2dev.gameserver.data.xml.parser;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.commons.util.RandomUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.VariationChanceHolder;
import ru.j2dev.gameserver.templates.item.support.VariationChanceData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VariationChanceParser extends AbstractFileParser<VariationChanceHolder> {

    private VariationChanceParser() {
        super(VariationChanceHolder.getInstance());
    }

    public static VariationChanceParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/variation/variation_data.xml");
    }

    private List<Pair<List<Pair<Integer, Double>>, Double>> readGroups(final Element groupRootElement) {
        final List<Pair<List<Pair<Integer, Double>>, Double>> variation = new ArrayList<>();
        groupRootElement.getChildren().stream().filter(ge -> "group".equalsIgnoreCase(ge.getName())).forEach(ge -> {
            final double groupChance = Double.parseDouble(ge.getAttributeValue("chance"));
            final List<Pair<Integer, Double>> groupOptions = new ArrayList<>();
            ge.getChildren().forEach(oe -> {
                final int id = Integer.parseInt(oe.getAttributeValue("id"));
                final double chance = Double.parseDouble(oe.getAttributeValue("chance"));
                groupOptions.add(new ImmutablePair<>(id, chance));
            });
            groupOptions.sort(RandomUtils.DOUBLE_GROUP_COMPARATOR);
            variation.add(new ImmutablePair<>(groupOptions, groupChance));
        });
        return variation;
    }

    @Override
    protected void readData(final VariationChanceHolder holder, final Element rootElement) {
        for (final Element vde : rootElement.getChildren()) {
            final int mineralId = Integer.parseInt(vde.getAttributeValue("mineralId"));
            final boolean isMage = false;
            VariationChanceData warriorData = null;
            VariationChanceData mageData = null;
            for (final Element oe : vde.getChildren()) {
                final String typeStr = oe.getAttributeValue("type");
                List<Pair<List<Pair<Integer, Double>>, Double>> variation1 = null;
                List<Pair<List<Pair<Integer, Double>>, Double>> variation2 = null;
                for (final Element ve : oe.getChildren()) {
                    if ("variation1".equalsIgnoreCase(ve.getName())) {
                        final List<Pair<List<Pair<Integer, Double>>, Double>> variation3 = readGroups(ve);
                        variation3.sort(RandomUtils.DOUBLE_GROUP_COMPARATOR);
                        variation1 = variation3;
                    } else {
                        if (!"variation2".equalsIgnoreCase(ve.getName())) {
                            continue;
                        }
                        final List<Pair<List<Pair<Integer, Double>>, Double>> variation3 = readGroups(ve);
                        variation3.sort(RandomUtils.DOUBLE_GROUP_COMPARATOR);
                        variation2 = variation3;
                    }
                }
                if ("WARRIOR".equalsIgnoreCase(typeStr)) {
                    warriorData = new VariationChanceData(mineralId, (variation1 != null) ? variation1 : Collections.emptyList(), (variation2 != null) ? variation2 : Collections.emptyList());
                } else {
                    if (!"MAGE".equalsIgnoreCase(typeStr)) {
                        throw new RuntimeException("Unknown type " + typeStr);
                    }
                    mageData = new VariationChanceData(mineralId, (variation1 != null) ? variation1 : Collections.emptyList(), (variation2 != null) ? variation2 : Collections.emptyList());
                }
            }
            holder.add(new ImmutablePair<>(warriorData, mageData));
        }
    }

    private static class LazyHolder {
        protected static final VariationChanceParser INSTANCE = new VariationChanceParser();
    }
}
