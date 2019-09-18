package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Summon;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.StaticObjectInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;

public abstract class SysMsgContainer<T extends SysMsgContainer<T>> extends L2GameServerPacket {
    protected SystemMsg _message;
    protected List<IArgument> _arguments;

    protected SysMsgContainer(final int messageId) {
        this(SystemMsg.valueOf(messageId));
    }

    protected SysMsgContainer(final SystemMsg message) {
        if (message == null) {
            throw new IllegalArgumentException("SystemMsg is null");
        }
        _message = message;
        _arguments = new ArrayList<>(_message.size());
    }

    protected void writeElements() {
        if (_message.size() != _arguments.size()) {
            throw new IllegalArgumentException("Wrong count of arguments: " + _message);
        }
        writeD(_message.getId());
        writeD(_arguments.size());
        for (final IArgument argument : _arguments) {
            argument.write(this);
        }
    }

    public T addName(final GameObject object) {
        if (object == null) {
            return add(new StringArgument(null));
        }
        if (object.isNpc()) {
            return add(new NpcNameArgument(((NpcInstance) object).getNpcId() + 1000000));
        }
        if (object.isSummon()) {
            return add(new NpcNameArgument(((Summon) object).getNpcId() + 1000000));
        }
        if (object.isItem()) {
            return add(new ItemNameArgument(((ItemInstance) object).getItemId()));
        }
        if (object.isPlayer()) {
            return add(new PlayerNameArgument((Creature) object));
        }
        if (object.isDoor()) {
            return add(new StaticObjectNameArgument(((DoorInstance) object).getDoorId()));
        }
        if (object instanceof StaticObjectInstance) {
            return add(new StaticObjectNameArgument(((StaticObjectInstance) object).getUId()));
        }
        return add(new StringArgument(object.getName()));
    }

    public T addInstanceName(final int id) {
        return add(new InstanceNameArgument(id));
    }

    public T addSysString(final int id) {
        return add(new SysStringArgument(id));
    }

    public T addSkillName(final Skill skill) {
        return addSkillName(skill.getDisplayId(), skill.getDisplayLevel());
    }

    public T addSkillName(final int id, final int level) {
        return add(new SkillArgument(id, level));
    }

    public T addItemName(final int item_id) {
        return add(new ItemNameArgument(item_id));
    }

    @Deprecated
    public T addItemNameWithAugmentation(final ItemInstance item) {
        return add(new ItemNameWithAugmentationArgument(item.getItemId(), item.getVariationStat1(), item.getVariationStat2()));
    }

    public T addZoneName(final Creature c) {
        return addZoneName(c.getX(), c.getY(), c.getZ());
    }

    public T addZoneName(final Location loc) {
        return add(new ZoneArgument(loc.x, loc.y, loc.z));
    }

    public T addZoneName(final int x, final int y, final int z) {
        return add(new ZoneArgument(x, y, z));
    }

    public T addResidenceName(final Residence r) {
        return add(new ResidenceArgument(r.getId()));
    }

    public T addResidenceName(final int i) {
        return add(new ResidenceArgument(i));
    }

    public T addElementName(final int i) {
        return add(new ElementNameArgument(i));
    }

    public T addElementName(final Element i) {
        return add(new ElementNameArgument(i.getId()));
    }

    public T addInteger(final double i) {
        return add(new IntegerArgument((int) i));
    }

    public T addLong(final long i) {
        return add(new LongArgument(i));
    }

    public T addString(final String t) {
        return add(new StringArgument(t));
    }

    @SuppressWarnings("unchecked")
    protected T add(final IArgument arg) {
        _arguments.add(arg);
        return (T) this;
    }

    public enum Types {
        TEXT,
        NUMBER,
        NPC_NAME,
        ITEM_NAME,
        SKILL_NAME,
        RESIDENCE_NAME,
        LONG,
        ZONE_NAME,
        ITEM_NAME_WITH_AUGMENTATION,
        ELEMENT_NAME,
        INSTANCE_NAME,
        STATIC_OBJECT_NAME,
        PLAYER_NAME,
        SYSTEM_STRING
    }

    public abstract static class IArgument {
        void write(final SysMsgContainer m) {
            m.writeD(getType().ordinal());
            writeData(m);
        }

        abstract Types getType();

