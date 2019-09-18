package services.petevolve;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.handler.npcdialog.INpcDialogAppender;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Summon;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.PetInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.InventoryUpdate;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.Util;

import java.util.Arrays;
import java.util.List;

public class exchange extends Functions implements INpcDialogAppender {
    private static final int PEticketB = 7583;
    private static final int PEticketC = 7584;
    private static final int PEticketK = 7585;
    private static final int BbuffaloP = 6648;
    private static final int BcougarC = 6649;
    private static final int BkookaburraO = 6650;

    public void exch_1() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (getItemCount(player, 7583) >= 1L) {
            removeItem(player, 7583, 1L);
            addItem(player, 6648, 1L);
            return;
        }
        show("scripts/services/petevolve/exchange_no.htm", player);
    }

    public void exch_2() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (getItemCount(player, 7584) >= 1L) {
            removeItem(player, 7584, 1L);
            addItem(player, 6649, 1L);
            return;
        }
        show("scripts/services/petevolve/exchange_no.htm", player);
    }

    public void exch_3() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (getItemCount(player, 7585) >= 1L) {
            removeItem(player, 7585, 1L);
            addItem(player, 6650, 1L);
            return;
        }
        show("scripts/services/petevolve/exchange_no.htm", player);
    }

    public void showErasePetName() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_CHANGE_PET_NAME_ENABLED) {
            show("\u0421\u0435\u0440\u0432\u0438\u0441 \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d.", player);
            return;
        }
        final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_PET_NAME_ITEM);
        String out = "";
        out += "<html><body>\u0412\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u043e\u0431\u043d\u0443\u043b\u0438\u0442\u044c \u0438\u043c\u044f \u0443 \u043f\u0435\u0442\u0430, \u0434\u043b\u044f \u0442\u043e\u0433\u043e \u0447\u0442\u043e\u0431\u044b \u043d\u0430\u0437\u043d\u0430\u0447\u0438\u0442\u044c \u043d\u043e\u0432\u043e\u0435. \u041f\u0435\u0442 \u043f\u0440\u0438 \u044d\u0442\u043e\u043c \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0432\u044b\u0437\u0432\u0430\u043d.";
        out = out + "<br>\u0421\u0442\u043e\u0438\u043c\u043e\u0441\u0442\u044c \u043e\u0431\u043d\u0443\u043b\u0435\u043d\u0438\u044f: " + Util.formatAdena((long) Config.SERVICES_CHANGE_PET_NAME_PRICE) + " " + item.getName();
        out += "<br><button width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:erasePetName\" value=\"\u041e\u0431\u043d\u0443\u043b\u0438\u0442\u044c \u0438\u043c\u044f\">";
        out += "</body></html>";
        show(out, player);
    }

    public void erasePetName() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_CHANGE_PET_NAME_ENABLED) {
            show("\u0421\u0435\u0440\u0432\u0438\u0441 \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d.", player);
            return;
        }
        final Summon pl_pet = player.getPet();
        if (pl_pet == null || !pl_pet.isPet()) {
            show("\u041f\u0438\u0442\u043e\u043c\u0435\u0446 \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0432\u044b\u0437\u0432\u0430\u043d.", player);
            return;
        }
        if (player.getInventory().destroyItemByItemId(Config.SERVICES_CHANGE_PET_NAME_ITEM, (long) Config.SERVICES_CHANGE_PET_NAME_PRICE)) {
            pl_pet.setName(pl_pet.getTemplate().name);
            pl_pet.broadcastCharInfo();
            final PetInstance _pet = (PetInstance) pl_pet;
            final ItemInstance control = _pet.getControlItem();
            if (control != null) {
                control.setDamaged(1);
                player.sendPacket(new InventoryUpdate().addModifiedItem(control));
            }
            show("\u0418\u043c\u044f \u0441\u0442\u0435\u0440\u0442\u043e.", player);
        } else if (Config.SERVICES_CHANGE_PET_NAME_ITEM == 57) {
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        } else {
            player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
        }
    }


    @Override
    public String getAppend(Player player, NpcInstance npc, int val) {
        if (Config.SERVICES_CHANGE_PET_NAME_ENABLED) {
            return player.isLangRus() ? "<br>[scripts_services.petevolve.exchange:showErasePetName|\u041e\u0431\u043d\u0443\u043b\u0438\u0442\u044c \u0438\u043c\u044f \u0443 \u043f\u0435\u0442\u0430]" : "<br>[scripts_services.petevolve.exchange:showErasePetName|Erase Pet Name]";
        }
        return "";
    }

    @Override
    public List<Integer> getNpcIds() {
        return Arrays.asList(30731, 30827, 30828, 30829, 30830, 30831, 30869, 31067, 31265, 31309, 31954);
    }
}
