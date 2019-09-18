package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExEventMatchMessage extends L2GameServerPacket {
    private final MessageType _type;
    private final String _message;

    /**
     * Create an event match message.
     *
     * @param type    0 - gm, 1 - finish, 2 - start, 3 - game over, 4 - 1, 5 - 2, 6 - 3, 7 - 4, 8 - 5
     * @param message message to show, only when type is 0 - gm
     */
    public ExEventMatchMessage(MessageType type, String message) {
        _type = type;
        _message = message;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x4);
        writeC(_type.ordinal());
        writeS(_message);
    }

    /**
     * Виды специальных эвентовых сообщений
     */
    public enum MessageType {
        TEXT, // Произвольный текст
        FINISH, // Большая надпись "FINISH" посреди экрана
        START, // Большая надпись "START" посреди экрана
        GAME_OVER, // Большая надпись "GAME OVER" посреди экрана
        NUMBER_1, // Большая цифра "1" посреди экрана
        NUMBER_2, // Большая цифра "2" посреди экрана
        NUMBER_3, // Большая цифра "3" посреди экрана
        NUMBER_4, // Большая цифра "4" посреди экрана
        NUMBER_5 // Большая цифра "5" посреди экрана
    }
}
