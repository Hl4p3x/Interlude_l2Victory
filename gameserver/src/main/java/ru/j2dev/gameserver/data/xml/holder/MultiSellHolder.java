package ru.j2dev.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.MultiSellEntry;
import ru.j2dev.gameserver.model.base.MultiSellIngredient;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MultiSellList;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.XMLUtil;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultiSellHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiSellHolder.class);
    private static final String NODE_PRODUCTION = "production";
    private static final String NODE_INGRIDIENT = "ingredient";
    private static final MultiSellHolder _instance = new MultiSellHolder();

    private final TIntObjectHashMap<MultiSellListContainer> entries = new TIntObjectHashMap<>();

    public MultiSellHolder() {
        parseData();
    }

    public static MultiSellHolder getInstance() {
        return _instance;
    }

    private static long[] parseItemIdAndCount(final String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        final String[] a = s.split(":");
        try {
            final long id = Integer.parseInt(a[0]);
            final long count = (a.length > 1) ? Long.parseLong(a[1]) : 1L;
            return new long[]{id, count};
        } catch (Exception e) {
            LOGGER.error("", e);
            return null;
        }
    }

    public static MultiSellEntry parseEntryFromStr(final String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        final String[] a = s.split("->");
        if (a.length != 2) {
            return null;
        }
        final long[] ingredient;
        final long[] production;
        if ((ingredient = parseItemIdAndCount(a[0])) == null || (production = parseItemIdAndCount(a[1])) == null) {
            return null;
        }
        final MultiSellEntry entry = new MultiSellEntry();
        entry.addIngredient(new MultiSellIngredient((int) ingredient[0], ingredient[1]));
        entry.addProduct(new MultiSellIngredient((int) production[0], production[1]));
        return entry;
    }

    public MultiSellListContainer getList(final int id) {
        return entries.get(id);
    }

    public void reload() {
        parseData();
    }

    private void parseData() {
        entries.clear();
        parse();
    }

    private void hashFiles(final String dirname, final List<File> hash) {
        final File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
        if (!dir.exists()) {
            LOGGER.info("Dir " + dir.getAbsolutePath() + " not exists");
            return;
        }
        final File[] listFiles = dir.listFiles();
        for (final File f : Objects.requireNonNull(listFiles)) {
            if (f.getName().endsWith(".xml")) {
                hash.add(f);
            } else if (f.isDirectory() && !".svn".equals(f.getName())) {
                hashFiles(dirname + "/" + f.getName(), hash);
            }
        }
    }

    public void addMultiSellListContainer(final int id, final MultiSellListContainer list) {
        if (entries.containsKey(id)) {
            LOGGER.warn("MultiSell redefined: " + id);
        }
        list.setListId(id);
        entries.put(id, list);
    }

    public MultiSellListContainer remove(final String s) {
        return remove(new File(s));
    }

    public MultiSellListContainer remove(final File f) {
        return remove(Integer.parseInt(f.getName().replaceAll(".xml", "")));
    }

    public MultiSellListContainer remove(final int id) {
        return entries.remove(id);
    }

    public void parseFile(final File f) {
        int id;
        try {
            id = Integer.parseInt(f.getName().replaceAll(".xml", "").split(" ")[0]);
        } catch (Exception e) {
            LOGGER.error("Error loading file " + f, e);
            return;
        }
        Document doc;
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            doc = factory.newDocumentBuilder().parse(f);
        } catch (Exception e2) {
            LOGGER.error("Error loading file " + f, e2);
            return;
        }
        try {
            addMultiSellListContainer(id, parseDocument(doc, id));
        } catch (Exception e2) {
            LOGGER.error("Error in file " + f, e2);
        }
    }

    private void parse() {
        final List<File> files = new ArrayList<>();
        hashFiles("multisell", files);
        for (final File f : files) {
            parseFile(f);
        }
    }

    protected MultiSellListContainer parseDocument(final Document doc, final int id) {
        final MultiSellListContainer list = new MultiSellListContainer();
        int entId = 1;
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("list".equalsIgnoreCase(n.getNodeName())) {
                for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                    if ("item".equalsIgnoreCase(d.getNodeName())) {
                        final MultiSellEntry e = parseEntry(d, id);
                        if (e != null) {
                            e.setEntryId(entId++);
                            list.addEntry(e);
                        }
                    } else if ("config".equalsIgnoreCase(d.getNodeName())) {
                        list.setShowAll(XMLUtil.getAttributeBooleanValue(d, "showall", true));
                        list.setNoTax(XMLUtil.getAttributeBooleanValue(d, "notax", false));
                        list.setKeepEnchant(XMLUtil.getAttributeBooleanValue(d, "keepenchanted", false));
                        list.setNoKey(XMLUtil.getAttributeBooleanValue(d, "nokey", false));
                    }
                }
            }
        }
        return list;
    }

    protected MultiSellEntry parseEntry(final Node n, final int multiSellId) {
        final MultiSellEntry entry = new MultiSellEntry();
        for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
            if (NODE_INGRIDIENT.equalsIgnoreCase(d.getNodeName())) {
                final int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
                final long count = Long.parseLong(d.getAttributes().getNamedItem("count").getNodeValue());
                final MultiSellIngredient mi = new MultiSellIngredient(id, count);
                if (d.getAttributes().getNamedItem("enchant") != null) {
                    mi.setItemEnchant(Integer.parseInt(d.getAttributes().getNamedItem("enchant").getNodeValue()));
                }
                if (d.getAttributes().getNamedItem("mantainIngredient") != null) {
                    mi.setMantainIngredient(Boolean.parseBoolean(d.getAttributes().getNamedItem("mantainIngredient").getNodeValue()));
                }
                if (d.getAttributes().getNamedItem("fireAttr") != null) {
                    mi.getItemAttributes().setFire(Integer.parseInt(d.getAttributes().getNamedItem("fireAttr").getNodeValue()));
                }
                if (d.getAttributes().getNamedItem("waterAttr") != null) {
                    mi.getItemAttributes().setWater(Integer.parseInt(d.getAttributes().getNamedItem("waterAttr").getNodeValue()));
                }
                if (d.getAttributes().getNamedItem("earthAttr") != null) {
                    mi.getItemAttributes().setEarth(Integer.parseInt(d.getAttributes().getNamedItem("earthAttr").getNodeValue()));
                }
                if (d.getAttributes().getNamedItem("windAttr") != null) {
                    mi.getItemAttributes().setWind(Integer.parseInt(d.getAttributes().getNamedItem("windAttr").getNodeValue()));
                }
                if (d.getAttributes().getNamedItem("holyAttr") != null) {
                    mi.getItemAttributes().setHoly(Integer.parseInt(d.getAttributes().getNamedItem("holyAttr").getNodeValue()));
                }
                if (d.getAttributes().getNamedItem("unholyAttr") != null) {
                    mi.getItemAttributes().setUnholy(Integer.parseInt(d.getAttributes().getNamedItem("unholyAttr").getNodeValue()));
                }
                entry.addIngredient(mi);
            } else if (NODE_PRODUCTION.equalsIgnoreCase(d.getNodeName())) {
                final int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
                final long count = Long.parseLong(d.getAttributes().getNamedItem("count").getNodeValue());
                final MultiSellIngredient mi = new MultiSellIngredient(id, count);
                if (d.getAttributes().getNamedItem("enchant") != null) {
                    mi.setItemEnchant(Integer.parseInt(d.getAttributes().getNamedItem("enchant").getNodeValue()));
                }
                if (d.getAttributes().getNamedItem("fireAttr") != null) {
                    mi.getItemAttributes().setFire(Integer.parseInt(d.getAttributes().getNamedItem("fireAttr").getNodeValue()));
                }
                if (d.getAttributes().getNamedItem("waterAttr") != null) {
                    mi.getItemAttributes().setWater(Integer.parseInt(d.getAttributes().getNamedItem("waterAttr").getNodeValue()));
                }
                if (d.getAttributes().getNamedItem("earthAttr") != null) {
                    mi.getItemAttributes().setEarth(Integer.parseInt(d.getAttributes().getNamedItem("earthAttr").getNodeValue()));
                }
                if (d.getAttributes().getNamedItem("windAttr") != null) {
                    mi.getItemAttributes().setWind(Integer.parseInt(d.getAttributes().getNamedItem("windAttr").getNodeValue()));
                }
                if (d.getAttributes().getNamedItem("holyAttr") != null) {
                    mi.getItemAttributes().setHoly(Integer.parseInt(d.getAttributes().getNamedItem("holyAttr").getNodeValue()));
                }
                if (d.getAttributes().getNamedItem("unholyAttr") != null) {
                    mi.getItemAttributes().setUnholy(Integer.parseInt(d.getAttributes().getNamedItem("unholyAttr").getNodeValue()));
                }
                if (!Config.ALT_ALLOW_SHADOW_WEAPONS && id > 0) {
                    final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(id);
                    if (item != null && item.isShadowItem() && item.isWeapon() && !Config.ALT_ALLOW_SHADOW_WEAPONS) {
                        return null;
                    }
                }
                entry.addProduct(mi);
            }
        }
        if (entry.getIngredients().isEmpty() || entry.getProduction().isEmpty()) {
            LOGGER.warn("MultiSell [" + multiSellId + "] is empty!");
            return null;
        }
        if (entry.getIngredients().size() == 1 && entry.getProduction().size() == 1 && entry.getIngredients().get(0).getItemId() == 57) {
            final ItemTemplate item2 = ItemTemplateHolder.getInstance().getTemplate(entry.getProduction().get(0).getItemId());
            if (item2 == null) {
                LOGGER.warn("MultiSell [" + multiSellId + "] Production [" + entry.getProduction().get(0).getItemId() + "] not found!");
                return null;
            }
            final long refPrice = entry.getProduction().get(0).getItemCount() * item2.getReferencePrice();
            if (refPrice > entry.getIngredients().get(0).getItemCount()) {
                LOGGER.warn("MultiSell [" + multiSellId + "] Production '" + item2.getName() + "' [" + entry.getProduction().get(0).getItemId() + "] price is lower than referenced | " + refPrice + " > " + entry.getIngredients().get(0).getItemCount());
            }
        }
        return entry;
    }

    public void SeparateAndSend(final int listId, final Player player, final double taxRate) {
        for (final int i : Config.ALT_DISABLED_MULTISELL) {
            if (i == listId) {
                player.sendMessage(new CustomMessage("common.Disabled", player));
                return;
            }
        }
        final MultiSellListContainer list = getList(listId);
        if (list == null) {
            player.sendMessage(new CustomMessage("common.Disabled", player));
            return;
        }
        SeparateAndSend(list, player, taxRate);
    }

    public void SeparateAndSend(MultiSellListContainer list, final Player player, final double taxRate) {
        list = generateMultiSell(list, player, taxRate);
        MultiSellListContainer temp = new MultiSellListContainer();
        int page = 1;
        temp.setListId(list.getListId());
        player.setMultisell(list);
        for (final MultiSellEntry e : list.getEntries()) {
            if (temp.getEntries().size() == Config.MULTISELL_SIZE) {
                player.sendPacket(new MultiSellList(temp, page, 0));
                ++page;
                temp = new MultiSellListContainer();
                temp.setListId(list.getListId());
            }
            temp.addEntry(e);
        }
        player.sendPacket(new MultiSellList(temp, page, 1));
    }

    private MultiSellListContainer generateMultiSell(final MultiSellListContainer container, final Player player, final double taxRate) {
        final MultiSellListContainer list = new MultiSellListContainer();
        list.setListId(container.getListId());
        final boolean enchant = container.isKeepEnchant();
        final boolean notax = container.isNoTax();
        final boolean showall = container.isShowAll();
        final boolean nokey = container.isNoKey();
        list.setShowAll(showall);
        list.setKeepEnchant(enchant);
        list.setNoTax(notax);
        list.setNoKey(nokey);
        final List<ItemInstance> items = player.getInventory().getItems();
        for (final MultiSellEntry origEntry : container.getEntries()) {
            final MultiSellEntry ent = origEntry.clone();
            List<MultiSellIngredient> ingridients;
            if (!notax && taxRate > 0.0) {
                double tax = 0.0;
                long adena = 0L;
                ingridients = new ArrayList<>(ent.getIngredients().size() + 1);
                for (final MultiSellIngredient i : ent.getIngredients()) {
                    if (i.getItemId() == 57) {
                        adena += i.getItemCount();
                        tax += i.getItemCount() * taxRate;
                    } else {
                        ingridients.add(i);
                        if (i.getItemId() == -200) {
                            tax += i.getItemCount() / 120L * 1000L * taxRate * 100.0;
                        }
                        if (i.getItemId() < 1) {
                            continue;
                        }
                        final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(i.getItemId());
                        if (!item.isStackable()) {
                            continue;
                        }
                        tax += item.getReferencePrice() * i.getItemCount() * taxRate;
                    }
                }
                adena = Math.round(adena + tax);
                if (adena > 0L) {
                    ingridients.add(new MultiSellIngredient(57, adena));
                }
                ent.setTax(Math.round(tax));
                ent.getIngredients().clear();
                ent.getIngredients().addAll(ingridients);
            } else {
                ingridients = ent.getIngredients();
            }
            if (showall) {
                list.entries.add(ent);
            } else {
                final List<Integer> itms = new ArrayList<>();
                for (final MultiSellIngredient ingredient : ingridients) {
                    final ItemTemplate template = (ingredient.getItemId() <= 0) ? null : ItemTemplateHolder.getInstance().getTemplate(ingredient.getItemId());
                    if (ingredient.getItemId() <= 0 || nokey || template.isEquipment()) {
                        if (ingredient.getItemId() == 12374) {
                            continue;
                        }
                        switch (ingredient.getItemId()) {
                            case -200:
                                if (itms.contains(ingredient.getItemId()) || player.getClan() == null || player.getClan().getReputationScore() < ingredient.getItemCount()) {
                                    continue;
                                }
                                itms.add(ingredient.getItemId());
                                break;
                            case -100:
                                if (itms.contains(ingredient.getItemId()) || player.getPcBangPoints() < ingredient.getItemCount()) {
                                    continue;
                                }
                                itms.add(ingredient.getItemId());
                                break;
                            default:
                                for (final ItemInstance item2 : items) {
                                    if (item2.getItemId() == ingredient.getItemId() && item2.canBeExchanged(player)) {
                                        if (!itms.contains(enchant ? (ingredient.getItemId() + ingredient.getItemEnchant() * 100000L) : ingredient.getItemId())) {
                                            if (item2.getEnchantLevel() >= ingredient.getItemEnchant()) {
                                                if (item2.isStackable() && item2.getCount() < ingredient.getItemCount()) {
                                                    break;
                                                }
                                                itms.add(enchant ? (ingredient.getItemId() + ingredient.getItemEnchant() * 100000) : ingredient.getItemId());
                                                final MultiSellEntry possibleEntry = new MultiSellEntry(enchant ? (ent.getEntryId() + item2.getEnchantLevel() * 100000) : ent.getEntryId());
                                                for (final MultiSellIngredient p : ent.getProduction()) {
                                                    if (enchant && template.canBeEnchanted(true)) {
                                                        p.setItemEnchant(item2.getEnchantLevel());
                                                        p.setItemAttributes(item2.getAttributes().clone());
                                                    }
                                                    possibleEntry.addProduct(p);
                                                }
                                                for (final MultiSellIngredient ig : ingridients) {
                                                    if (enchant && ig.getItemId() > 0 && ItemTemplateHolder.getInstance().getTemplate(ig.getItemId()).canBeEnchanted(true)) {
                                                        ig.setItemEnchant(item2.getEnchantLevel());
                                                        ig.setItemAttributes(item2.getAttributes().clone());
                                                    }
                                                    possibleEntry.addIngredient(ig);
                                                }
                                                list.entries.add(possibleEntry);
                                                break;
                                            }
                                        }
                                    }
                                }
                                break;
                        }
                    }
                }
            }
        }
        return list;
    }

    public static class MultiSellListContainer {
        private final List<MultiSellEntry> entries;
        private int _listId;
        private boolean _showall;
        private boolean keep_enchanted;
        private boolean is_dutyfree;
        private boolean nokey;

        public MultiSellListContainer() {
            _showall = true;
            keep_enchanted = false;
            is_dutyfree = false;
            nokey = false;
            entries = new ArrayList<>();
        }

        public int getListId() {
            return _listId;
        }

        public void setListId(final int listId) {
            _listId = listId;
        }

        public boolean isShowAll() {
            return _showall;
        }

        public void setShowAll(final boolean bool) {
            _showall = bool;
        }

        public boolean isNoTax() {
            return is_dutyfree;
        }

        public void setNoTax(final boolean bool) {
            is_dutyfree = bool;
        }

        public boolean isNoKey() {
            return nokey;
        }

        public void setNoKey(final boolean bool) {
            nokey = bool;
        }

        public boolean isKeepEnchant() {
            return keep_enchanted;
        }

        public void setKeepEnchant(final boolean bool) {
            keep_enchanted = bool;
        }

        public void addEntry(final MultiSellEntry e) {
            entries.add(e);
        }

        public List<MultiSellEntry> getEntries() {
            return entries;
        }

        public boolean isEmpty() {
            return entries.isEmpty();
        }
    }
}
