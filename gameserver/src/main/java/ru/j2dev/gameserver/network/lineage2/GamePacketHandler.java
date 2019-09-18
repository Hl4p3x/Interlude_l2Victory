package ru.j2dev.gameserver.network.lineage2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.net.nio.impl.*;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.network.lineage2.clientpackets.*;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public final class GamePacketHandler implements IPacketHandler<GameClient>, IClientFactory<GameClient>, IMMOExecutor<GameClient> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GamePacketHandler.class);

    @Override
    public ReceivablePacket<GameClient> handlePacket(ByteBuffer buf, GameClient client) {
        int id = buf.get() & 0xff;
        L2GameClientPacket msg = null;
        try {
            int id2;
            switch (client.getState()) {
                case CONNECTED: {
                    switch (id) {
                        case 0x00: {
                            msg = new SendProtocolVersion();
                            break;
                        }
                        case 0x08: {
                            msg = new RequestLogin();
                            break;
                        }
                        case 0xCB: {
                            msg = new ReplyGameGuardQuery();
                            break;
                        }
                        case 0xA8: {
                            msg = new NetPing();
                            break;
                        }
                        default:
                            LOGGER.error("Unknown packet on state: CONNECTED, opcode: " + Integer.toHexString(id) + " from " + client.getConnection().getSocket().getInetAddress().getHostAddress());
                            client.onUnknownPacket();
                            break;
                    }
                    break;
                }
                case AUTHED: {
                    switch (id) {
                        case 0x09: {
                            msg = new SendLogOut();
                            break;
                        }
                        case 0x0B: {
                            msg = new RequestCharacterCreate();
                            break;
                        }
                        case 0x0C: {
                            msg = new RequestCharacterDelete();
                            break;
                        }
                        case 0x0D: {
                            msg = new RequestGameStart();
                            break;
                        }
                        case 0x0E: {
                            msg = new RequestNewCharacter();
                            break;
                        }
                        case 0x21: {
                            msg = new RequestBypassToServer();
                            break;
                        }
                        case 0x62: {
                            msg = new RequestCharacterRestore();
                            break;
                        }
                        case 0xCA: {
                            msg = new ReplyGameGuardQuery();
                            break;
                        }
                        case 0xA8: {
                            msg = new NetPing();
                            break;
                        }
                        default:
                            LOGGER.error("Unknown packet on state: AUTHED, opcode: " + Integer.toHexString(id) + " from " + client.getConnection().getSocket().getInetAddress().getHostAddress());
                            client.onUnknownPacket();
                            break;
                    }
                    break;
                }
                case IN_GAME: {
                    switch (id) {
                        case 0x00: {
                            msg = new SendProtocolVersion();
                            break;
                        }
                        case 0x01: {
                            msg = new MoveBackwardToLocation();
                            break;
                        }
                        case 0x02: {
                            //msg = new Say(); //todo check this shit
                            break;
                        }
                        case 0x03: {
                            msg = new RequestEnterWorld();
                            break;
                        }
                        case 0x04: {
                            msg = new Action();
                            break;
                        }
                        case 0x09: {
                            msg = new SendLogOut();
                            break;
                        }
                        case 0x0A: {
                            msg = new AttackRequest();
                            break;
                        }
                        case 0x0F: {
                            msg = new RequestItemList();
                            break;
                        }
                        case 0x10: {
                            //msg = new RequestEquipItem();todo check this shit
                            break;
                        }
                        case 0x11: {
                            msg = new RequestUnEquipItem();
                            break;
                        }
                        case 0x12: {
                            msg = new RequestDropItem();
                            break;
                        }
                        case 0x14: {
                            msg = new RequestUseItem();
                            break;
                        }
                        case 0x15: {
                            msg = new RequestTrade();
                            break;
                        }
                        case 0x16: {
                            msg = new RequestAddTradeItem();
                            break;
                        }
                        case 0x17: {
                            msg = new TradeDone();
                            break;
                        }
                        case 0x1A: {
                            msg = new RequestTeleport();
                            break;
                        }
                        case 0x1B: {
                            msg = new RequestSocialAction();
                            break;
                        }
                        case 0x1E: {
                            msg = new RequestSellItem();
                            break;
                        }
                        case 0x1C: {
                            //msg = new ChangeMoveType(); todo check this shit
                            break;
                        }
                        case 0x1D: {
                            //msg = new ChangeWaitType(); todo chek this shit
                            break;
                        }
                        case 0x1F: {
                            msg = new RequestBuyItem();
                            break;
                        }
                        case 0x20: {
                            msg = new RequestLinkHtml();
                            break;
                        }
                        case 0x21: {
                            msg = new RequestBypassToServer();
                            break;
                        }
                        case 0x22: {
                            msg = new RequestBBSwrite();
                            break;
                        }
                        case 0x23: {
                            msg = new RequestCreatePledge();
                            break;
                        }
                        case 0x24: {
                            msg = new RequestJoinPledge();
                            break;
                        }
                        case 0x25: {
                            msg = new RequestAnswerJoinPledge();
                            break;
                        }
                        case 0x26: {
                            msg = new RequestWithdrawalPledge();
                            break;
                        }
                        case 0x27: {
                            msg = new RequestOustPledgeMember();
                            break;
                        }
                        case 0x28: {
                            //msg = new RequestDismissPledge();todo check this shit
                            break;
                        }
                        case 0x29: {
                            msg = new RequestJoinParty();
                            break;
                        }
                        case 0x2A: {
                            msg = new RequestAnswerJoinParty();
                            break;
                        }
                        case 0x2B: {
                            msg = new RequestWithDrawalParty();
                            break;
                        }
                        case 0x2C: {
                            msg = new RequestOustPartyMember();
                            break;
                        }
                        case 0x2D: {
                            msg = new RequestDismissParty();
                            break;
                        }
                        case 0x2E: {
                            //msg = new UserAck(); todo check this shit
                            break;
                        }
                        case 0x2F: {
                            msg = new RequestMagicSkillUse();
                            break;
                        }
                        case 0x30: {
                            msg = new SendAppearing();
                            break;
                        }
                        case 0x31: {
                            if (!Config.ALLOW_WAREHOUSE)
                                break;
                            msg = new SendWareHouseDepositList();
                            break;
                        }
                        case 0x32: {
                            msg = new SendWareHouseWithDrawList();
                            break;
                        }
                        case 0x33: {
                            msg = new RequestShortCutReg();
                            break;
                        }
                        case 0x34: {
                            //msg = new RequestShortCutUse(); todo check this shit
                            break;
                        }
                        case 0x35: {
                            msg = new RequestShortCutDel();
                            break;
                        }
                        case 0x36: {
                            msg = new CannotMoveAnymore();
                            break;
                        }
                        case 0x37: {
                            msg = new RequestTargetCanceld();
                            break;
                        }
                        case 0x38: {
                            msg = new Say2C();
                            break;
                        }
                        case 0x3C: {
                            msg = new RequestPledgeMemberList();
                            break;
                        }
                        case 0x3E: {
                            //msg = new RequestMagicList(); todo check this shit
                            break;
                        }
                        case 0x3F: {
                            msg = new RequestSkillList();
                            break;
                        }
                        case 0x41: {
                            msg = new MoveWithDelta();
                            break;
                        }
                        case 0x42: {
                            msg = new GetOnVehicle();
                            break;
                        }
                        case 0x43: {
                            msg = new GetOffVehicle();
                            break;
                        }
                        case 0x44: {
                            msg = new AnswerTradeRequest();
                            break;
                        }
                        case 0x45: {
                            msg = new RequestActionUse();
                            break;
                        }
                        case 0x46: {
                            msg = new RequestRestart();
                            break;
                        }
                        case 0x47: {
                            msg = new RequestSiegeInfo();
                            break;
                        }
                        case 0x48: {
                            msg = new ValidatePosition();
                            break;
                        }
                        case 0x49: {
                            msg = new RequestSEKCustom();
                            break;
                        }
                        case 0x4A: {
                            msg = new StartRotatingC();
                            break;
                        }
                        case 0x4B: {
                            msg = new FinishRotatingC();
                            break;
                        }
                        case 0x4D: {
                            msg = new RequestStartPledgeWar();
                            break;
                        }
                        case 0x4E: {
                            //msg = new RequestReplyStartPledgeWar(); todo check this shit
                            break;
                        }
                        case 0x4F: {
                            msg = new RequestStopPledgeWar();
                            break;
                        }
                        case 0x50: {
                            //msg = new RequestReplyStopPledgeWar();todo check this shit
                            break;
                        }
                        case 0x51: {
                            //msg = new RequestSurrenderPledgeWar();todo check this shit
                            break;
                        }
                        case 0x52: {
                            //msg = new RequestReplySurrenderPledgeWar();todo check this shit
                            break;
                        }
                        case 0x53: {
                            msg = new RequestSetPledgeCrest();
                            break;
                        }
                        case 0x55: {
                            msg = new RequestGiveNickName();
                            break;
                        }
                        case 0x57: {
                            msg = new RequestShowboard();
                            break;
                        }
                        case 0x58: {
                            msg = new RequestEnchantItem();
                            break;
                        }
                        case 0x59: {
                            msg = new RequestDestroyItem();
                            break;
                        }
                        case 0x5B: {
                            msg = new SendBypassBuildCmd();
                            break;
                        }
                        case 0x5C: {
                            msg = new RequestMoveToLocationInVehicle();
                            break;
                        }
                        case 0x5D: {
                            msg = new CannotMoveAnymoreInVehicle();
                            break;
                        }
                        case 0x5E: {
                            msg = new RequestFriendInvite();
                            break;
                        }
                        case 0x5F: {
                            msg = new RequestFriendAddReply();
                            break;
                        }
                        case 0x60: {
                            msg = new RequestFriendInfoList();
                            break;
                        }
                        case 0x61: {
                            msg = new RequestFriendDel();
                            break;
                        }
                        case 0x63: {
                            msg = new RequestQuestList();
                            break;
                        }
                        case 0x64: {
                            msg = new RequestDestroyQuest();
                            break;
                        }
                        case 0x66: {
                            msg = new RequestPledgeInfo();
                            break;
                        }
                        case 0x67: {
                            msg = new RequestPledgeExtendedInfo();
                            break;
                        }
                        case 0x68: {
                            msg = new RequestPledgeCrest();
                            break;
                        }
                        case 0x69: {
                            //msg = new RequestSurrenderPersonally();todo check this shit
                            break;
                        }
                        case 0x6A: {
                            //msg = new RequestRide();todo check this shit
                            break;
                        }
                        case 0x6B: {
                            msg = new RequestAquireSkillInfo();
                            break;
                        }
                        case 0x6C: {
                            msg = new RequestAquireSkill();
                            break;
                        }
                        case 0x6D: {
                            msg = new RequestRestartPoint();
                            break;
                        }
                        case 0x6E: {
                            msg = new RequestGMCommand();
                            break;
                        }
                        case 0x6F: {
                            msg = new RequestPartyMatchConfig(); //todo check!!! msg = new RequestListPartyWaiting();
                            break;
                        }
                        case 0x70: {
                            msg = new RequestPartyMatchList(); //todo check!!! msg = new RequestManagePartyRoom();
                            break;
                        }
                        case 0x71: {
                            msg = new RequestJoinPartyRoom();
                            break;
                        }
                        case 0x72: {
                            msg = new RequestCrystallizeItem();
                            break;
                        }
                        case 0x73: {
                            msg = new RequestPrivateStoreSellManageList();
                            break;
                        }
                        case 0x74: {
                            msg = new SetPrivateStoreSellList();
                            break;
                        }
                        case 0x75: {
                            //msg = new RequestPrivateStoreSellManageCancel();todo check this shit
                            break;
                        }
                        case 0x76: {
                            msg = new RequestPrivateStoreSellQuit();
                            break;
                        }
                        case 0x77: {
                            msg = new SetPrivateStoreSellMsg();
                            break;
                        }
                        case 0x79: {
                            msg = new SendPrivateStoreBuyList();
                            break;
                        }
                        case 0x7A: {
                            //msg = new RequestReviveReply();todo check this shit
                            break;
                        }
                        case 0x7B: {
                            msg = new RequestTutorialLinkHtml();
                            break;
                        }
                        case 0x7C: {
                            msg = new RequestTutorialPassCmdToServer();
                            break;
                        }
                        case 0x7D: {
                            msg = new RequestTutorialQuestionMarkPressed();
                            break;
                        }
                        case 0x7E: {
                            msg = new RequestTutorialClientEvent();
                            break;
                        }
                        case 0x7F: {
                            msg = new RequestPetition();
                            break;
                        }
                        case 0x80: {
                            msg = new RequestPetitionCancel();
                            break;
                        }
                        case 0x81: {
                            msg = new RequestGmList();
                            break;
                        }
                        case 0x82: {
                            msg = new RequestJoinAlly();
                            break;
                        }
                        case 0x83: {
                            msg = new RequestAnswerJoinAlly();
                            break;
                        }
                        case 0x84: {
                            msg = new RequestWithdrawAlly();
                            break;
                        }
                        case 0x85: {
                            msg = new RequestOustAlly();
                            break;
                        }
                        case 0x86: {
                            msg = new RequestDismissAlly();
                            break;
                        }
                        case 0x87: {
                            msg = new RequestSetAllyCrest();
                            break;
                        }
                        case 0x88: {
                            msg = new RequestAllyCrest();
                            break;
                        }
                        case 0x89: {
                            msg = new RequestChangePetName();
                            break;
                        }
                        case 0x8A: {
                            msg = new RequestPetUseItem();
                            break;
                        }
                        case 0x8B: {
                            msg = new RequestGiveItemToPet();
                            break;
                        }
                        case 0x8C: {
                            msg = new RequestGetItemFromPet();
                            break;
                        }
                        case 0x8E: {
                            msg = new RequestAllyInfo();
                            break;
                        }
                        case 0x8F: {
                            msg = new RequestPetGetItem();
                            break;
                        }
                        case 0x90: {
                            msg = new RequestPrivateStoreBuyManageList();
                            break;
                        }
                        case 0x91: {
                            msg = new SetPrivateStoreBuyList();
                            break;
                        }
                        case 0x93: {
                            msg = new RequestPrivateStoreBuyManageQuit();
                            break;
                        }
                        case 0x94: {
                            msg = new SetPrivateStoreBuyMsg();
                            break;
                        }
                        case 0x96: {
                            msg = new SendPrivateStoreSellList();
                            break;
                        }
                        case 0x97: {
                            //msg = new SendTimeCheck();todo check this shit
                            break;
                        }
                        case 0x98: {
                            //msg = new RequestStartAllianceWar();todo check this shit
                            break;
                        }
                        case 0x99: {
                            //msg = new ReplyStartAllianceWar();todo check this shit
                            break;
                        }
                        case 0x9A: {
                            //msg = new RequestStopAllianceWar();todo check this shit
                            break;
                        }
                        case 0x9B: {
                            //msg = new ReplyStopAllianceWar();todo check this shit
                            break;
                        }
                        case 0x9C: {
                            //msg = new RequestSurrenderAllianceWar();todo check this shit
                            break;
                        }
                        case 0x9D: {
                            msg = new RequestSkillCoolTime();
                            break;
                        }
                        case 0x9E: {
                            msg = new RequestPackageSendableItemList();
                            break;
                        }
                        case 0x9F: {
                            msg = new RequestPackageSend();
                            break;
                        }
                        case 0xA0: {
                            msg = new RequestBlock();
                            break;
                        }
                        case 0xA1: {
                            //msg = new RequestCastleSiegeInfo();todo check this shit
                            break;
                        }
                        case 0xA2: {
                            msg = new RequestCastleSiegeAttackerList();
                            break;
                        }
                        case 0xA3: {
                            msg = new RequestCastleSiegeDefenderList();
                            break;
                        }
                        case 0xA4: {
                            msg = new RequestJoinCastleSiege();
                            break;
                        }
                        case 0xA5: {
                            msg = new RequestConfirmCastleSiegeWaitingList();
                            break;
                        }
                        case 0xA6: {
                            msg = new RequestSetCastleSiegeTime();
                            break;
                        }
                        case 0xA7: {
                            msg = new RequestMultiSellChoose();
                            break;
                        }
                        case 0xA8: {
                            msg = new NetPing();
                            break;
                        }
                        case 0xA9: {
                            msg = new RequestRemainTime(); //todo impl packet
                            break;
                        }
                        case 0xAA: {
                            msg = new BypassUserCmd();
                            break;
                        }
                        case 0xAB: {
                            msg = new GMSnoopEnd();
                            break;
                        }
                        case 0xAC: {
                            msg = new RequestRecipeBookOpen();
                            break;
                        }
                        case 0xAD: {
                            msg = new RequestRecipeItemDelete();
                            break;
                        }
                        case 0xAE: {
                            msg = new RequestRecipeItemMakeInfo();
                            break;
                        }
                        case 0xAF: {
                            msg = new RequestRecipeItemMakeSelf();
                            break;
                        }
                        case 0xB0: {
                            //msg = new RequestRecipeShopManageList();
                            break;
                        }
                        case 0xB1: {
                            msg = new RequestRecipeShopMessageSet();
                            break;
                        }
                        case 0xB2: {
                            msg = new RequestRecipeShopListSet();
                            break;
                        }
                        case 0xB3: {
                            msg = new RequestRecipeShopManageQuit();
                            break;
                        }
                        case 0xB4: {
                            msg = new RequestRecipeShopManageCancel();
                            break;
                        }
                        case 0xB5: {
                            msg = new RequestRecipeShopMakeInfo();
                            break;
                        }
                        case 0xB6: {
                            msg = new RequestRecipeShopMakeDo();
                            break;
                        }
                        case 0xB7: {
                            msg = new RequestRecipeShopSellList();
                            break;
                        }
                        case 0xB8: {
                            msg = new RequestObserverEnd();
                            break;
                        }
                        case 0xB9: {
                            msg = new VoteSociality();
                            break;
                        }
                        case 0xBA: {
                            msg = new RequestHennaItemList();
                            break;
                        }
                        case 0xBB: {
                            msg = new RequestHennaItemInfo();
                            break;
                        }
                        case 0xBC: {
                            msg = new RequestHennaEquip();
                            break;
                        }
                        case 0xBD: {
                            msg = new RequestHennaUnequipList();
                            break;
                        }
                        case 0xBE: {
                            msg = new RequestHennaUnequipInfo();
                            break;
                        }
                        case 0xBF: {
                            msg = new RequestHennaUnequip();
                            break;
                        }
                        case 0xC0: {
                            msg = new RequestPledgePower();
                            break;
                        }
                        case 0xC1: {
                            msg = new RequestMakeMacro();
                            break;
                        }
                        case 0xC2: {
                            msg = new RequestDeleteMacro();
                            break;
                        }
                        case 0xC3: {
                            msg = new RequestProcureCrop();
                            break;
                        }
                        case 0xC4: {
                            msg = new RequestBuySeed();
                            break;
                        }
                        case 0xC5: {
                            msg = new ConfirmDlg();
                            break;
                        }
                        case 0xC6: {
                            msg = new RequestPreviewItem();
                            break;
                        }
                        case 0xC7: {
                            msg = new RequestSSQStatus();
                            break;
                        }
                        case 0xC8: {
                            msg = new PetitionVote();
                            break;
                        }
                        case 0xCA: {
                            msg = new ReplyGameGuardQuery();
                            break;
                        }
                        case 0xCC: {
                            msg = new RequestSendL2FriendSay();
                            break;
                        }
                        case 0xCD: {
                            msg = new RequestOpenMinimap();
                            break;
                        }
                        case 0xCE: {
                            msg = new RequestSendMsnChatLog();
                            break;
                        }
                        case 0xCF: {
                            msg = new RequestReload();
                            break;
                        }
                        case 0xD0: {
                            if (buf.remaining() < 2) {
                                LOGGER.warn("Client: " + client.toString() + " sent a 0xd0 without the second opcode.");
                                break;
                            }
                            id2 = buf.getShort() & 0xffff;
                            switch (id2) {
                                case 0x01: {
                                    msg = new RequestOustFromPartyRoom();
                                    break;
                                }
                                case 0x02: {
                                    msg = new RequestDismissPartyRoom();
                                    break;
                                }
                                case 0x03: {
                                    msg = new RequestWithdrawPartyRoom();
                                    break;
                                }
                                case 0x04: {
                                    msg = new RequestHandOverPartyMaster();
                                    break;
                                }
                                case 0x05: {
                                    msg = new RequestAutoSoulShot();
                                    break;
                                }
                                case 0x06: {
                                    msg = new RequestExEnchantSkillInfo();
                                    break;
                                }
                                case 0x07: {
                                    msg = new RequestExEnchantSkill();
                                    break;
                                }
                                case 0x08: {
                                    msg = new RequestManorList();
                                    break;
                                }
                                case 0x09: {
                                    msg = new RequestProcureCropList();
                                    break;
                                }
                                case 0x0A: {
                                    msg = new RequestSetSeed();
                                    break;
                                }
                                case 0x0B: {
                                    msg = new RequestSetCrop();
                                    break;
                                }
                                case 0x0C: {
                                    msg = new RequestWriteHeroWords();
                                    break;
                                }
                                case 0x0D: {
                                    msg = new RequestExAskJoinMPCC();
                                    break;
                                }
                                case 0x0E: {
                                    msg = new RequestExAcceptJoinMPCC();
                                    break;
                                }
                                case 0x0F: {
                                    msg = new RequestExOustFromMPCC();
                                    break;
                                }
                                case 0x10: {
                                    msg = new RequestExPledgeCrestLarge();
                                    break;
                                }
                                case 0x11: {
                                    msg = new RequestExSetPledgeCrestLarge();
                                    break;
                                }
                                case 0x12: {
                                    msg = new RequestOlympiadObserverEnd();
                                    break;
                                }
                                case 0x13: {
                                    msg = new RequestOlympiadMatchList();
                                    break;
                                }
                                case 0x14: {
                                    msg = new RequestAskJoinPartyRoom();
                                    break;
                                }
                                case 0x15: {
                                    msg = new AnswerJoinPartyRoom();
                                    break;
                                }
                                case 0x16: {
                                    msg = new RequestListPartyMatchingWaitingRoom();
                                    break;
                                }
                                case 0x17: {
                                    msg = new RequestExitPartyMatchingWaitingRoom();
                                    break;
                                }
                                case 0x18: {
                                    msg = new RequestGetBossRecord();
                                    break;
                                }
                                case 0x19: {
                                    msg = new RequestPledgeSetAcademyMaster();
                                    break;
                                }
                                case 0x1A: {
                                    msg = new RequestPledgePowerGradeList();
                                    break;
                                }
                                case 0x1B: {
                                    msg = new RequestPledgeMemberPowerInfo();
                                    break;
                                }
                                case 0x1C: {
                                    msg = new RequestPledgeSetMemberPowerGrade();
                                    break;
                                }
                                case 0x1D: {
                                    msg = new RequestPledgeMemberInfo();
                                    break;
                                }
                                case 0x1E: {
                                    msg = new RequestPledgeWarList();
                                    break;
                                }
                                case 0x1F: {
                                    msg = new RequestExFishRanking();
                                    break;
                                }
                                case 0x20: {
                                    msg = new RequestPCCafeCouponUse();
                                    break;
                                }
                                case 0x22: {
                                    msg = new RequestCursedWeaponList();
                                    break;
                                }
                                case 0x23: {
                                    msg = new RequestCursedWeaponLocation();
                                    break;
                                }
                                case 0x24: {
                                    msg = new RequestPledgeReorganizeMember();
                                    break;
                                }
                                case 0x26: {
                                    msg = new RequestExMPCCShowPartyMembersInfo();
                                    break;
                                }
                                case 0x27: {
                                    msg = new RequestDuelStart();
                                    break;
                                }
                                case 0x28: {
                                    msg = new RequestDuelAnswerStart();
                                    break;
                                }
                                case 0x29: {
                                    msg = new RequestConfirmTargetItem();
                                    break;
                                }
                                case 0x2A: {
                                    msg = new RequestConfirmRefinerItem();
                                    break;
                                }
                                case 0x2B: {
                                    msg = new RequestConfirmGemStone();
                                    break;
                                }
                                case 0x2C: {
                                    msg = new RequestRefine();
                                    break;
                                }
                                case 0x2D: {
                                    msg = new RequestConfirmCancelItem();
                                    break;
                                }
                                case 0x2E: {
                                    msg = new RequestRefineCancel();
                                    break;
                                }
                                case 0x2F: {
                                    msg = new RequestExMagicSkillUseGround();
                                    break;
                                }
                                case 0x30: {
                                    msg = new RequestDuelSurrender();
                                    break;
                                }
                                case 0x31: {
                                    msg = new RequestExChangeName();
                                    break;
                                }
                                default:
                                    LOGGER.error("Unknown packet on state: IN_GAME, opcode: " + Integer.toHexString(id2) + " from " + client.getConnection().getSocket().getInetAddress().getHostAddress());
                                    client.onUnknownPacket();
                                    break;
                            }
                            break;
                        }
                        default: {
                            client.onUnknownPacket();
                            break;
                        }
                    }
                }
            }
        } catch (BufferUnderflowException e) {
            client.onPacketReadFail();
        }
        return msg;
    }

    @Override
    public GameClient create(final MMOConnection<GameClient> con) {
        return new GameClient(con);
    }

    @Override
    public void execute(final Runnable r) {
        ThreadPoolManager.getInstance().execute(r);
    }
}