        abstract void writeData(final SysMsgContainer p0);
    }

    public static class IntegerArgument extends IArgument {
        private final int _data;

        public IntegerArgument(final int da) {
            _data = da;
        }

        @Override
        public void writeData(final SysMsgContainer message) {
            message.writeD(_data);
        }

        @Override
        Types getType() {
            return Types.NUMBER;
        }
    }

    public static class NpcNameArgument extends IntegerArgument {
        public NpcNameArgument(final int da) {
            super(da);
        }

        @Override
        Types getType() {
            return Types.NPC_NAME;
        }
    }

    public static class ItemNameArgument extends IntegerArgument {
        public ItemNameArgument(final int da) {
            super(da);
        }

        @Override
        Types getType() {
            return Types.ITEM_NAME;
        }
    }

    public static class ItemNameWithAugmentationArgument extends IArgument {
        private final int _itemId;
        private final int _variation1;
        private final int _variation2;

        public ItemNameWithAugmentationArgument(final int itemId, final int variation1, final int variation2) {
            _itemId = itemId;
            _variation1 = variation1;
            _variation2 = variation2;
        }

        @Override
        Types getType() {
            return Types.ITEM_NAME_WITH_AUGMENTATION;
        }

        @Override
        void writeData(final SysMsgContainer message) {
            message.writeD(_itemId);
            message.writeH(_variation1);
            message.writeH(_variation2);
        }
    }

    public static class InstanceNameArgument extends IntegerArgument {
        public InstanceNameArgument(final int da) {
            super(da);
        }

        @Override
        Types getType() {
            return Types.INSTANCE_NAME;
        }
    }

    public static class SysStringArgument extends IntegerArgument {
        public SysStringArgument(final int da) {
            super(da);
        }

        @Override
        Types getType() {
            return Types.SYSTEM_STRING;
        }
    }

    public static class ResidenceArgument extends IntegerArgument {
        public ResidenceArgument(final int da) {
            super(da);
        }

        @Override
        Types getType() {
            return Types.RESIDENCE_NAME;
        }
    }

    public static class StaticObjectNameArgument extends IntegerArgument {
        public StaticObjectNameArgument(final int da) {
            super(da);
        }

        @Override
        Types getType() {
            return Types.STATIC_OBJECT_NAME;
        }
    }

    public static class LongArgument extends IArgument {
        private final long _data;

        public LongArgument(final long da) {
            _data = da;
        }

        @Override
        void writeData(final SysMsgContainer message) {
            message.writeQ(_data);
        }

        @Override
        Types getType() {
            return Types.LONG;
        }
    }

    public static class StringArgument extends IArgument {
        private final String _data;

        public StringArgument(final String da) {
            _data = ((da == null) ? "null" : da);
        }

        @Override
        void writeData(final SysMsgContainer message) {
            message.writeS(_data);
        }

        @Override
        Types getType() {
            return Types.TEXT;
        }
    }

    public static class SkillArgument extends IArgument {
        private final int _skillId;
        private final int _skillLevel;

        public SkillArgument(final int t1, final int t2) {
            _skillId = t1;
            _skillLevel = t2;
        }

        @Override
        void writeData(final SysMsgContainer message) {
            message.writeD(_skillId);
            message.writeD(_skillLevel);
        }

        @Override
        Types getType() {
            return Types.SKILL_NAME;
        }
    }

    public static class ZoneArgument extends IArgument {
        private final int _x;
        private final int _y;
        private final int _z;

        public ZoneArgument(final int t1, final int t2, final int t3) {
            _x = t1;
            _y = t2;
            _z = t3;
        }

        @Override
        void writeData(final SysMsgContainer message) {
            message.writeD(_x);
            message.writeD(_y);
            message.writeD(_z);
        }

        @Override
        Types getType() {
            return Types.ZONE_NAME;
        }
    }

    public static class ElementNameArgument extends IntegerArgument {
        public ElementNameArgument(final int type) {
            super(type);
        }

        @Override
        Types getType() {
            return Types.ELEMENT_NAME;
        }
    }

    public static class PlayerNameArgument extends StringArgument {
        public PlayerNameArgument(final Creature creature) {
            super(creature.getName());
        }

        @Override
        Types getType() {
            return Types.TEXT;
        }
    }
}
