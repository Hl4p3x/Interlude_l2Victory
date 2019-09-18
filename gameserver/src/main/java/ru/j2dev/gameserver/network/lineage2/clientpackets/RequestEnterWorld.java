package ru.j2dev.gameserver.network.lineage2.clientpackets;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.commons.net.nio.impl.SelectorThread;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.commons.versioning.Version;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.listener.actor.player.OnAnswerListener;
import ru.j2dev.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import ru.j2dev.gameserver.manager.*;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.base.InvisibleType;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import ru.j2dev.gameserver.model.entity.olympiad.HeroManager;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.SubUnit;
import ru.j2dev.gameserver.model.pledge.UnitMember;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ConfirmDlg;
import ru.j2dev.gameserver.skills.AbnormalEffect;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.GameStats;
import ru.j2dev.gameserver.utils.PtsUtils;
import ru.j2dev.gameserver.utils.TradeHelper;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class RequestEnterWorld extends L2GameClientPacket {
    public static Version version;


    private static void notifyClanMembers(final Player activeChar) {
        final Clan clan = activeChar.getClan();
        final SubUnit subUnit = activeChar.getSubUnit();
        if (clan == null || subUnit == null) {
            return;
        }
        final UnitMember member = subUnit.getUnitMember(activeChar.getObjectId());
        if (member == null) {
            return;
        }
        member.setPlayerInstance(activeChar, false);
        final int sponsor = activeChar.getSponsor();
        final int apprentice = activeChar.getApprentice();
        final L2GameServerPacket msg = (new SystemMessage2(SystemMsg.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME)).addName(activeChar);
        final PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(activeChar);
        for (final Player clanMember : clan.getOnlineMembers(activeChar.getObjectId())) {
            clanMember.sendPacket(memberUpdate);
            if (clanMember.getObjectId() == sponsor) {
                clanMember.sendPacket((new SystemMessage2(SystemMsg.YOUR_APPRENTICE_C1_HAS_LOGGED_OUT)).addName(activeChar));
            } else if (clanMember.getObjectId() == apprentice) {
                clanMember.sendPacket((new SystemMessage2(SystemMsg.YOUR_SPONSOR_C1_HAS_LOGGED_IN)).addName(activeChar));
            } else {
                clanMember.sendPacket(msg);
            }
        }
        if (!activeChar.isClanLeader()) {
            return;
        }
        final ClanHall clanHall = (clan.getHasHideout() > 0) ? ResidenceHolder.getInstance().getResidence(ClanHall.class, clan.getHasHideout()) : null;
        if (clanHall == null || clanHall.getAuctionLength() != 0) {
            return;
        }
        if (clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class) {
            return;
        }
        if (clan.getWarehouse().getCountOf(57) < clanHall.getRentalFee()) {
            activeChar.sendPacket((new SystemMessage2(SystemMsg.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_ME_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW)).addLong(clanHall.getRentalFee()));
        }
    }

    @Override
    protected void readImpl() {
    }

    @HideAccess
    @StringEncryption
    @Override
    protected void runImpl() {
        version = new Version(RequestEnterWorld.class);
        final GameClient client = getClient();
        final Player activeChar = client.getActiveChar();
        if (activeChar == null) {
            client.closeNow(false);
            return;
        }
        GameStats.incrementPlayerEnterGame();
        final boolean first = activeChar.entering;
        if (first) {
            activeChar.setOnlineStatus(true);
            if (activeChar.getPlayerAccess().GodMode && (!Config.SAVE_GM_EFFECTS || (Config.SAVE_GM_EFFECTS && !activeChar.getVarB("gm_vis")))) {
                activeChar.setInvisibleType(InvisibleType.NORMAL);
            }
            activeChar.setNonAggroTime(Long.MAX_VALUE);
            activeChar.spawnMe();
            if (activeChar.isInStoreMode() && !TradeHelper.checksIfCanOpenStore(activeChar, activeChar.getPrivateStoreType())) {
                activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
                activeChar.standUp();
                activeChar.broadcastCharInfo();
            }
            activeChar.setRunning();
            activeChar.standUp();
            activeChar.startTimers();
        }
        activeChar.getMacroses().sendUpdate();
        activeChar.sendPacket(new SSQInfo(), new HennaInfo(activeChar));
        activeChar.sendPacket(new SkillList(activeChar), new SkillCoolTime(activeChar));
        if (Config.SEND_LINEAGE2_WELCOME_MESSAGE) {
            activeChar.sendPacket(SystemMsg.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);
        }
        Announcements.getInstance().showAnnouncements(activeChar);
        if (Config.SEND_SSQ_WELCOME_MESSAGE) {
            SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
        }
        if (first) {
            activeChar.getListeners().onEnter();
        }
        if (activeChar.getClan() != null) {
            notifyClanMembers(activeChar);
            activeChar.sendPacket(activeChar.getClan().listAll());
            activeChar.sendPacket(new PledgeShowInfoUpdate(activeChar.getClan()), new PledgeSkillList(activeChar.getClan()));
        }
        if (Config.SHOW_HTML_WELCOME && (activeChar.getClan() == null || activeChar.getClan().getNotice() == null || activeChar.getClan().getNotice().isEmpty())) {
            final NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setFile("welcome.htm");
            sendPacket(html);
        }

        if (activeChar.isGM()) {
            activeChar.sendMessage("Revision: .... " + version.getRevisionNumber());
            activeChar.sendMessage("Version: ..... " + version.getVersionNumber());
            activeChar.sendMessage("Build date: .. " + version.getBuildDate());
        }
        if (first && Config.ALLOW_WEDDING) {
            CoupleManager.getInstance().engage(activeChar);
            CoupleManager.getInstance().notifyPartner(activeChar);
        }
        if (first) {
            activeChar.getFriendList().notifyFriends(true);
            //loadTutorial(activeChar);
            loadTutorial(activeChar);
            activeChar.restoreDisableSkills();
        }
        sendPacket(new L2FriendList(activeChar), new QuestList(activeChar), new EtcStatusUpdate(activeChar));
        activeChar.checkHpMessages(activeChar.getMaxHp(), activeChar.getCurrentHp());
        activeChar.checkDayNightMessages();
        if (Config.PETITIONING_ALLOWED) {
            PetitionManager.getInstance().checkPetitionMessages(activeChar);
            if (activeChar.isGM() && PetitionManager.getInstance().isPetitionPending()) {
                activeChar.sendPacket(new Say2(0, ChatType.CRITICAL_ANNOUNCE, "SYS", "There are pended petition(s)"));
                activeChar.sendPacket(new Say2(0, ChatType.CRITICAL_ANNOUNCE, "SYS", "Show all petition: //view_petitions"));
            }
        }
        if (!first) {
            if (activeChar.isCastingNow()) {
                final Creature castingTarget = activeChar.getCastingTarget();
                final Skill castingSkill = activeChar.getCastingSkill();
                final long animationEndTime = activeChar.getAnimationEndTime();
                if (castingSkill != null && castingTarget != null && castingTarget.isCreature() && activeChar.getAnimationEndTime() > 0L) {
                    sendPacket(new MagicSkillUse(activeChar, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0L));
                }
            }
            if (activeChar.isInBoat()) {
                activeChar.sendPacket(activeChar.getBoat().getOnPacket(activeChar, activeChar.getInBoatPosition()));
            }
            if (activeChar.isMoving() || activeChar.isFollowing()) {
                sendPacket(activeChar.movePacket());
            }
            if (activeChar.getMountNpcId() != 0) {
                sendPacket(new Ride(activeChar));
            }
            if (activeChar.isFishing()) {
                activeChar.stopFishing();
            }
        }
        activeChar.entering = false;
        activeChar.sendUserInfo(true);
        activeChar.sendItemList(false);
        activeChar.sendPacket(new ShortCutInit(activeChar));
        if (activeChar.isSitting()) {
            activeChar.sendPacket(new ChangeWaitType(activeChar, 0));
        }
        if (activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_NONE) {
            switch (activeChar.getPrivateStoreType()) {
                case Player.STORE_PRIVATE_BUY:
                    sendPacket(new PrivateStoreMsgBuy(activeChar));
                    break;
                case Player.STORE_PRIVATE_SELL:
                case Player.STORE_PRIVATE_SELL_PACKAGE:
                    sendPacket(new PrivateStoreMsgSell(activeChar));
                    break;
                case Player.STORE_PRIVATE_MANUFACTURE:
                    sendPacket(new RecipeShopMsg(activeChar));
                    break;
            }
        }
        if (activeChar.isDead()) {
            sendPacket(new Die(activeChar));
        }
        activeChar.unsetVar("offline");
        activeChar.sendActionFailed();
        if (first && activeChar.isGM() && Config.SAVE_GM_EFFECTS && activeChar.getPlayerAccess().CanUseGMCommand) {
            if (activeChar.getVarB("gm_silence")) {
                activeChar.setMessageRefusal(true);
                activeChar.sendPacket(SystemMsg.MESSAGE_REFUSAL_MODE);
            }
            if (activeChar.getVarB("gm_invul")) {
                activeChar.setIsInvul(true);
                activeChar.startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
                activeChar.sendMessage(activeChar.getName() + " is now immortal.");
            }
            try {
                final int var_gmspeed = Integer.parseInt(activeChar.getVar("gm_gmspeed"));
                if (var_gmspeed >= 1 && var_gmspeed <= 4) {
                    activeChar.doCast(SkillTable.getInstance().getInfo(7029, var_gmspeed), activeChar, true);
                }
            } catch (Exception ignored) {
            }
        }
        if (first && activeChar.isGM() && activeChar.getPlayerAccess().GodMode && Config.SHOW_GM_LOGIN && activeChar.getInvisibleType() == InvisibleType.NONE) {
            Announcements.getInstance().announceByCustomMessage("enterworld.show.gm.login", new String[]{activeChar.getName()});
        }
        PlayerMessageStack.getInstance().CheckMessages(activeChar);
        sendPacket(ClientSetTime.STATIC, new ExSetCompassZoneCode(activeChar));
        final Pair<Integer, OnAnswerListener> entry = activeChar.getAskListener(false);
        if (entry != null && entry.getValue() instanceof ReviveAnswerListener) {
            sendPacket((new ConfirmDlg(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0).addString("Other player")).addString("some"));
        }
        if (activeChar.isCursedWeaponEquipped()) {
            final CursedWeaponsManager cursedManagerInstance = CursedWeaponsManager.getInstance();
            cursedManagerInstance.getCursedWeapon(activeChar.getCursedWeaponEquippedId()).giveSkillAndUpdateStats();
            cursedManagerInstance.showUsageTime(activeChar, activeChar.getCursedWeaponEquippedId());
        }
        if (HeroManager.isHaveHeroWeapon(activeChar)) {
            HeroManager.checkHeroWeaponary(activeChar);
        }
        if (!first) {
            if (activeChar.isInObserverMode()) {
                if (activeChar.getObserverMode() == 2) {
                    activeChar.returnFromObserverMode();
                } else if (activeChar.isOlyObserver()) {
                    activeChar.leaveOlympiadObserverMode();
                } else {
                    activeChar.leaveObserverMode();
                }
            } else if (activeChar.isVisible()) {
                World.showObjectsToPlayer(activeChar);
            }
            if (activeChar.getPet() != null) {
                sendPacket(new PetInfo(activeChar.getPet()));
            }
            if (activeChar.isInParty()) {
                sendPacket(new PartySmallWindowAll(activeChar.getParty(), activeChar));
                for (final Player member : activeChar.getParty().getPartyMembers()) {
                    if (member != activeChar) {
                        sendPacket(new PartySpelled(member, true));
                        final Summon member_pet;
                        if ((member_pet = member.getPet()) != null) {
                            sendPacket(new PartySpelled(member_pet, true));
                        }
                        sendPacket(RelationChanged.create(activeChar, member, activeChar));
                    }
                }
                if (activeChar.getParty().isInCommandChannel()) {
                    sendPacket(ExMPCCOpen.STATIC);
                }
            }
            for (final int shotId : activeChar.getAutoSoulShot()) {
                sendPacket(new ExAutoSoulShot(shotId, true));
            }
            for (final Effect e2 : activeChar.getEffectList().getAllFirstEffects()) {
                if (e2.getSkill().isToggle()) {
                    sendPacket(new MagicSkillLaunched(activeChar, e2.getSkill(), activeChar.getObjectId()));
                }
            }
            activeChar.broadcastCharInfo();
        } else {
            activeChar.sendUserInfo();
        }
        activeChar.updateEffectIcons();
        activeChar.updateStats();
        if (Config.ALT_PCBANG_POINTS_ENABLED) {
            activeChar.sendPacket(new ExPCCafePointInfo(activeChar, 0, 1, 2, 12));
        }
        if (!activeChar.getPremiumItemList().isEmpty()) {
            activeChar.sendPacket(Config.GOODS_INVENTORY_ENABLED ? ExGoodsInventoryChangedNotify.STATIC : ExNotifyPremiumItem.STATIC);
        }
        if (activeChar.getOnlineTime() == 0L && !Config.ALT_STAR_CHAR) {
            for (final Pair<Integer, Integer> skIdLvl : activeChar.isMageClass() ? Config.OTHER_MAGE_BUFF_ON_CHAR_CREATE : Config.OTHER_WARRIOR_BUFF_ON_CHAR_CREATE) {
                final Skill skill = SkillTable.getInstance().getInfo(skIdLvl.getLeft(), skIdLvl.getRight());
                skill.getEffects(activeChar, activeChar, false, false);
            }
        }
    }

    private void loadTutorial(final Player player) {
        final Quest q = QuestManager.getQuest(255);
        if (q != null) {
            player.processQuestEvent(q.getName(), "UC", null);
        }
    }

}
