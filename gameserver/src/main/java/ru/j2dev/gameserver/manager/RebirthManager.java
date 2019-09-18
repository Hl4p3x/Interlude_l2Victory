package ru.j2dev.gameserver.manager;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.SubClass;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.base.PlayerClass;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.utils.HtmlUtils;

import java.util.Collections;
import java.util.Set;

/**
 * Created by JunkyFunky
 * on 17.02.2018 11:18
 * group j2dev
 */
@HideAccess
@StringEncryption
public class RebirthManager {

    /**
     * Генерирует и возвращает NpcHtmlMessage в котором список всех доступных професский для заемены
     *
     * @param player
     * @return
     */
    public NpcHtmlMessage generateAvailRebirthHtml(final Player player) {
        Set<PlayerClass> availSubs = getAvailableSubClasses(player);
        NpcHtmlMessage rebirthSetHtml = new NpcHtmlMessage(0);
        rebirthSetHtml.setFile("rebirth_manager/rebirth.htm");
        StringBuilder sb = new StringBuilder();
        for (PlayerClass playerClass : availSubs) {
            sb.append(HtmlUtils.makeClassNameFString(player, playerClass.ordinal()));
        }
        rebirthSetHtml.replace("%rebitth_list%", sb.toString());
        return rebirthSetHtml;
    }

    /**
     * Получение списка доступных сабклассов
     *
     * @param player
     * @return Set<PlayerClass>
     */
    private Set<PlayerClass> getAvailableSubClasses(final Player player) {
        final int charClassId = player.getBaseClassId();
        final PlayerClass currClass = PlayerClass.values()[charClassId];
        final Set<PlayerClass> availSubs = currClass.getAvailableSubclasses();
        if (availSubs == null) {
            return Collections.emptySet();
        }
        availSubs.remove(currClass);
        availSubs.forEach(availSub -> {
            for (final SubClass subClass : player.getSubClasses().values()) {
                if (availSub.ordinal() == subClass.getClassId()) {
                    availSubs.remove(availSub);
                } else {
                    final ClassId parent = ClassId.VALUES[availSub.ordinal()].getParent(player.getSex());
                    if (parent != null && parent.getId() == subClass.getClassId()) {
                        availSubs.remove(availSub);
                    } else {
                        final ClassId subParent = ClassId.VALUES[subClass.getClassId()].getParent(player.getSex());
                        if (subParent == null || subParent.getId() != availSub.ordinal()) {
                            return;
                        }
                        availSubs.remove(availSub);
                    }
                }
            }
        });
        return availSubs;
    }

    private static class LazyHolder {
        private static final RebirthManager INSTANCE = new RebirthManager();
    }
}