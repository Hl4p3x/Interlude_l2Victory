package services.community;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.RecipeHolder;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.handler.bbs.CommunityBoardManager;
import ru.j2dev.gameserver.handler.bbs.ICommunityBoardHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Recipe;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.items.ManufactureItem;
import ru.j2dev.gameserver.model.items.TradeItem;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RadarControl;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShowBoard;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.item.ItemGrade;
import ru.j2dev.gameserver.utils.MapUtils;
import ru.j2dev.gameserver.utils.Util;

import java.util.*;

public class RegionCommunity implements OnInitScriptListener, ICommunityBoardHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegionCommunity.class);
    private static final TownEntry[] _towns = {new TownEntry("Gatekeeper.TheTownofGludio", 19, 21), new TownEntry("Gatekeeper.TheTownofDion", 20, 22), new TownEntry("Gatekeeper.TheTownofGiran", 22, 22), new TownEntry("Gatekeeper.TheTownofOren", 22, 19), new TownEntry("Gatekeeper.TheTownofAden", 24, 18), new TownEntry("Gatekeeper.Heine", 23, 24), new TownEntry("Gatekeeper.TheTownofGoddard", 24, 16), new TownEntry("Gatekeeper.RuneTownship", 21, 16), new TownEntry("Gatekeeper.TheTownofSchuttgart", 22, 13)};
    private static final String[] _regionTypes = {"&$596;", "&$597;", "&$665;"};
    private static final String[] _elements = {"&$1622;", "&$1623;", "&$1624;", "&$1625;", "&$1626;", "&$1627;"};
    private static final String[] _grade = {"&$1291;", "&$1292;", "&$1293;", "&$1294;", "&$1295;", "S80 Grade", "S84 Grade"};
    private static final int SELLER_PER_PAGE = 12;

    private static List<Player> getSellersList(final int townId, final int type, final String search, final boolean byItem) {
        List<Player> list = new ArrayList<>();
        final TownEntry town = RegionCommunity._towns[townId];
        final int rx = town.getX();
        final int ry = town.getY();
        final int offset = 0;
        for (final Player seller : GameObjectsStorage.getPlayers()) {
            final int tx = MapUtils.regionX(seller);
            final int ty = MapUtils.regionY(seller);
            if (tx >= rx - offset && tx <= rx + offset && ty >= ry - offset && ty <= ry + offset) {
                final List<TradeItem> tl = seller.getTradeList();
                final List<ManufactureItem> cl = seller.getCreateList();
                if (seller.getPrivateStoreType() <= 0) {
                    continue;
                }
                if (type == 0 && tl != null && (seller.getPrivateStoreType() == 1 || seller.getPrivateStoreType() == 8)) {
                    list.add(seller);
                } else if (type == 1 && tl != null && seller.getPrivateStoreType() == 3) {
                    list.add(seller);
                } else {
                    if (type != 2 || cl == null || seller.getPrivateStoreType() != 5) {
                        continue;
                    }
                    list.add(seller);
                }
            }
        }
        if (!search.isEmpty() && !list.isEmpty()) {
            final List<Player> s_list = new ArrayList<>();
            for (final Player seller2 : list) {
                final List<TradeItem> tl2 = seller2.getTradeList();
                final List<ManufactureItem> cl2 = seller2.getCreateList();
                if (byItem) {
                    if ((type == 0 || type == 1) && tl2 != null) {
                        final List<TradeItem> sl = (type == 0) ? seller2.getSellList() : seller2.getBuyList();
                        if (sl == null) {
                            continue;
                        }
                        for (final TradeItem ti : sl) {
                            final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(ti.getItemId());
                            if (item != null && item.getName() != null && item.getName().toLowerCase().contains(search)) {
                                s_list.add(seller2);
                                break;
                            }
                        }
                    } else {
                        if (type != 2 || cl2 == null) {
                            continue;
                        }
                        for (final ManufactureItem mi : cl2) {
                            final Recipe recipe = RecipeHolder.getInstance().getRecipeById(mi.getRecipeId() - 1);
                            if (recipe != null && !recipe.getProducts().isEmpty()) {
                                final ItemTemplate item = recipe.getProducts().get(0).getKey();
                                if (item != null && item.getName() != null && item.getName().toLowerCase().contains(search)) {
                                    s_list.add(seller2);
                                    break;
                                }
                            }
                        }
                    }
                } else if (type == 0 && tl2 != null && seller2.getSellStoreName() != null && seller2.getSellStoreName().toLowerCase().contains(search)) {
                    s_list.add(seller2);
                } else if (type == 1 && tl2 != null && seller2.getBuyStoreName() != null && seller2.getBuyStoreName().toLowerCase().contains(search)) {
                    s_list.add(seller2);
                } else {
                    if (type != 2 || cl2 == null || seller2.getCreateList() == null || seller2.getManufactureName() == null || !seller2.getManufactureName().toLowerCase().contains(search)) {
                        continue;
                    }
                    s_list.add(seller2);
                }
            }
            list = s_list;
        }
        if (!list.isEmpty()) {
            final Player[] players = new Player[list.size()];
            list.toArray(players);
            Arrays.sort(players, new PlayersComparator<Object>());
            list.clear();
            list.addAll(Arrays.asList(players));
        }
        return list;
    }

    @Override
    public void onInit() {
        if (Config.COMMUNITYBOARD_ENABLED) {
            RegionCommunity.LOGGER.info("CommunityBoard: Region service loaded.");
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public String[] getBypassCommands() {
        return new String[]{"_bbsloc", "_bbsregion_", "_bbsreglist_", "_bbsregsearch", "_bbsregview_", "_bbsregtarget_"};
    }

    @Override
    public void onBypassCommand(final Player player, final String bypass) {
        final StringTokenizer st = new StringTokenizer(bypass, "_");
        final String cmd = st.nextToken();
        player.setSessionVar("add_fav", null);
        switch (cmd) {
            case "bbsloc": {
                final String tpl = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_regiontpl.htm", player);
                final StringBuilder rl = new StringBuilder("");
                for (int townId = 0; townId < RegionCommunity._towns.length; ++townId) {
                    final TownEntry town = RegionCommunity._towns[townId];
                    String reg = tpl.replace("%region_bypass%", "_bbsregion_" + String.valueOf(townId));
                    reg = reg.replace("%region_name%", StringHolder.getInstance().getNotNull(player, town.getTownNameAddr()));
                    reg = reg.replace("%region_desc%", "&$498;: &$1157;, &$1434;, &$645;.");
                    reg = reg.replace("%region_type%", "l2ui.bbs_folder");
                    int sellers = 0;
                    final int rx = town.getX();
                    final int ry = town.getY();
                    final int offset = 0;
                    for (final Player seller : GameObjectsStorage.getPlayers()) {
                        final int tx = MapUtils.regionX(seller);
                        final int ty = MapUtils.regionY(seller);
                        if (tx >= rx - offset && tx <= rx + offset && ty >= ry - offset && ty <= ry + offset && seller.getPrivateStoreType() > 0 && seller.getPrivateStoreType() != 7) {
                            ++sellers;
                        }
                    }
                    reg = reg.replace("%sellers_count%", String.valueOf(sellers));
                    rl.append(reg);
                }
                final HashMap<Integer, String> tpls = Util.parseTemplate(HtmCache.getInstance().getNotNull("scripts/services/community/bbs_region_list.htm", player));
                String html = tpls.get(0);
                html = html.replace("%REGION_LIST%", rl.toString());
                html = html.replace("<?tree_menu?>", tpls.get(1));
                ShowBoard.separateAndSend(html, player);
                break;
            }
            case "bbsregion": {
                final String tpl = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_regiontpl.htm", player);
                final int townId2 = Integer.parseInt(st.nextToken());
                final StringBuilder rl2 = new StringBuilder("");
                final TownEntry town = RegionCommunity._towns[townId2];
                player.setSessionVar("add_fav", bypass + "&Region " + townId2);
                for (int type = 0; type < RegionCommunity._regionTypes.length; ++type) {
                    String reg2 = tpl.replace("%region_bypass%", "_bbsreglist_" + townId2 + "_" + type + "_1_0_");
                    reg2 = reg2.replace("%region_name%", RegionCommunity._regionTypes[type]);
                    reg2 = reg2.replace("%region_desc%", RegionCommunity._regionTypes[type] + ".");
                    reg2 = reg2.replace("%region_type%", "l2ui.bbs_board");
                    int sellers2 = 0;
                    final int rx2 = town.getX();
                    final int ry2 = town.getY();
                    final int offset2 = 0;
                    for (final Player seller2 : GameObjectsStorage.getPlayers()) {
                        final int tx2 = MapUtils.regionX(seller2);
                        final int ty2 = MapUtils.regionY(seller2);
                        if (tx2 >= rx2 - offset2 && tx2 <= rx2 + offset2 && ty2 >= ry2 - offset2 && ty2 <= ry2 + offset2) {
                            if (type == 0 && (seller2.getPrivateStoreType() == 1 || seller2.getPrivateStoreType() == 8)) {
                                ++sellers2;
                            } else if (type == 1 && seller2.getPrivateStoreType() == 3) {
                                ++sellers2;
                            } else {
                                if (type != 2 || seller2.getPrivateStoreType() != 5) {
                                    continue;
                                }
                                ++sellers2;
                            }
                        }
                    }
                    reg2 = reg2.replace("%sellers_count%", String.valueOf(sellers2));
                    rl2.append(reg2);
                }
                final HashMap<Integer, String> tpls2 = Util.parseTemplate(HtmCache.getInstance().getNotNull("scripts/services/community/bbs_region_list.htm", player));
                String html2 = tpls2.get(0);
                html2 = html2.replace("%REGION_LIST%", rl2.toString());
                html2 = html2.replace("<?tree_menu?>", tpls2.get(2).replace("%TREE%", "&nbsp;>&nbsp;" + StringHolder.getInstance().getNotNull(player, town.getTownNameAddr())));
                ShowBoard.separateAndSend(html2, player);
                break;
            }
            case "bbsreglist": {
                final int townId3 = Integer.parseInt(st.nextToken());
                final int type2 = Integer.parseInt(st.nextToken());
                final int page = Integer.parseInt(st.nextToken());
                final int byItem = Integer.parseInt(st.nextToken());
                final String search = st.hasMoreTokens() ? st.nextToken().toLowerCase() : "";
                final TownEntry town2 = RegionCommunity._towns[townId3];
                player.setSessionVar("add_fav", bypass + "&Region " + townId3 + " " + RegionCommunity._regionTypes[type2]);
                final List<Player> sellers3 = getSellersList(townId3, type2, search, byItem == 1);
                final int start = (page - 1) * 12;
                final int end = Math.min(page * 12, sellers3.size());
                String html3 = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_region_sellers.htm", player);
                if (page == 1) {
                    html3 = html3.replace("%ACTION_GO_LEFT%", "");
                    html3 = html3.replace("%GO_LIST%", "");
                    html3 = html3.replace("%NPAGE%", "1");
                } else {
                    html3 = html3.replace("%ACTION_GO_LEFT%", "bypass _bbsreglist_" + townId3 + "_" + type2 + "_" + (page - 1) + "_" + byItem + "_" + search);
                    html3 = html3.replace("%NPAGE%", String.valueOf(page));
                    final StringBuilder goList = new StringBuilder("");
                    for (int i = (page > 10) ? (page - 10) : 1; i < page; ++i) {
                        goList.append("<td><a action=\"bypass _bbsreglist_").append(townId3).append("_").append(type2).append("_").append(i).append("_").append(byItem).append("_").append(search).append("\"> ").append(i).append(" </a> </td>\n\n");
                    }
                    html3 = html3.replace("%GO_LIST%", goList.toString());
                }
                int pages = Math.max(sellers3.size() / 12, 1);
                if (sellers3.size() > pages * 12) {
                    ++pages;
                }
                if (pages > page) {
                    html3 = html3.replace("%ACTION_GO_RIGHT%", "bypass _bbsreglist_" + townId3 + "_" + type2 + "_" + (page + 1) + "_" + byItem + "_" + search);
                    final int ep = Math.min(page + 10, pages);
                    final StringBuilder goList2 = new StringBuilder("");
                    for (int j = page + 1; j <= ep; ++j) {
                        goList2.append("<td><a action=\"bypass _bbsreglist_").append(townId3).append("_").append(type2).append("_").append(j).append("_").append(byItem).append("_").append(search).append("\"> ").append(j).append(" </a> </td>\n\n");
                    }
                    html3 = html3.replace("%GO_LIST2%", goList2.toString());
                } else {
                    html3 = html3.replace("%ACTION_GO_RIGHT%", "");
                    html3 = html3.replace("%GO_LIST2%", "");
                }
                final StringBuilder seller_list = new StringBuilder("");
                final String tpl2 = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_region_stpl.htm", player);
                for (int j = start; j < end; ++j) {
                    final Player seller3 = sellers3.get(j);
                    final List<TradeItem> tl = seller3.getTradeList();
                    final List<ManufactureItem> cl = seller3.getCreateList();
                    if (tl != null || cl != null) {
                        String stpl = tpl2;
                        stpl = stpl.replace("%view_bypass%", "bypass _bbsregview_" + townId3 + "_" + type2 + "_" + page + "_" + seller3.getObjectId() + "_" + byItem + "_" + search);
                        stpl = stpl.replace("%seller_name%", seller3.getName());
                        String title = "-";
                        if (type2 == 0) {
                            title = ((tl != null && seller3.getSellStoreName() != null && !seller3.getSellStoreName().isEmpty()) ? seller3.getSellStoreName() : "-");
                        } else if (type2 == 1) {
                            title = ((tl != null && seller3.getBuyStoreName() != null && !seller3.getBuyStoreName().isEmpty()) ? seller3.getBuyStoreName() : "-");
                        } else if (type2 == 2 && seller3.getPrivateStoreType() == 5) {
                            title = ((cl != null && seller3.getManufactureName() != null && !seller3.getManufactureName().isEmpty()) ? seller3.getManufactureName() : "-");
                        }
                        title = title.replace("<", "");
                        title = title.replace(">", "");
                        title = title.replace("&", "");
                        title = title.replace("$", "");
                        if (title.isEmpty()) {
                            title = "-";
                        }
                        stpl = stpl.replace("%seller_title%", title);
                        seller_list.append(stpl);
                    }
                }
                html3 = html3.replace("%SELLER_LIST%", seller_list.toString());
                html3 = html3.replace("%search_bypass%", "_bbsregsearch_" + townId3 + "_" + type2);
                html3 = html3.replace("%TREE%", "&nbsp;>&nbsp;<a action=\"bypass _bbsregion_" + townId3 + "\">" + StringHolder.getInstance().getNotNull(player, town2.getTownNameAddr()) + "</a>&nbsp;>&nbsp;" + RegionCommunity._regionTypes[type2]);
                ShowBoard.separateAndSend(html3, player);
                break;
            }
            case "bbsregview": {
                final int townId3 = Integer.parseInt(st.nextToken());
                final int type2 = Integer.parseInt(st.nextToken());
                final int page = Integer.parseInt(st.nextToken());
                final int objectId = Integer.parseInt(st.nextToken());
                final int byItem2 = Integer.parseInt(st.nextToken());
                final String search2 = st.hasMoreTokens() ? st.nextToken().toLowerCase() : "";
                final TownEntry town3 = RegionCommunity._towns[townId3];
                final Player seller4 = World.getPlayer(objectId);
                if (seller4 == null || seller4.getPrivateStoreType() == 0) {
                    onBypassCommand(player, "_bbsreglist_" + townId3 + "_" + type2 + "_" + page + "_" + byItem2 + "_" + search2);
                    return;
                }
                String title2 = "-";
                final String tpl3 = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_region_storetpl.htm", player);
                final StringBuilder sb = new StringBuilder("");
                if (type2 < 2) {
                    final List<TradeItem> sl = (type2 == 0) ? seller4.getSellList() : seller4.getBuyList();
                    final List<TradeItem> tl2 = seller4.getTradeList();
                    if (sl == null || sl.isEmpty() || tl2 == null) {
                        onBypassCommand(player, "_bbsreglist_" + townId3 + "_" + type2 + "_" + page + "_" + byItem2 + "_" + search2);
                        return;
                    }
                    if (type2 == 0 && seller4.getSellStoreName() != null && !seller4.getSellStoreName().isEmpty()) {
                        title2 = seller4.getSellStoreName();
                    } else if (type2 == 1 && seller4.getBuyStoreName() != null && !seller4.getBuyStoreName().isEmpty()) {
                        title2 = seller4.getBuyStoreName();
                    }
                    for (final TradeItem ti : sl) {
                        final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(ti.getItemId());
                        if (item != null) {
                            String stpl2 = tpl3.replace("%item_name%", item.getName() + ((item.isEquipment() && ti.getEnchantLevel() > 0) ? (" +" + ti.getEnchantLevel()) : ""));
                            stpl2 = stpl2.replace("%item_img%", item.getIcon());
                            stpl2 = stpl2.replace("%item_count%", String.valueOf(ti.getCount()));
                            stpl2 = stpl2.replace("%item_price%", String.format("%,3d", ti.getOwnersPrice()).replace(" ", ","));
                            String desc = "";
                            if (item.getCrystalType() != ItemGrade.NONE) {
                                desc = RegionCommunity._grade[item.getCrystalType().ordinal() - 1];
                                desc += ((item.getCrystalCount() > 0) ? ((player.isLangRus() ? " \u041a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u043e\u0432: " : " Crystals: ") + item.getCrystalCount() + ";&nbsp;") : ";&nbsp;");
                            }
                            if (item.isEquipment()) {
                                if (ti.getAttackElement() >= 0 && ti.getAttackElementValue() > 0) {
                                    desc = desc + "&$1620;: " + RegionCommunity._elements[ti.getAttackElement()] + " +" + ti.getAttackElementValue();
                                } else if (ti.getDefenceFire() > 0 || ti.getDefenceWater() > 0 || ti.getDefenceWind() > 0 || ti.getDefenceEarth() > 0 || ti.getDefenceHoly() > 0 || ti.getDefenceUnholy() > 0) {
                                    desc += "&$1651;:";
                                    if (ti.getDefenceFire() > 0) {
                                        desc = desc + " &$1622; +" + ti.getDefenceFire() + ";&nbsp;";
                                    }
                                    if (ti.getDefenceWater() > 0) {
                                        desc = desc + " &$1623; +" + ti.getDefenceWater() + ";&nbsp;";
                                    }
                                    if (ti.getDefenceWind() > 0) {
                                        desc = desc + " &$1624; +" + ti.getDefenceWind() + ";&nbsp;";
                                    }
                                    if (ti.getDefenceEarth() > 0) {
                                        desc = desc + " &$1625; +" + ti.getDefenceEarth() + ";&nbsp;";
                                    }
                                    if (ti.getDefenceHoly() > 0) {
                                        desc = desc + " &$1626; +" + ti.getDefenceHoly() + ";&nbsp;";
                                    }
                                    if (ti.getDefenceUnholy() > 0) {
                                        desc = desc + " &$1627; +" + ti.getDefenceUnholy() + ";&nbsp;";
                                    }
                                }
                            }
                            if (item.isStackable()) {
                                desc += (player.isLangRus() ? "\u0421\u0442\u044b\u043a\u0443\u0435\u043c\u044b\u0439;&nbsp;" : "Stackable;&nbsp;");
                            }
                            if (item.isSealedItem()) {
                                desc += (player.isLangRus() ? "\u0417\u0430\u043f\u0435\u0447\u0430\u0442\u0430\u043d\u043d\u044b\u0439;&nbsp;" : "Sealed;&nbsp;");
                            }
                            if (item.isShadowItem()) {
                                desc += (player.isLangRus() ? "\u0422\u0435\u043d\u0435\u0432\u043e\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442;&nbsp;" : "Shadow item;&nbsp;");
                            }
                            if (item.isTemporal()) {
                                desc += (player.isLangRus() ? "\u0412\u0440\u0435\u043c\u0435\u043d\u043d\u044b\u0439;&nbsp;" : "Temporal;&nbsp;");
                            }
                            stpl2 = stpl2.replace("%item_desc%", desc);
                            sb.append(stpl2);
                        }
                    }
                } else {
                    final List<ManufactureItem> cl2 = seller4.getCreateList();
                    if (cl2 == null) {
                        onBypassCommand(player, "_bbsreglist_" + townId3 + "_" + type2 + "_" + page + "_" + byItem2 + "_" + search2);
                        return;
                    }
                    if ((title2 = seller4.getManufactureName()) == null) {
                        title2 = "-";
                    }
                    for (final ManufactureItem mi : cl2) {
                        final Recipe rec = RecipeHolder.getInstance().getRecipeById(mi.getRecipeId() - 1);
                        if (rec != null) {
                            if (rec.getProducts().isEmpty()) {
                                continue;
                            }
                            final ItemTemplate item = rec.getProducts().get(0).getKey();
                            if (item == null) {
                                continue;
                            }
                            String stpl2 = tpl3.replace("%item_name%", item.getName());
                            stpl2 = stpl2.replace("%item_img%", item.getIcon());
                            stpl2 = stpl2.replace("%item_count%", "N/A");
                            stpl2 = stpl2.replace("%item_price%", String.format("%,3d", mi.getCost()).replace(" ", ","));
                            String desc = "";
                            if (item.getCrystalType() != ItemGrade.NONE) {
                                desc = RegionCommunity._grade[item.getCrystalType().ordinal() - 1] + ((item.getCrystalCount() > 0) ? ((player.isLangRus() ? " \u041a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u043e\u0432: " : " Crystals: ") + item.getCrystalCount() + ";&nbsp;") : ";&nbsp;");
                            }
                            if (item.isStackable()) {
                                desc = (player.isLangRus() ? "\u0421\u0442\u044b\u043a\u0443\u0435\u043c\u044b\u0439;&nbsp;" : "Stackable;&nbsp;");
                            }
                            if (item.isSealedItem()) {
                                desc += (player.isLangRus() ? "\u0417\u0430\u043f\u0435\u0447\u0430\u0442\u0430\u043d\u043d\u044b\u0439;&nbsp;" : "Sealed;&nbsp;");
                            }
                            stpl2 = stpl2.replace("%item_desc%", desc);
                            sb.append(stpl2);
                        }
                    }
                }
                String html4 = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_region_view.htm", player);
                html4 = html4.replace("%sell_type%", RegionCommunity._regionTypes[type2]);
                title2 = title2.replace("<", "");
                title2 = title2.replace(">", "");
                title2 = title2.replace("&", "");
                title2 = title2.replace("$", "");
                if (title2.isEmpty()) {
                    title2 = "-";
                }
                html4 = html4.replace("%title%", title2);
                html4 = html4.replace("%char_name%", seller4.getName());
                html4 = html4.replace("%object_id%", String.valueOf(seller4.getObjectId()));
                html4 = html4.replace("%STORE_LIST%", sb.toString());
                html4 = html4.replace("%list_bypass%", "_bbsreglist_" + townId3 + "_" + type2 + "_" + page + "_" + byItem2 + "_" + search2);
                html4 = html4.replace("%TREE%", "&nbsp;>&nbsp;<a action=\"bypass _bbsregion_" + townId3 + "\">" + StringHolder.getInstance().getNotNull(player, town3.getTownNameAddr()) + "</a>&nbsp;>&nbsp;<a action=\"bypass _bbsreglist_" + townId3 + "_" + type2 + "_" + page + "_" + byItem2 + "_\">" + RegionCommunity._regionTypes[type2] + "</a>&nbsp;>&nbsp;" + seller4.getName());
                ShowBoard.separateAndSend(html4, player);
                break;
            }
            case "bbsregtarget":
                final int objectId2 = Integer.parseInt(st.nextToken());
                final Player seller5 = World.getPlayer(objectId2);
                if (seller5 != null) {
                    player.sendPacket(new RadarControl(0, 2, seller5.getLoc()));
                    if (player.knowsObject(seller5)) {
                        player.setObjectTarget(seller5);
                        seller5.broadcastRelationChanged();
                    }
                } else {
                    player.sendActionFailed();
                }
                break;
        }
    }

    @Override
    public void onWriteCommand(final Player player, final String bypass, final String arg1, final String arg2, String arg3, final String arg4, final String arg5) {
        final StringTokenizer st = new StringTokenizer(bypass, "_");
        final String cmd = st.nextToken();
        if ("bbsregsearch".equals(cmd)) {
            final int townId = Integer.parseInt(st.nextToken());
            final int type = Integer.parseInt(st.nextToken());
            final String byItem = "Item".equals(arg4) ? "1" : "0";
            if (arg3 == null) {
                arg3 = "";
            }
            arg3 = arg3.replace("<", "");
            arg3 = arg3.replace(">", "");
            arg3 = arg3.replace("&", "");
            arg3 = arg3.replace("$", "");
            if (arg3.length() > 30) {
                arg3 = arg3.substring(0, 30);
            }
            onBypassCommand(player, "_bbsreglist_" + townId + "_" + type + "_1_" + byItem + "_" + arg3);
        }
    }

    private static class TownEntry {
        private final String _townNameAddr;
        private final int _gx;
        private final int _gy;

        private TownEntry(final String townNameAddr, final int gx, final int gy) {
            _townNameAddr = townNameAddr;
            _gx = gx;
            _gy = gy;
        }

        public String getTownNameAddr() {
            return _townNameAddr;
        }

        public int getX() {
            return _gx;
        }

        public int getY() {
            return _gy;
        }
    }

    private static class PlayersComparator<T> implements Comparator<T> {
        @Override
        public int compare(final Object o1, final Object o2) {
            if (o1 instanceof Player && o2 instanceof Player) {
                final Player p1 = (Player) o1;
                final Player p2 = (Player) o2;
                return p1.getName().compareTo(p2.getName());
            }
            return 0;
        }
    }
}
