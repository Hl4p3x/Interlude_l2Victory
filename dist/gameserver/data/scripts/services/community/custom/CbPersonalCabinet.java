package services.community.custom;

import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.handler.bbs.CommunityBoardManager;
import ru.j2dev.gameserver.handler.bbs.ICommunityBoardHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.authcomm.AuthServerCommunication;
import ru.j2dev.gameserver.network.authcomm.gs2as.IGPwdCng;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShowBoard;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class CbPersonalCabinet implements OnInitScriptListener, ICommunityBoardHandler {
    private static final Logger LOG = LoggerFactory.getLogger(CbPersonalCabinet.class);
    private static final Pattern PASSWORD_BYPASS_PATTERN = Pattern.compile("^([\\w\\d_-]{4,18})$");
    private static final long PASSWORD_CHANGE_INTERVAL = 3600000L;
    private static final String[] BBSBYPASSES = {"_bbscpassword", "_bbscrepair", "_bbscstats"};

    private static List<Pair<Integer, String>> getCharactersOfAccount(final String login) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        final List<Pair<Integer, String>> result = new LinkedList<>();
        try {
            con = DatabaseFactory.getInstance().getConnection();
            pstmt = con.prepareStatement("SELECT `obj_Id`, `char_name` FROM `characters` WHERE `account_name` = ? AND `online` = 0");
            pstmt.setString(1, login);
            rset = pstmt.executeQuery();
            while (rset.next()) {
                final int charId = rset.getInt("obj_Id");
                final String charName = rset.getString("char_name");
                result.add(new ImmutablePair<>(charId, charName));
            }
        } catch (Exception ex) {
            CbPersonalCabinet.LOG.error("Error while getting characters of " + login, ex);
        } finally {
            DbUtils.closeQuietly(con, pstmt, rset);
        }
        return result;
    }

    private static void repairCharacter(final int charObjId) {
        Connection con = null;
        PreparedStatement fpstmt = null;
        final ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            fpstmt = con.prepareStatement("UPDATE `characters` SET `x` = 17867, `y` = 170259, `z` = -3503 WHERE `obj_Id` = ?");
            fpstmt.setInt(1, charObjId);
            fpstmt.executeUpdate();
            DbUtils.close(fpstmt);
            fpstmt = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=?");
            fpstmt.setInt(1, charObjId);
            fpstmt.executeUpdate();
            DbUtils.close(fpstmt);
            fpstmt = con.prepareStatement("UPDATE `items` SET `location` = \"WAREHOUSE\" WHERE `location` IN (\"PAPERDOLL\", \"INVENTORY\") AND `owner_id` = ? AND `item_type` NOT IN (13530, 13531, 13532, 13533, 13534, 13535, 13536, 13537, 13538, 10281, 10283, 10282, 10286, 10284, 10285, 10287, 10289, 10290, 10288, 10294, 10292, 10291, 10293, 10280, 10612)");
            fpstmt.setInt(1, charObjId);
            fpstmt.executeUpdate();
            DbUtils.close(fpstmt);
        } catch (Exception ex) {
            CbPersonalCabinet.LOG.error("Error while repairing character " + charObjId, ex);
        } finally {
            DbUtils.closeQuietly(con, fpstmt, rset);
        }
    }

    @Override
    public String[] getBypassCommands() {
        return CbPersonalCabinet.BBSBYPASSES;
    }

    private void onRequestChangePassword(final Player player, final String arg) {
        final StringTokenizer st = new StringTokenizer(arg);
        if (!st.hasMoreTokens()) {
            return;
        }
        final String currentPassword = st.nextToken();
        if (!st.hasMoreTokens()) {
            player.sendMessage("New password required.");
            return;
        }
        final String newPassword0 = st.nextToken();
        if (!st.hasMoreTokens()) {
            player.sendMessage("Confirm new password required.");
            return;
        }
        final String newPassword2 = st.nextToken();
        if (!newPassword0.equals(newPassword2)) {
            player.sendMessage("New password and confirm must match.");
            return;
        }
        if (!CbPersonalCabinet.PASSWORD_BYPASS_PATTERN.matcher(newPassword0).matches()) {
            player.sendMessage("Password requirement's is not met!");
            return;
        }
        final String lastChanged = player.getVar("LastPwdChng");
        if (lastChanged != null && !lastChanged.isEmpty()) {
            final long lastChange = Long.parseLong(lastChanged) * 1000L;
            if (lastChange + 3600000L > System.currentTimeMillis()) {
                player.sendMessage("Password can't be change so frequently.");
                return;
            }
        }
        AuthServerCommunication.getInstance().sendPacket(new IGPwdCng(player, currentPassword, newPassword0));
    }

    private int getRepairRequestCharObjId(final String arg) {
        final StringTokenizer st = new StringTokenizer(arg);
        if (!st.hasMoreTokens()) {
            return -1;
        }
        final String objIdStr = st.nextToken();
        int objId;
        try {
            objId = Integer.parseInt(objIdStr);
        } catch (NumberFormatException nfe) {
            return -1;
        }
        return objId;
    }

    private String makeWhiIAmHtml(final Player player, final String html) {
        final Creature target = null;
        final double hpRegen = Formulas.calcHpRegen(player);
        final double cpRegen = Formulas.calcCpRegen(player);
        final double mpRegen = Formulas.calcMpRegen(player);
        final double hpDrain = player.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0, target, null);
        final double hpGain = player.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0, target, null);
        final double mpGain = player.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0, target, null);
        final double critPerc = 2.0 * player.calcStat(Stats.CRITICAL_DAMAGE, target, null);
        final double critStatic = player.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, null);
        final double mCritRate = player.calcStat(Stats.MCRITICAL_RATE, target, null);
        final double blowRate = player.calcStat(Stats.FATALBLOW_RATE, target, null);
        final ItemInstance shld = player.getSecondaryWeaponInstance();
        final boolean shield = shld != null && shld.getItemType() == WeaponType.NONE;
        final double shieldDef = shield ? player.calcStat(Stats.SHIELD_DEFENCE, (double) player.getTemplate().getBaseShldDef(), target, null) : 0.0;
        final double shieldRate = shield ? player.calcStat(Stats.SHIELD_RATE, target, null) : 0.0;
        final double SkillPower = player.calcStat(Stats.SKILL_POWER, 1.0, target, null);
        final double PvPPhysDmg = player.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1.0, target, null);
        final double PvPSkillDmg = player.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1.0, target, null);
        final double MagicPvPSkillDmg = player.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1.0, target, null);
        final double pSkillEvas = player.calcStat(Stats.PSKILL_EVASION, null, null);
        final double reflectDam = player.calcStat(Stats.REFLECT_DAMAGE_PERCENT, target, null);
        final double reflectSMagic = player.calcStat(Stats.REFLECT_MAGIC_SKILL, target, null);
        final double reflectSPhys = player.calcStat(Stats.REFLECT_PHYSIC_SKILL, target, null);
        final double meleePhysRes = player.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, target, null);
        final double pReuse = player.calcStat(Stats.PHYSIC_REUSE_RATE, target, null);
        final double mReuse = player.calcStat(Stats.MAGIC_REUSE_RATE, target, null);
        final double bleedPower = player.calcStat(Stats.BLEED_POWER, target, null);
        final double bleedResist = player.calcStat(Stats.BLEED_RESIST, target, null);
        final double poisonPower = player.calcStat(Stats.POISON_POWER, target, null);
        final double poisonResist = player.calcStat(Stats.POISON_RESIST, target, null);
        final double stunPower = player.calcStat(Stats.STUN_POWER, target, null);
        final double stunResist = player.calcStat(Stats.STUN_RESIST, target, null);
        final double rootPower = player.calcStat(Stats.ROOT_POWER, target, null);
        final double rootResist = player.calcStat(Stats.ROOT_RESIST, target, null);
        final double sleepPower = player.calcStat(Stats.SLEEP_POWER, target, null);
        final double sleepResist = player.calcStat(Stats.SLEEP_RESIST, target, null);
        final double paralyzePower = player.calcStat(Stats.PARALYZE_POWER, target, null);
        final double paralyzeResist = player.calcStat(Stats.PARALYZE_RESIST, target, null);
        final double mentalPower = player.calcStat(Stats.MENTAL_POWER, target, null);
        final double mentalResist = player.calcStat(Stats.MENTAL_RESIST, target, null);
        final double debuffPower = player.calcStat(Stats.DEBUFF_POWER, target, null);
        final double debuffResist = player.calcStat(Stats.DEBUFF_RESIST, target, null);
        final double cancelPower = player.calcStat(Stats.CANCEL_POWER, target, null);
        final double cancelResist = player.calcStat(Stats.CANCEL_RESIST, target, null);
        final double swordResist = 100.0 - player.calcStat(Stats.SWORD_WPN_VULNERABILITY, target, null);
        final double dualResist = 100.0 - player.calcStat(Stats.DUAL_WPN_VULNERABILITY, target, null);
        final double bluntResist = 100.0 - player.calcStat(Stats.BLUNT_WPN_VULNERABILITY, target, null);
        final double daggerResist = 100.0 - player.calcStat(Stats.DAGGER_WPN_VULNERABILITY, target, null);
        final double bowResist = 100.0 - player.calcStat(Stats.BOW_WPN_VULNERABILITY, target, null);
        final double poleResist = 100.0 - player.calcStat(Stats.POLE_WPN_VULNERABILITY, target, null);
        final double fistResist = 100.0 - player.calcStat(Stats.FIST_WPN_VULNERABILITY, target, null);
        final double critChanceResist = 100.0 - player.calcStat(Stats.CRIT_CHANCE_RECEPTIVE, target, null);
        final double critDamResistStatic = player.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, target, null);
        final double critDamResist = 100.0 - 100.0 * (player.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, 1.0, target, null) - critDamResistStatic);
        final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
        df.setMaximumFractionDigits(1);
        df.setMinimumFractionDigits(1);
        final NumberFormat df2 = NumberFormat.getInstance(Locale.ENGLISH);
        df2.setMaximumFractionDigits(1);
        df2.setMinimumFractionDigits(2);
        final StrBuilder sb = new StrBuilder(html);
        sb.replaceFirst("%hpRegen%", df.format(hpRegen));
        sb.replaceFirst("%cpRegen%", df.format(cpRegen));
        sb.replaceFirst("%mpRegen%", df.format(mpRegen));
        sb.replaceFirst("%hpDrain%", df.format(hpDrain));
        sb.replaceFirst("%hpGain%", df.format(hpGain));
        sb.replaceFirst("%mpGain%", df.format(mpGain));
        sb.replaceFirst("%critPerc%", df.format(critPerc));
        sb.replaceFirst("%critStatic%", df.format(critStatic));
        sb.replaceFirst("%mCritRate%", df.format(mCritRate));
        sb.replaceFirst("%blowRate%", df.format(blowRate));
        sb.replaceFirst("%shieldDef%", df.format(shieldDef));
        sb.replaceFirst("%shieldRate%", df.format(shieldRate));
        sb.replaceFirst("%bleedPower%", df.format(bleedPower));
        sb.replaceFirst("%bleedResist%", df.format(bleedResist));
        sb.replaceFirst("%poisonPower%", df.format(poisonPower));
        sb.replaceFirst("%poisonResist%", df.format(poisonResist));
        sb.replaceFirst("%stunPower%", df.format(stunPower));
        sb.replaceFirst("%stunResist%", df.format(stunResist));
        sb.replaceFirst("%rootPower%", df.format(rootPower));
        sb.replaceFirst("%SkillPower%", df2.format(SkillPower));
        sb.replaceFirst("%PvPPhysDmg%", df2.format(PvPPhysDmg));
        sb.replaceFirst("%PvPSkillDmg%", df2.format(PvPSkillDmg));
        sb.replaceFirst("%MagicPvPSkillDmg%", df2.format(MagicPvPSkillDmg));
        sb.replaceFirst("%pSkillEvas%", df.format(pSkillEvas));
        sb.replaceFirst("%reflectDam%", df.format(reflectDam));
        sb.replaceFirst("%reflectSMagic%", df.format(reflectSMagic));
        sb.replaceFirst("%reflectSPhys%", df.format(reflectSPhys));
        sb.replaceFirst("%meleePhysRes%", df.format(meleePhysRes));
        sb.replaceFirst("%pReuse%", df.format(pReuse));
        sb.replaceFirst("%mReuse%", df.format(mReuse));
        sb.replaceFirst("%rootResist%", df.format(rootResist));
        sb.replaceFirst("%sleepPower%", df.format(sleepPower));
        sb.replaceFirst("%sleepResist%", df.format(sleepResist));
        sb.replaceFirst("%paralyzePower%", df.format(paralyzePower));
        sb.replaceFirst("%paralyzeResist%", df.format(paralyzeResist));
        sb.replaceFirst("%mentalPower%", df.format(mentalPower));
        sb.replaceFirst("%mentalResist%", df.format(mentalResist));
        sb.replaceFirst("%debuffPower%", df.format(debuffPower));
        sb.replaceFirst("%debuffResist%", df.format(debuffResist));
        sb.replaceFirst("%cancelPower%", df.format(cancelPower));
        sb.replaceFirst("%cancelResist%", df.format(cancelResist));
        sb.replaceFirst("%swordResist%", df.format(swordResist));
        sb.replaceFirst("%dualResist%", df.format(dualResist));
        sb.replaceFirst("%bluntResist%", df.format(bluntResist));
        sb.replaceFirst("%daggerResist%", df.format(daggerResist));
        sb.replaceFirst("%bowResist%", df.format(bowResist));
        sb.replaceFirst("%fistResist%", df.format(fistResist));
        sb.replaceFirst("%poleResist%", df.format(poleResist));
        sb.replaceFirst("%critChanceResist%", df.format(critChanceResist));
        sb.replaceFirst("%critDamResist%", df.format(critDamResist));
        return sb.toString();
    }

    @Override
    public void onBypassCommand(final Player player, final String bypass) {
        if (bypass.startsWith(CbPersonalCabinet.BBSBYPASSES[0])) {
            final String html = HtmCache.getInstance().getNotNull("scripts/services/community/percab/pass.htm", player);
            ShowBoard.separateAndSend(html, player);
            onRequestChangePassword(player, bypass.substring(CbPersonalCabinet.BBSBYPASSES[0].length()));
        } else if (bypass.startsWith(CbPersonalCabinet.BBSBYPASSES[1])) {
            String html = HtmCache.getInstance().getNotNull("scripts/services/community/percab/repair.htm", player);
            final String login = player.getAccountName();
            final StringBuilder sb = new StringBuilder();
            final int repairReqObjId = getRepairRequestCharObjId(bypass.substring(CbPersonalCabinet.BBSBYPASSES[1].length()));
            int repairObjId = -1;
            String repairCharName = null;
            final List<Pair<Integer, String>> accountCharacters = getCharactersOfAccount(login);
            int cnt = 1;
            for (final Pair<Integer, String> e : accountCharacters) {
                if (e.getLeft() != player.getObjectId()) {
                    if (World.getPlayer(e.getLeft()) != null) {
                        continue;
                    }
                    if (cnt % 2 != 0) {
                        sb.append("<tr><td ALIGN=\"left\"><table height=20 WIDTH=216><tr><td height=20 WIDTH=20 ALIGN=\"center\">");
                    } else {
                        sb.append("<tr><td ALIGN=\"left\"><table height=20 WIDTH=216><tr><td height=20 WIDTH=20 ALIGN=\"center\">");
                    }
                    sb.append("<IMG HEIGHT=32 WIDTH=32 SRC=\"L2UI_CH3.calculate1_").append(cnt);
                    sb.append("\" WIDTH=15></td><td height=20 WIDTH=200 ALIGN=\"left\"><a action=\"bypass -h _bbscrepair ");
                    sb.append(e.getLeft()).append("\" msg=\"Are you really want to repair ");
                    sb.append(e.getRight()).append("?\">").append(e.getRight()).append("</a>");
                    sb.append("</td></tr></table></td></tr>\n");
                    ++cnt;
                    if (e.getLeft() != repairReqObjId) {
                        continue;
                    }
                    repairObjId = e.getLeft();
                    repairCharName = e.getRight();
                }
            }
            html = html.replace("<%repair_char_list_tbl%>", sb.toString());
            ShowBoard.separateAndSend(html, player);
            if (repairObjId > 0) {
                if (World.getPlayer(repairObjId) == null) {
                    repairCharacter(repairObjId);
                    player.sendMessage("Character " + repairCharName + " repaired.");
                } else {
                    player.sendMessage("Character online.");
                }
            }
        } else if (bypass.startsWith(CbPersonalCabinet.BBSBYPASSES[2])) {
            String html = HtmCache.getInstance().getNotNull("scripts/services/community/percab/stats.htm", player);
            html = makeWhiIAmHtml(player, html);
            ShowBoard.separateAndSend(html, player);
        }
    }

    @Override
    public void onWriteCommand(final Player player, final String bypass, final String arg1, final String arg2, final String arg3, final String arg4, final String arg5) {
    }

    @Override
    public void onInit() {
        CommunityBoardManager.getInstance().registerHandler(this);
    }

}
