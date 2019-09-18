package ru.j2dev.gameserver.model.event;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;

@HideAccess
@StringEncryption
public class PlayerEventStatus {
    public Player player;
    public List<Player> kills = new ArrayList<>();
    public boolean eventSitForced;
    private Location initLoc;
    private int initReflectionId;
    private int initKarma;
    private int initPvpKills;
    private int initPkKills;
    private String initTitle;

    public PlayerEventStatus(Player player) {
        this.player = player;
        initLoc = new Location(player.getX(), player.getY(), player.getZ(), player.getHeading());
        initReflectionId = player.getReflectionId();
        initKarma = player.getKarma();
        initPvpKills = player.getPvpKills();
        initPkKills = player.getPkKills();
        initTitle = player.getTitle();

    }

    public void restoreInits() {
        player.teleToLocation(initLoc, initReflectionId);
        player.setKarma(initKarma);
        player.setPvpKills(initPvpKills);
        player.setPkKills(initPkKills);
        player.setTitle(initTitle);
    }
}
