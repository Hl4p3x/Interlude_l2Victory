package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;

import java.util.List;

public class RequestGMCommand extends L2GameClientPacket {
    private String _targetName;
    private int _command;

    @Override
    protected void readImpl() {
        _targetName = readS();
        _command = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        final Player target = World.getPlayer(_targetName);
        if (player == null || target == null) {
            return;
        }
        if (!player.getPlayerAccess().CanViewChar) {
            return;
        }
        switch (_command) {
            case 1: {
                player.sendPacket(new GMViewCharacterInfo(target));
                player.sendPacket(new GMHennaInfo(target));
                break;
            }
            case 2: {
                if (target.getClan() != null) {
                    player.sendPacket(new GMViewPledgeInfo(target));
                    break;
                }
                break;
            }
            case 3: {
                player.sendPacket(new GMViewSkillInfo(target));
                break;
            }
            case 4: {
                player.sendPacket(new GMViewQuestInfo(target));
                break;
            }
            case 5: {
                final List<ItemInstance> items = target.getInventory().getItems();
                final int questSize = 0;
                player.sendPacket(new GMViewItemList(target, items, items.size() - questSize));
                break;
            }
            case 6: {
                player.sendPacket(new GMViewWarehouseWithdrawList(target));
                break;
            }
        }
    }
}
