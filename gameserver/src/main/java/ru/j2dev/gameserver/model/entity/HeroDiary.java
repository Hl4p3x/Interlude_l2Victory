package ru.j2dev.gameserver.model.entity;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.utils.HtmlUtils;

import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public class HeroDiary {
    public static final int ACTION_RAID_KILLED = 1;
    public static final int ACTION_HERO_GAINED = 2;
    public static final int ACTION_CASTLE_TAKEN = 3;
    private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:** dd.MM.yyyy");

    private final int _id;
    private final long _time;
    private final int _param;

    public HeroDiary(final int id, final long time, final int param) {
        _id = id;
        _time = time;
        _param = param;
    }

    public Entry<String, String> toString(final Player player) {
        CustomMessage message;
        switch (_id) {
            case ACTION_RAID_KILLED: {
                message = new CustomMessage("l2p.gameserver.model.entity.Hero.RaidBossKilled", player).addString(HtmlUtils.htmlNpcName(_param));
                break;
            }
            case ACTION_HERO_GAINED: {
                message = new CustomMessage("l2p.gameserver.model.entity.Hero.HeroGained", player);
                break;
            }
            case ACTION_CASTLE_TAKEN: {
                message = new CustomMessage("l2p.gameserver.model.entity.Hero.CastleTaken", player).addString(HtmlUtils.htmlResidenceName(_param));
                break;
            }
            default: {
                return null;
            }
        }
        return new SimpleEntry<>(SIMPLE_FORMAT.format(_time), message.toString());
    }
}
