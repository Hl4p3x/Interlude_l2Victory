package services.villagemasters;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.scripts.Functions;

public class Ally extends Functions {
    public void CheckCreateAlly() {
        if (getNpc() == null || getSelf() == null) {
            return;
        }
        final Player pl = getSelf();
        String htmltext = "ally-01.htm";
        if (pl.isClanLeader()) {
            htmltext = "ally-02.htm";
        }
        getNpc().showChatWindow(pl, "villagemaster/" + htmltext);
    }

    public void CheckDissolveAlly() {
        if (getNpc() == null || getSelf() == null) {
            return;
        }
        final Player pl = getSelf();
        String htmltext = "ally-01.htm";
        if (pl.isAllyLeader()) {
            htmltext = "ally-03.htm";
        }
        getNpc().showChatWindow(pl, "villagemaster/" + htmltext);
    }
}
