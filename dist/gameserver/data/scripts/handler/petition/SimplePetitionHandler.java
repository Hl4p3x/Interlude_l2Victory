package handler.petition;

import ru.j2dev.gameserver.handler.petition.IPetitionHandler;
import ru.j2dev.gameserver.model.Player;

public class SimplePetitionHandler implements IPetitionHandler {
    @Override
    public void handle(final Player player, final int id, final String txt) {
        player.sendMessage(txt);
    }
}
