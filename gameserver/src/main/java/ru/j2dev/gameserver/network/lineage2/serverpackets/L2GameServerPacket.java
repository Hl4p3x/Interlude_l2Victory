package ru.j2dev.gameserver.network.lineage2.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.net.nio.impl.SendablePacket;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.GameServer;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.base.MultiSellIngredient;
import ru.j2dev.gameserver.model.items.ItemInfo;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.components.OutgoingPackets;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class L2GameServerPacket extends SendablePacket<GameClient> implements IStaticPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(L2GameServerPacket.class);

    @Override
    public final boolean write() {
        try {
            writeImpl();
            if (Config.LOG_SERVER_PACKETS) {
                Log.serverPacket(getType() + " to Client : " + getClient().toString());
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Client: " + getClient() + " - Failed writing: " + getType() + " - Server Version: " + GameServer.getInstance().getVersion().getRevisionNumber(), e);
        }
        return false;
    }

    protected abstract void writeImpl();

    private void writeOpcode() {
        try {
            final int id1 = OutgoingPackets.valueOf(getClass().getSimpleName()).getFirstId();
            final int id2 = OutgoingPackets.valueOf(getClass().getSimpleName()).getSecondId();
            writeC(id1);
            if (id2 > 0) {
                writeH(id2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getClass().getName(), e.getLocalizedMessage());
        }

    }

    protected void writeEx(final int value) {
        writeC(0xfe);
        writeH(value);
    }

    protected void writeD(final boolean b) {
        writeD(b ? 1 : 0);
    }

    protected void writeDD(final int[] values, final boolean sendCount) {
        if (sendCount) {
            getByteBuffer().putInt(values.length);
        }
        Arrays.stream(values).forEach(value -> getByteBuffer().putInt(value));
    }

    protected void writeDD(final int[] values) {
        writeDD(values, false);
    }

    protected void writeItemInfo(final ItemInstance item) {
        writeItemInfo(item, item.getCount());
    }

    protected void writeItemInfo(final ItemInstance item, final long count) {
        writeH(item.getTemplate().getType1());
        writeD(item.getObjectId());
        writeD(item.getItemId());
        writeD((int) count);
        writeH(item.getTemplate().getType2ForPackets());
        writeH(item.getBlessed());
        writeH(item.isEquipped() ? 1 : 0);
        writeD(item.getTemplate().getBodyPart());
        writeH(item.getEnchantLevel());
        writeH(item.getDamaged());
        writeH(item.getVariationStat1());
        writeH(item.getVariationStat2());
        writeD(item.getDuration());
    }

    protected void writeWatehouseItemInfo(List<ItemInfo> infoList) {
        writeH(infoList.size());
        infoList.forEach(item -> {
            writeH(item.getItem().getType1());
            writeD(item.getObjectId());
            writeD(item.getItemId());
            writeD((int) item.getCount());
            writeH(item.getItem().getType2ForPackets());
            writeH(item.getCustomType1());
            writeD(item.getItem().getBodyPart());
            writeH(item.getEnchantLevel());
            writeH(item.getCustomType2());
            writeH(0);
            writeD(item.getObjectId());
            writeD(item.getVariationStat1());
            writeD(item.getVariationStat2());
        });
    }

    protected void writeItemInfo(final ItemInfo item) {
        writeItemInfo(item, item.getCount());
    }

    protected void writeItemInfo(final ItemInfo item, final long count) {
        writeH(item.getItem().getType1());
        writeD(item.getObjectId());
        writeD(item.getItemId());
        writeD((int) count);
        writeH(item.getItem().getType2ForPackets());
        writeH(item.getCustomType1());
        writeH(item.isEquipped() ? 1 : 0);
        writeD(item.getItem().getBodyPart());
        writeH(item.getEnchantLevel());
        writeH(item.getCustomType2());
        writeH(item.getVariationStat1());
        writeH(item.getVariationStat2());
        writeD(item.getShadowLifeTime());
    }

    protected void writeItemElements(MultiSellIngredient item) {
        if (item.getItemId() <= 0) {
            writeItemElements();
            return;
        }
        Optional<ItemTemplate> i = Optional.ofNullable(ItemTemplateHolder.getInstance().getTemplate(item.getItemId()));
        i.ifPresent(itemTemplate -> {
            if (item.getItemAttributes().getValue() > 0) {
                if (itemTemplate.isWeapon()) {
                    Element e = item.getItemAttributes().getElement();
                    writeH(e.getId()); // attack element (-1 - none)
                    writeH(item.getItemAttributes().getValue(e) + itemTemplate.getBaseAttributeValue(e)); // attack element value
                    writeH(0); // водная стихия (fire pdef)
                    writeH(0); // огненная стихия (water pdef)
                    writeH(0); // земляная стихия (wind pdef)
                    writeH(0); // воздушная стихия (earth pdef)
                    writeH(0); // темная стихия (holy pdef)
                    writeH(0); // светлая стихия (dark pdef)
                } else if (itemTemplate.isArmor()) {
                    writeH(-1); // attack element (-1 - none)
                    writeH(0); // attack element value
                    Arrays.stream(Element.VALUES).mapToInt(e -> item.getItemAttributes().getValue(e) + itemTemplate.getBaseAttributeValue(e)).forEach(this::writeH);
                } else {
                    writeItemElements();
                }
            } else {
                writeItemElements();
            }
        });
    }

    protected void writeItemElements() {
        writeH(-1);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
    }

    public String getType() {
        return "[S] " + getClass().getSimpleName();
    }

    @Override
    public L2GameServerPacket packet(final Player player) {
        return this;
    }
}
