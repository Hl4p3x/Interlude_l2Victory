package ru.j2dev.gameserver.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.handler.petition.IPetitionHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Say2;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.tables.GmListTable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class PetitionManager implements IPetitionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetitionManager.class.getName());

    private final AtomicInteger _nextId;
    private final Map<Integer, Petition> _pendingPetitions;
    private final Map<Integer, Petition> _completedPetitions;

    private PetitionManager() {
        _nextId = new AtomicInteger();
        _pendingPetitions = new ConcurrentHashMap<>();
        _completedPetitions = new ConcurrentHashMap<>();
        LOGGER.info("Initializing PetitionManager");
    }

    public static PetitionManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public int getNextId() {
        return _nextId.incrementAndGet();
    }

    public void clearCompletedPetitions() {
        final int numPetitions = getPendingPetitionCount();
        getCompletedPetitions().clear();
        LOGGER.info("PetitionManager: Completed petition data cleared. " + numPetitions + " petition(s) removed.");
    }

    public void clearPendingPetitions() {
        final int numPetitions = getPendingPetitionCount();
        getPendingPetitions().clear();
        LOGGER.info("PetitionManager: Pending petition queue cleared. " + numPetitions + " petition(s) removed.");
    }

    public boolean acceptPetition(final Player respondingAdmin, final int petitionId) {
        if (!isValidPetition(petitionId)) {
            return false;
        }
        final Petition currPetition = getPendingPetitions().get(petitionId);
        if (currPetition.getResponder() != null) {
            return false;
        }
        currPetition.setResponder(respondingAdmin);
        currPetition.setState(PetitionState.In_Process);
        currPetition.sendPetitionerPacket(new SystemMessage(406));
        currPetition.sendResponderPacket(new SystemMessage(389).addNumber(currPetition.getId()));
        currPetition.sendResponderPacket(new SystemMessage(394).addString(currPetition.getPetitioner().getName()));
        return true;
    }

    public boolean cancelActivePetition(final Player player) {
        for (final Petition currPetition : getPendingPetitions().values()) {
            if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId()) {
                return currPetition.endPetitionConsultation(PetitionState.Petitioner_Cancel);
            }
            if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId()) {
                return currPetition.endPetitionConsultation(PetitionState.Responder_Cancel);
            }
        }
        return false;
    }

    public void checkPetitionMessages(final Player petitioner) {
        if (petitioner != null) {
            for (final Petition currPetition : getPendingPetitions().values()) {
                if (currPetition == null) {
                    continue;
                }
                if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == petitioner.getObjectId()) {
                    for (final Say2 logMessage : currPetition.getLogMessages()) {
                        petitioner.sendPacket(logMessage);
                    }
                }
            }
        }
    }

    public boolean endActivePetition(final Player player) {
        if (!player.isGM()) {
            return false;
        }
        for (final Petition currPetition : getPendingPetitions().values()) {
            if (currPetition == null) {
                continue;
            }
            if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId()) {
                return currPetition.endPetitionConsultation(PetitionState.Completed);
            }
        }
        return false;
    }

    protected Map<Integer, Petition> getCompletedPetitions() {
        return _completedPetitions;
    }

    protected Map<Integer, Petition> getPendingPetitions() {
        return _pendingPetitions;
    }

    public int getPendingPetitionCount() {
        return getPendingPetitions().size();
    }

    public int getPlayerTotalPetitionCount(final Player player) {
        if (player == null) {
            return 0;
        }
        int petitionCount = 0;
        for (final Petition currPetition : getPendingPetitions().values()) {
            if (currPetition == null) {
                continue;
            }
            if (currPetition.getPetitioner() == null || currPetition.getPetitioner().getObjectId() != player.getObjectId()) {
                continue;
            }
            ++petitionCount;
        }
        for (final Petition currPetition : getCompletedPetitions().values()) {
            if (currPetition == null) {
                continue;
            }
            if (currPetition.getPetitioner() == null || currPetition.getPetitioner().getObjectId() != player.getObjectId()) {
                continue;
            }
            ++petitionCount;
        }
        return petitionCount;
    }

    public boolean isPetitionPending() {
        for (final Petition currPetition : getPendingPetitions().values()) {
            if (currPetition == null) {
                continue;
            }
            if (currPetition.getState() == PetitionState.Pending) {
                return true;
            }
        }
        return false;
    }

    public boolean isPetitionInProcess() {
        for (final Petition currPetition : getPendingPetitions().values()) {
            if (currPetition == null) {
                continue;
            }
            if (currPetition.getState() == PetitionState.In_Process) {
                return true;
            }
        }
        return false;
    }

    public boolean isPetitionInProcess(final int petitionId) {
        if (!isValidPetition(petitionId)) {
            return false;
        }
        final Petition currPetition = getPendingPetitions().get(petitionId);
        return currPetition.getState() == PetitionState.In_Process;
    }

    public boolean isPlayerInConsultation(final Player player) {
        if (player != null) {
            for (final Petition currPetition : getPendingPetitions().values()) {
                if (currPetition == null) {
                    continue;
                }
                if (currPetition.getState() != PetitionState.In_Process) {
                    continue;
                }
                if ((currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId()) || (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPetitioningAllowed() {
        return Config.PETITIONING_ALLOWED;
    }

    public boolean isPlayerPetitionPending(final Player petitioner) {
        if (petitioner != null) {
            for (final Petition currPetition : getPendingPetitions().values()) {
                if (currPetition == null) {
                    continue;
                }
                if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == petitioner.getObjectId()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidPetition(final int petitionId) {
        return getPendingPetitions().containsKey(petitionId);
    }

    public boolean rejectPetition(final Player respondingAdmin, final int petitionId) {
        if (!isValidPetition(petitionId)) {
            return false;
        }
        final Petition currPetition = getPendingPetitions().get(petitionId);
        if (currPetition.getResponder() != null) {
            return false;
        }
        currPetition.setResponder(respondingAdmin);
        return currPetition.endPetitionConsultation(PetitionState.Responder_Reject);
    }

    public boolean sendActivePetitionMessage(final Player player, final String messageText) {
        for (final Petition currPetition : getPendingPetitions().values()) {
            if (currPetition == null) {
                continue;
            }
            if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId()) {
                final Say2 cs = new Say2(player.getObjectId(), ChatType.PETITION_PLAYER, player.getName(), messageText);
                currPetition.addLogMessage(cs);
                currPetition.sendResponderPacket(cs);
                currPetition.sendPetitionerPacket(cs);
                return true;
            }
            if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId()) {
                final Say2 cs = new Say2(player.getObjectId(), ChatType.PETITION_GM, player.getName(), messageText);
                currPetition.addLogMessage(cs);
                currPetition.sendResponderPacket(cs);
                currPetition.sendPetitionerPacket(cs);
                return true;
            }
        }
        return false;
    }

    public void sendPendingPetitionList(final Player activeChar) {
        final StringBuilder htmlContent = new StringBuilder(600 + getPendingPetitionCount() * 300);
        htmlContent.append("<html><body><center><table width=270><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td width=180><center>Petition Menu</center></td><td width=45><button value=\"Back\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr></table><br><table width=\"270\"><tr><td><table width=\"270\"><tr><td><button value=\"Reset\" action=\"bypass -h admin_reset_petitions\" width=\"80\" height=\"21\" back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td align=right><button value=\"Refresh\" action=\"bypass -h admin_view_petitions\" width=\"80\" height=\"21\" back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr></table><br></td></tr>");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (getPendingPetitionCount() == 0) {
            htmlContent.append("<tr><td>There are no currently pending petitions.</td></tr>");
        } else {
            htmlContent.append("<tr><td><font color=\"LEVEL\">Current Petitions:</font><br></td></tr>");
        }
        boolean color = true;
        int petcount = 0;
        for (final Petition currPetition : getPendingPetitions().values()) {
            if (currPetition == null) {
                continue;
            }
            htmlContent.append("<tr><td width=\"270\"><table width=\"270\" cellpadding=\"2\" bgcolor=").append(color ? "131210" : "444444").append("><tr><td width=\"130\">").append(dateFormat.format(new Date(currPetition.getSubmitTime())));
            htmlContent.append("</td><td width=\"140\" align=right><font color=\"").append(currPetition.getPetitioner().isOnline() ? "00FF00" : "999999").append("\">").append(currPetition.getPetitioner().getName()).append("</font></td></tr>");
            htmlContent.append("<tr><td width=\"130\">");
            if (currPetition.getState() != PetitionState.In_Process) {
                htmlContent.append("<table width=\"130\" cellpadding=\"2\"><tr><td><button value=\"View\" action=\"bypass -h admin_view_petition ").append(currPetition.getId()).append("\" width=\"50\" height=\"21\" back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td><button value=\"Reject\" action=\"bypass -h admin_reject_petition ").append(currPetition.getId()).append("\" width=\"50\" height=\"21\" back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr></table>");
            } else {
                htmlContent.append("<font color=\"").append(currPetition.getResponder().isOnline() ? "00FF00" : "999999").append("\">").append(currPetition.getResponder().getName()).append("</font>");
            }
            htmlContent.append("</td>").append(currPetition.getTypeAsString()).append("<td width=\"140\" align=right>").append(currPetition.getTypeAsString()).append("</td></tr></table></td></tr>");
            color = !color;
            if (++petcount > 10) {
                htmlContent.append("<tr><td><font color=\"LEVEL\">There is more pending petition...</font><br></td></tr>");
                break;
            }
        }
        htmlContent.append("</table></center></body></html>");
        final NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
        htmlMsg.setHtml(htmlContent.toString());
        activeChar.sendPacket(htmlMsg);
    }

    public int submitPetition(final Player petitioner, final String petitionText, final int petitionType) {
        final Petition newPetition = new Petition(petitioner, petitionText, petitionType);
        final int newPetitionId = newPetition.getId();
        getPendingPetitions().put(newPetitionId, newPetition);
        final String msgContent = petitioner.getName() + " has submitted a new petition.";
        GmListTable.broadcastToGMs(new Say2(petitioner.getObjectId(), ChatType.CRITICAL_ANNOUNCE, "Petition System", msgContent));
        return newPetitionId;
    }

    public void viewPetition(final Player activeChar, final int petitionId) {
        if (!activeChar.isGM()) {
            return;
        }
        if (!isValidPetition(petitionId)) {
            return;
        }
        final Petition currPetition = getPendingPetitions().get(petitionId);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("admin/petition.htm");
        html.replace("%petition%", String.valueOf(currPetition.getId()));
        html.replace("%time%", dateFormat.format(new Date(currPetition.getSubmitTime())));
        html.replace("%type%", currPetition.getTypeAsString());
        html.replace("%petitioner%", currPetition.getPetitioner().getName());
        html.replace("%online%", currPetition.getPetitioner().isOnline() ? "00FF00" : "999999");
        html.replace("%text%", currPetition.getContent());
        activeChar.sendPacket(html);
    }

    @Override
    public void handle(final Player player, final int typeId, final String txt) {
        if (!Config.CAN_PETITION_TO_OFFLINE_GM && GmListTable.getAllGMs().size() == 0) {
            player.sendPacket(new SystemMessage(702));
            return;
        }
        if (!getInstance().isPetitioningAllowed()) {
            player.sendPacket(new SystemMessage(381));
            return;
        }
        if (getInstance().isPlayerPetitionPending(player)) {
            player.sendPacket(new SystemMessage(390));
            return;
        }
        if (getInstance().getPendingPetitionCount() == Config.MAX_PETITIONS_PENDING) {
            player.sendPacket(new SystemMessage(602));
            return;
        }
        final int totalPetitions = getInstance().getPlayerTotalPetitionCount(player) + 1;
        if (totalPetitions > Config.MAX_PETITIONS_PER_PLAYER) {
            player.sendPacket(new SystemMessage(733));
            return;
        }
        if (txt.length() > 255) {
            player.sendPacket(new SystemMessage(971));
            return;
        }
        if (typeId >= PetitionType.values().length) {
            LOGGER.warn("PetitionManager: Invalid petition type : " + typeId);
            return;
        }
        final int petitionId = getInstance().submitPetition(player, txt, typeId);
        player.sendPacket(new SystemMessage(389).addNumber(petitionId));
        player.sendPacket(new SystemMessage(730).addNumber(totalPetitions).addNumber(Config.MAX_PETITIONS_PER_PLAYER - totalPetitions));
        player.sendPacket(new SystemMessage(601).addNumber(getInstance().getPendingPetitionCount()));
    }

    public enum PetitionState {
        Pending,
        Responder_Cancel,
        Responder_Missing,
        Responder_Reject,
        Responder_Complete,
        Petitioner_Cancel,
        Petitioner_Missing,
        In_Process,
        Completed
    }

    public enum PetitionType {
        Immobility,
        Recovery_Related,
        Bug_Report,
        Quest_Related,
        Bad_User,
        Suggestions,
        Game_Tip,
        Operation_Related,
        Other
    }

    private static class LazyHolder {
        private static final PetitionManager INSTANCE = new PetitionManager();
    }

    private class Petition {
        private final long _submitTime;
        private final int _id;
        private final PetitionType _type;
        private final String _content;
        private final List<Say2> _messageLog;
        private final int _petitioner;
        private long _endTime;
        private PetitionState _state;
        private int _responder;

        public Petition(final Player petitioner, final String petitionText, final int petitionType) {
            _submitTime = System.currentTimeMillis();
            _endTime = -1L;
            _state = PetitionState.Pending;
            _messageLog = new ArrayList<>();
            _id = getNextId();
            _type = PetitionType.values()[petitionType - 1];
            _content = petitionText;
            _petitioner = petitioner.getObjectId();
        }

        protected boolean addLogMessage(final Say2 cs) {
            return _messageLog.add(cs);
        }

        protected List<Say2> getLogMessages() {
            return _messageLog;
        }

        public boolean endPetitionConsultation(final PetitionState endState) {
            setState(endState);
            _endTime = System.currentTimeMillis();
            if (getResponder() != null && getResponder().isOnline()) {
                if (endState == PetitionState.Responder_Reject) {
                    getPetitioner().sendMessage("Your petition was rejected. Please try again later.");
                } else {
                    getResponder().sendPacket(new SystemMessage(395).addString(getPetitioner().getName()));
                    if (endState == PetitionState.Petitioner_Cancel) {
                        getResponder().sendPacket(new SystemMessage(391).addNumber(getId()));
                    }
                }
            }
            if (getPetitioner() != null && getPetitioner().isOnline()) {
                getPetitioner().sendPacket(new SystemMessage(387));
            }
            getCompletedPetitions().put(getId(), this);
            return getPendingPetitions().remove(getId()) != null;
        }

        public String getContent() {
            return _content;
        }

        public int getId() {
            return _id;
        }

        public Player getPetitioner() {
            return World.getPlayer(_petitioner);
        }

        public Player getResponder() {
            return World.getPlayer(_responder);
        }

        public void setResponder(final Player responder) {
            if (getResponder() != null) {
                return;
            }
            _responder = responder.getObjectId();
        }

        public long getEndTime() {
            return _endTime;
        }

        public long getSubmitTime() {
            return _submitTime;
        }

        public PetitionState getState() {
            return _state;
        }

        public void setState(final PetitionState state) {
            _state = state;
        }

        public String getTypeAsString() {
            return _type.toString().replace("_", " ");
        }

        public void sendPetitionerPacket(final L2GameServerPacket responsePacket) {
            if (getPetitioner() == null || !getPetitioner().isOnline()) {
                return;
            }
            getPetitioner().sendPacket(responsePacket);
        }

        public void sendResponderPacket(final L2GameServerPacket responsePacket) {
            if (getResponder() == null || !getResponder().isOnline()) {
                endPetitionConsultation(PetitionState.Responder_Missing);
                return;
            }
            getResponder().sendPacket(responsePacket);
        }
    }
}
