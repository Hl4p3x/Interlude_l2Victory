package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.instances.player.ShortCut;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.skills.TimeStamp;

public abstract class ShortCutPacket extends L2GameServerPacket {
    public static ShortcutInfo convert(final Player player, final ShortCut shortCut) {
        ShortcutInfo shortcutInfo;
        final int page = shortCut.getSlot() + shortCut.getPage() * 12;
        switch (shortCut.getType()) {
            case 1: {
                int reuseGroup = -1;
                int currentReuse = 0;
                int reuse = 0;
                int variation1 = 0;
                int variation2 = 0;
                final ItemInstance item = player.getInventory().getItemByObjectId(shortCut.getId());
                if (item != null) {
                    variation1 = item.getVariationStat1();
                    variation2 = item.getVariationStat2();
                    reuseGroup = item.getTemplate().getDisplayReuseGroup();
                    if (item.getTemplate().getReuseDelay() > 0) {
                        final TimeStamp timeStamp = player.getSharedGroupReuse(item.getTemplate().getReuseGroup());
                        if (timeStamp != null) {
                            currentReuse = (int) (timeStamp.getReuseCurrent() / 1000L);
                            reuse = (int) (timeStamp.getReuseBasic() / 1000L);
                        }
                    }
                }
                shortcutInfo = new ItemShortcutInfo(shortCut.getType(), page, shortCut.getId(), reuseGroup, currentReuse, reuse, variation1, variation2, shortCut.getCharacterType());
                break;
            }
            case 2: {
                shortcutInfo = new SkillShortcutInfo(shortCut.getType(), page, shortCut.getId(), shortCut.getLevel(), shortCut.getCharacterType());
                break;
            }
            default: {
                shortcutInfo = new ShortcutInfo(shortCut.getType(), page, shortCut.getId(), shortCut.getCharacterType());
                break;
            }
        }
        return shortcutInfo;
    }

    protected static class ItemShortcutInfo extends ShortcutInfo {
        private final int _reuseGroup;
        private final int _currentReuse;
        private final int _basicReuse;
        private final int _varia1;
        private final int _varia2;

        public ItemShortcutInfo(final int type, final int page, final int id, final int reuseGroup, final int currentReuse, final int basicReuse, final int variation1, final int variation2, final int characterType) {
            super(type, page, id, characterType);
            _reuseGroup = reuseGroup;
            _currentReuse = currentReuse;
            _basicReuse = basicReuse;
            _varia1 = variation1;
            _varia2 = variation2;
        }

        @Override
        protected void write0(final ShortCutPacket p) {
            p.writeD(_id);
            p.writeD(_characterType);
            p.writeD(_reuseGroup);
            p.writeD(_currentReuse);
            p.writeD(_basicReuse);
            p.writeH(_varia1);
            p.writeH(_varia2);
        }
    }

    protected static class SkillShortcutInfo extends ShortcutInfo {
        private final int _level;

        public SkillShortcutInfo(final int type, final int page, final int id, final int level, final int characterType) {
            super(type, page, id, characterType);
            _level = level;
        }

        public int getLevel() {
            return _level;
        }

        @Override
        protected void write0(final ShortCutPacket p) {
            p.writeD(_id);
            p.writeD(_level);
            p.writeC(0);
            p.writeD(_characterType);
        }
    }

    protected static class ShortcutInfo {
        protected final int _type;
        protected final int _page;
        protected final int _id;
        protected final int _characterType;

        public ShortcutInfo(final int type, final int page, final int id, final int characterType) {
            _type = type;
            _page = page;
            _id = id;
            _characterType = characterType;
        }

        protected void write(final ShortCutPacket p) {
            p.writeD(_type);
            p.writeD(_page);
            write0(p);
        }

        protected void write0(final ShortCutPacket p) {
            p.writeD(_id);
            p.writeD(_characterType);
        }
    }
}
