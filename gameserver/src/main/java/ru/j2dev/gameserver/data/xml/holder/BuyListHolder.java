package ru.j2dev.gameserver.data.xml.holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import ru.j2dev.commons.collections.IntMap;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.items.TradeItem;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BuyListHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(BuyListHolder.class);
    private static BuyListHolder _instance;

    private final IntMap<NpcTradeList> _lists = new IntMap<>();

    private BuyListHolder() {
        try {
            final File filelists = new File(Config.DATAPACK_ROOT, "data/merchant_filelists.xml");
            final DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
            factory1.setValidating(false);
            factory1.setIgnoringComments(true);
            final Document doc1 = factory1.newDocumentBuilder().parse(filelists);
            int counterFiles = 0;
            int counterItems = 0;
            for (Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
                if ("list".equalsIgnoreCase(n1.getNodeName())) {
                    for (Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling()) {
                        if ("file".equalsIgnoreCase(d1.getNodeName())) {
                            final String filename = d1.getAttributes().getNamedItem("name").getNodeValue();
                            final File file = new File(Config.DATAPACK_ROOT, "data/" + filename);
                            final DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
                            factory2.setValidating(false);
                            factory2.setIgnoringComments(true);
                            final Document doc2 = factory2.newDocumentBuilder().parse(file);
                            ++counterFiles;
                            for (Node n2 = doc2.getFirstChild(); n2 != null; n2 = n2.getNextSibling()) {
                                if ("list".equalsIgnoreCase(n2.getNodeName())) {
                                    for (Node d2 = n2.getFirstChild(); d2 != null; d2 = d2.getNextSibling()) {
                                        if ("tradelist".equalsIgnoreCase(d2.getNodeName())) {
                                            final String[] npcs = d2.getAttributes().getNamedItem("npc").getNodeValue().split(";");
                                            final String[] shopIds = d2.getAttributes().getNamedItem("shop").getNodeValue().split(";");
                                            String[] markups = new String[0];
                                            boolean haveMarkups = false;
                                            if (d2.getAttributes().getNamedItem("markup") != null) {
                                                markups = d2.getAttributes().getNamedItem("markup").getNodeValue().split(";");
                                                haveMarkups = true;
                                            }
                                            final int size = npcs.length;
                                            if (!haveMarkups) {
                                                markups = new String[size];
                                                for (int i = 0; i < size; ++i) {
                                                    markups[i] = "0";
                                                }
                                            }
                                            if (shopIds.length != size || markups.length != size) {
                                                LOGGER.warn("Do not correspond to the size of arrays");
                                            } else {
                                                for (int n3 = 0; n3 < size; ++n3) {
                                                    final int npc_id = Integer.parseInt(npcs[n3]);
                                                    final int shop_id = Integer.parseInt(shopIds[n3]);
                                                    final double markup = (npc_id > 0) ? (1.0 + Double.parseDouble(markups[n3]) / 100.0) : 0.0;
                                                    final NpcTradeList tl = new NpcTradeList(shop_id);
                                                    tl.setNpcId(npc_id);
                                                    for (Node j = d2.getFirstChild(); j != null; j = j.getNextSibling()) {
                                                        if ("item".equalsIgnoreCase(j.getNodeName())) {
                                                            final int itemId = Integer.parseInt(j.getAttributes().getNamedItem("id").getNodeValue());
                                                            final ItemTemplate template = ItemTemplateHolder.getInstance().getTemplate(itemId);
                                                            if (template == null) {
                                                                LOGGER.warn("Template not found for itemId: " + itemId + " for shop " + shop_id);
                                                            } else if (checkItem(template)) {
                                                                ++counterItems;
                                                                final long price = (j.getAttributes().getNamedItem("price") != null) ? Long.parseLong(j.getAttributes().getNamedItem("price").getNodeValue()) : Math.round(template.getReferencePrice() * markup);
                                                                final TradeItem item = new TradeItem();
                                                                item.setItemId(itemId);
                                                                final int itemCount = (j.getAttributes().getNamedItem("count") != null) ? Integer.parseInt(j.getAttributes().getNamedItem("count").getNodeValue()) : 0;
                                                                final int itemRechargeTime = (j.getAttributes().getNamedItem("time") != null) ? Integer.parseInt(j.getAttributes().getNamedItem("time").getNodeValue()) : 0;
                                                                item.setOwnersPrice(price);
                                                                item.setCount(itemCount);
                                                                item.setCurrentValue(itemCount);
                                                                item.setLastRechargeTime((int) (System.currentTimeMillis() / 60000L));
                                                                item.setRechargeTime(itemRechargeTime);
                                                                tl.addItem(item);
                                                            }
                                                        }
                                                    }
                                                    _lists.put(shop_id, tl);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            LOGGER.info("TradeController: Loaded " + counterFiles + " file(s).");
            LOGGER.info("TradeController: Loaded " + counterItems + " Items.");
            LOGGER.info("TradeController: Loaded " + _lists.size() + " Buylists.");
        } catch (Exception e) {
            LOGGER.warn("TradeController: Buylists could not be initialized.");
            LOGGER.error("", e);
        }
    }

    public static BuyListHolder getInstance() {
        if (_instance == null) {
            _instance = new BuyListHolder();
        }
        return _instance;
    }

    public static void reload() {
        _instance = new BuyListHolder();
    }

    private boolean checkItem(final ItemTemplate template) {
        if (template.isEquipment() && !template.isForPet() && Config.ALT_SHOP_PRICE_LIMITS.length > 0) {
            int i = 0;
            while (i < Config.ALT_SHOP_PRICE_LIMITS.length) {
                if (template.getBodyPart() == Config.ALT_SHOP_PRICE_LIMITS[i]) {
                    if (template.getReferencePrice() > Config.ALT_SHOP_PRICE_LIMITS[i + 1]) {
                        return false;
                    }
                    break;
                } else {
                    i += 2;
                }
            }
        }
        if (Config.ALT_SHOP_UNALLOWED_ITEMS.length > 0) {
            for (final int j : Config.ALT_SHOP_UNALLOWED_ITEMS) {
                if (template.getItemId() == j) {
                    return false;
                }
            }
        }
        return true;
    }

    public NpcTradeList getBuyList(final int listId) {
        return _lists.get(listId);
    }

    public void addToBuyList(final int listId, final NpcTradeList list) {
        _lists.put(listId, list);
    }

    public static class NpcTradeList {
        private final List<TradeItem> tradeList;
        private final int _id;
        private int _npcId;

        public NpcTradeList(final int id) {
            tradeList = new ArrayList<>();
            _id = id;
        }

        public int getListId() {
            return _id;
        }

        public int getNpcId() {
            return _npcId;
        }

        public void setNpcId(final int id) {
            _npcId = id;
        }

        public void addItem(final TradeItem ti) {
            tradeList.add(ti);
        }

        public synchronized List<TradeItem> getItems() {
            final List<TradeItem> result = new ArrayList<>();
            final long currentTime = System.currentTimeMillis() / 60000L;
            tradeList.forEach(ti -> {
                if (ti.isCountLimited()) {
                    if (ti.getCurrentValue() < ti.getCount() && ti.getLastRechargeTime() + ti.getRechargeTime() <= currentTime) {
                        ti.setLastRechargeTime(ti.getLastRechargeTime() + ti.getRechargeTime());
                        ti.setCurrentValue(ti.getCount());
                    }
                    if (ti.getCurrentValue() == 0L) {
                        return;
                    }
                }
                result.add(ti);
            });
            return result;
        }

        public TradeItem getItemByItemId(final int itemId) {
            return tradeList.stream().filter(ti -> ti.getItemId() == itemId).findFirst().orElse(null);
        }

        public synchronized void updateItems(final List<TradeItem> buyList) {
            buyList.forEach(ti -> {
                final TradeItem ic = getItemByItemId(ti.getItemId());
                if (ic.isCountLimited()) {
                    ic.setCurrentValue(Math.max(ic.getCurrentValue() - ti.getCount(), 0L));
                }
            });
        }
    }
}
