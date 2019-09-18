package ru.j2dev.gameserver.templates;

import ru.j2dev.gameserver.data.xml.holder.InstantZoneHolder;
import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.model.CommandChannel;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.utils.ItemFunctions;

public enum InstantZoneEntryType {
    SOLO {
        @Override
        public boolean canEnter(final Player player, final InstantZone instancedZone) {
            if (player.isInParty()) {
                player.sendMessage(new CustomMessage("A_PARTY_CANNOT_BE_FORMED_IN_THIS_AREA", player));
                return false;
            }
            final CustomMessage cmsg = checkPlayer(player, instancedZone);
            if (cmsg != null) {
                player.sendMessage(cmsg);
                return false;
            }
            return true;
        }

        @Override
        public boolean canReEnter(final Player player, final InstantZone instancedZone) {
            if (player.isCursedWeaponEquipped() || player.isInFlyingTransform()) {
                player.sendMessage(new CustomMessage("YOU_CANNOT_ENTER_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS", player));
                return false;
            }
            return true;
        }
    },
    PARTY {
        @Override
        public boolean canEnter(final Player player, final InstantZone instancedZone) {
            final Party party = player.getParty();
            if (party == null) {
                player.sendMessage(new CustomMessage("YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER", player));
                return false;
            }
            if (!party.isLeader(player)) {
                player.sendMessage(new CustomMessage("ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER", player));
                return false;
            }
            if (party.getMemberCount() < instancedZone.getMinParty()) {
                player.sendMessage(new CustomMessage("YOU_MUST_HAVE_A_MINIMUM_OF_S1_PEOPLE_TO_ENTER_THIS_INSTANT_ZONE", player).addNumber(instancedZone.getMinParty()));
                return false;
            }
            if (party.getMemberCount() > instancedZone.getMaxParty()) {
                player.sendMessage(new CustomMessage("YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT", player));
                return false;
            }
            for (final Player member : party.getPartyMembers()) {
                if (!player.isInRange(member, 500L)) {
                    for (final Player partyPlayer : member) {
                        partyPlayer.sendMessage(new CustomMessage("C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED", partyPlayer, member));
                    }
                    return false;
                }
                final CustomMessage cmsg = checkPlayer(member, instancedZone);
                if (cmsg != null) {
                    player.sendMessage(cmsg);
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean canReEnter(final Player player, final InstantZone instanceZone) {
            final Party party = player.getParty();
            if (party == null) {
                player.sendMessage(new CustomMessage("YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER", player));
                return false;
            }
            if (party.getMemberCount() > instanceZone.getMaxParty()) {
                player.sendMessage(new CustomMessage("YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT", player));
                return false;
            }
            if (player.isCursedWeaponEquipped() || player.isInFlyingTransform()) {
                player.sendMessage(new CustomMessage("YOU_CANNOT_ENTER_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS", player));
                return false;
            }
            return true;
        }
    },
    COMMAND_CHANNEL {
        @Override
        public boolean canEnter(final Player player, final InstantZone instancedZone) {
            final Party party = player.getParty();
            if (party == null || party.getCommandChannel() == null) {
                player.sendMessage(new CustomMessage("YOU_CANNOT_ENTER_BECAUSE_YOU_ARE_NOT_ASSOCIATED_WITH_THE_CURRENT_COMMAND_CHANNEL", player));
                return false;
            }
            final CommandChannel cc = party.getCommandChannel();
            if (cc.getMemberCount() < instancedZone.getMinParty()) {
                player.sendMessage(new CustomMessage("YOU_MUST_HAVE_A_MINIMUM_OF_S1_PEOPLE_TO_ENTER_THIS_INSTANT_ZONE", player).addNumber(instancedZone.getMinParty()));
                return false;
            }
            if (cc.getMemberCount() > instancedZone.getMaxParty()) {
                player.sendMessage(new CustomMessage("YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT", player));
                return false;
            }
            for (final Player member : cc) {
                if (!player.isInRange(member, 500L)) {
                    for (final Player partyPlayer : cc) {
                        partyPlayer.sendMessage(new CustomMessage("C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED", partyPlayer, member));
                    }
                    return false;
                }
                final CustomMessage cmsg = checkPlayer(member, instancedZone);
                if (cmsg != null) {
                    player.sendMessage(cmsg);
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean canReEnter(final Player player, final InstantZone instanceZone) {
            final Party commparty = player.getParty();
            if (commparty == null || commparty.getCommandChannel() == null) {
                player.sendMessage(new CustomMessage("YOU_CANNOT_ENTER_BECAUSE_YOU_ARE_NOT_ASSOCIATED_WITH_THE_CURRENT_COMMAND_CHANNEL", player));
                return false;
            }
            final CommandChannel cc = commparty.getCommandChannel();
            if (cc.getMemberCount() > instanceZone.getMaxParty()) {
                player.sendMessage(new CustomMessage("YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT", player));
                return false;
            }
            if (player.isCursedWeaponEquipped() || player.isInFlyingTransform()) {
                player.sendMessage(new CustomMessage("YOU_CANNOT_ENTER_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS", player));
                return false;
            }
            return true;
        }
    };

    private static CustomMessage checkPlayer(final Player player, final InstantZone instancedZone) {
        if (player.getActiveReflection() != null) {
            return new CustomMessage("YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON", player);
        }
        if (player.getLevel() < instancedZone.getMinLevel() || player.getLevel() > instancedZone.getMaxLevel()) {
            return new CustomMessage("C1S_LEVEL_DOES_NOT_CORRESPOND_TO_THE_REQUIREMENTS_FOR_ENTRY", player).addCharName(player);
        }
        if (player.isCursedWeaponEquipped() || player.isInFlyingTransform()) {
            return new CustomMessage("YOU_CANNOT_ENTER_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS", player);
        }
        if (InstantZoneHolder.getInstance().getMinutesToNextEntrance(instancedZone.getId(), player) > 0) {
            return new CustomMessage("C1_MAY_NOT_REENTER_YET", player).addCharName(player);
        }
        if (instancedZone.getRemovedItemId() > 0 && instancedZone.getRemovedItemNecessity() && ItemFunctions.getItemCount(player, instancedZone.getRemovedItemId()) < 1L) {
            return new CustomMessage("C1S_ITEM_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED", player).addCharName(player);
        }
        if (instancedZone.getRequiredQuestId() > 0) {
            final Quest q = QuestManager.getQuest(instancedZone.getRequiredQuestId());
            final QuestState qs = player.getQuestState(q.getClass());
            if (qs == null || qs.getState() != 2) {
                return new CustomMessage("C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED", player).addCharName(player);
            }
        }
        return null;
    }

    public abstract boolean canEnter(final Player p0, final InstantZone p1);

    public abstract boolean canReEnter(final Player p0, final InstantZone p1);
}
