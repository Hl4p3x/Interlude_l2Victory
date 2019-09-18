package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.dao.CharacterDAO;
import ru.j2dev.gameserver.data.xml.holder.AltStartHolder;
import ru.j2dev.gameserver.data.xml.holder.SkillAcquireHolder;
import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.instances.player.ShortCut;
import ru.j2dev.gameserver.model.base.AcquireType;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CharacterCreateFail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CharacterCreateSuccess;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CharacterSelectionInfo;
import ru.j2dev.gameserver.tables.CharTemplateTable;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.PlayerTemplate;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Util;

import java.util.List;

public class RequestCharacterCreate extends L2GameClientPacket {
    private String _name;
    private int _sex;
    private int _classId;
    private int _hairStyle;
    private int _hairColor;
    private int _face;

    public static void startInitialQuests(final Player player) {
        for (int ALT_INITIAL_QUEST : Config.ALT_INITIAL_QUESTS) {
            Quest q = QuestManager.getQuest(ALT_INITIAL_QUEST);
            if (q != null) {
                q.newQuestState(player, 1);
            }
        }
    }

    @Override
    protected void readImpl() {
        _name = readS();
        readD();
        _sex = readD();
        _classId = readD();
        readD();
        readD();
        readD();
        readD();
        readD();
        readD();
        _hairStyle = readD();
        _hairColor = readD();
        _face = readD();
    }

    @Override
    protected void runImpl() {
        for (final ClassId cid : ClassId.VALUES) {
            if (cid.getId() == _classId && cid.getLevel() != 1) {
                return;
            }
        }
        final GameClient client = getClient();
        if (client == null) {
            return;
        }
        if (CharacterDAO.getInstance().accountCharNumber(getClient().getLogin()) >= 8) {
            sendPacket(CharacterCreateFail.REASON_TOO_MANY_CHARACTERS);
            return;
        }
        if (!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE)) {
            sendPacket(CharacterCreateFail.REASON_16_ENG_CHARS);
            return;
        }
        if (Util.isMatchingRegexp(_name, Config.CNAME_FORBIDDEN_PATTERN) || CharacterDAO.getInstance().getObjectIdByName(_name) > 0) {
            sendPacket(CharacterCreateFail.REASON_NAME_ALREADY_EXISTS);
            return;
        }
        for (final String forbiddenName : Config.CNAME_FORBIDDEN_NAMES) {
            if (forbiddenName.equalsIgnoreCase(_name)) {
                sendPacket(CharacterCreateFail.REASON_NAME_ALREADY_EXISTS);
                return;
            }
        }
        if (_hairStyle < 0 || (_sex == 0 && _hairStyle > 4) || (_sex != 0 && _hairStyle > 6)) {
            sendPacket(CharacterCreateFail.REASON_CREATION_FAILED);
            return;
        }
        if (_face > 2 || _face < 0) {
            sendPacket(CharacterCreateFail.REASON_CREATION_FAILED);
            return;
        }
        if (_hairColor > 3 || _hairColor < 0) {
            sendPacket(CharacterCreateFail.REASON_CREATION_FAILED);
            return;
        }
        final Player newChar = Player.create(_classId, _sex, getClient().getLogin(), _name, _hairStyle, _hairColor, _face);
        if (newChar == null) {
            return;
        }
        final CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
        sendPacket(CharacterCreateSuccess.STATIC, csi);
        initNewChar(getClient(), newChar);
    }

    private void initNewChar(final GameClient client, final Player newChar) {
        final PlayerTemplate template = newChar.getTemplate();
        Player.restoreCharSubClasses(newChar);
        if (Config.STARTING_ADENA > 0) {
            newChar.addAdena(Config.STARTING_ADENA);
        }
        newChar.setLoc(template.spawnLoc);
        if (Config.ALT_NEW_CHARACTER_LEVEL > 0) {
            newChar.getActiveClass().setExp(Experience.getExpForLevel(Config.ALT_NEW_CHARACTER_LEVEL));
        }
        if (Config.CHAR_TITLE) {
            newChar.setTitle(Config.ADD_CHAR_TITLE);
        } else {
            newChar.setTitle("");
        }
        template.getItems().stream().map(i -> ItemFunctions.createItem(i.getItemId())).forEach(item -> {
            newChar.getInventory().addItem(item);
            if (item.isEquipable() && (newChar.getActiveWeaponItem() == null || item.getTemplate().getType2() != 0)) {
                newChar.getInventory().equipItem(item);
            }
        });
        for (int j = 0; j < Config.STARTING_ITEMS.length; j += 2) {
            final int itemId = Config.STARTING_ITEMS[j];
            final long count = Config.STARTING_ITEMS[j + 1];
            ItemFunctions.addItem(newChar, itemId, count, false);
        }
        SkillAcquireHolder.getInstance().getAvailableSkills(newChar, AcquireType.NORMAL).
                forEach(skill -> newChar.addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), true));
        for (final ShortCut shortCut : CharTemplateTable.getInstance().getShortCuts(newChar.getClassId())) {
            switch (shortCut.getType()) {
                case 2: {
                    final int lvl = newChar.getSkillLevel(shortCut.getId());
                    if (lvl <= 0) {
                        continue;
                    }
                    final ShortCut skillShortCut = new ShortCut(shortCut.getSlot(), shortCut.getPage(), shortCut.getType(), shortCut.getId(), lvl, shortCut.getCharacterType());
                    newChar.registerShortCut(skillShortCut);
                    continue;
                }
                case 1: {
                    final ItemInstance shortCutItem = newChar.getInventory().getItemByItemId(shortCut.getId());
                    if (shortCutItem == null) {
                        continue;
                    }
                    final ShortCut itemShortCut = new ShortCut(shortCut.getSlot(), shortCut.getPage(), shortCut.getType(), shortCutItem.getObjectId(), shortCut.getLevel(), shortCut.getCharacterType());
                    newChar.registerShortCut(itemShortCut);
                    continue;
                }
                default: {
                    newChar.registerShortCut(shortCut);
                    break;
                }
            }
        }


        ItemInstance item = newChar.getInventory().addItem(1467, 1);
        ShortCut newItem = new ShortCut(11, 0, 1, item.getObjectId(), -1, 1);
        newChar.registerShortCut(newItem);
        ItemInstance item1 = newChar.getInventory().addItem(3952, 1);
        ShortCut newItem1 = new ShortCut(10, 0, 1, item1.getObjectId(), -1, 1);
        newChar.registerShortCut(newItem1);
        ItemInstance item2 = newChar.getInventory().addItem(728, 1000);
        ShortCut newItem2 = new ShortCut(3, 0, 1, item2.getObjectId(), -1, 1);
        newChar.registerShortCut(newItem2);


        startInitialQuests(newChar);
        newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
        newChar.setCurrentCp(0.0);
        newChar.setOnlineStatus(false);
        newChar.store(false);
        newChar.getInventory().store();
        newChar.deleteMe();
        client.setCharSelection(CharacterSelectionInfo.loadCharacterSelectInfo(client.getLogin()));
    }
}
