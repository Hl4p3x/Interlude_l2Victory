package ru.j2dev.gameserver.utils;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.net.nio.impl.SelectorThread;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.FStringHolder;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.templates.FStringTemplate;

import java.net.InetAddress;
import java.util.Arrays;

/**
 * Created by JunkyFunky
 * on 03.02.2018 22:39
 * group j2dev
 */
public class PtsUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PtsUtils.class);
    private static final FStringHolder holder = FStringHolder.getInstance();
    @HideAccess
    @StringEncryption
    private static final String[] announceToAll = {
            "Данный мудак скорее всего спиздил приват сборку",
            "Но этот имбицил еще не осознает что стоит привязка к 127",
            "И как только онлайн перевалит за 10 человек, начнется трэш:D"
    };

    @HideAccess
    @StringEncryption
    public static int[] VALUES = {
            /*
             * j2dev clients
             * */
            //LocalHost
            4706979
    };

    public static String[] getAnnounceToAll() {
        return announceToAll;
    }

    @HideAccess
    @StringEncryption
    public static void checkLicense() {
        checked:
        {
            try {
                final InetAddress[] allByName = InetAddress.getAllByName(String.valueOf(Config.class.getDeclaredField("EXTERNAL_HOSTNAME").get(null)));
                int j = 0;
                while (j < allByName.length) {
                    final InetAddress addr = allByName[j];
                    check_ip:
                    {
                        for (final int a : VALUES) {
                            if (a == Arrays.hashCode(addr.getAddress())) {
                                break check_ip;
                            }
                        }
                        j++;
                        continue;
                    }
                    break checked;
                }
                checkPlayersCount();
            } catch (Exception e) {
                Announcements.getInstance().announceToAll(announceToAll);
                SelectorThread.MAX_CONNECTIONS = 10L;
                System.exit(0);
            }
        }

    }

    @HideAccess
    @StringEncryption
    private static void checkPlayersCount() {
        Announcements.getInstance().announceToAll(announceToAll);
        if (GameObjectsStorage.getPlayers(player -> !player.isPhantom()).size() > 10) {
            SelectorThread.MAX_CONNECTIONS = 10L;
        }
    }

    /**
     * - Вынимает текст из хмл fstring.xml, заменяет синтаксисы на параметры если имеются
     *
     * @param id     - айди текста
     * @param params - массив параметров
     *               <p>
     * @return - возвращает обработанный текст
     */
    @HideAccess
    @StringEncryption
    public static String MakeFString(final Player player, final int id, final String... params) {
        final FStringTemplate fstring = holder.getTemplate(id);
        if (fstring == null) {
            LOGGER.warn("WARNING: No FString from xml " + id + "For Player :" + player.toString() + " Npc : " + player.getLastNpc());
            return "No Fstring " + id;
        }
        String str = player.isLangRus() ? fstring.getRu() : fstring.getEn();
        if (str.contains("%1")) {
            if (params.length >= 1 && params[0] != null && !params[0].isEmpty()) {
                str = str.replaceFirst("%1", params[0]);
            }

            if (params.length >= 2 && params[1] != null && !params[1].isEmpty()) {
                str = str.replaceFirst("%2", params[1]);
            }

            if (params.length >= 3 && params[2] != null && !params[2].isEmpty()) {
                str = str.replaceFirst("%3", params[2]);
            }

            if (params.length >= 4 && params[3] != null && !params[3].isEmpty()) {
                str = str.replaceFirst("%4", params[3]);
            }

            if (params.length >= 5 && params[4] != null && !params[4].isEmpty()) {
                str = str.replaceFirst("%5", params[4]);
            }
        }
        return str;
    }
}
