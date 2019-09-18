package ru.j2dev.gameserver.templates;

/**
 * @author PaInKiLlEr
 */
public class FStringTemplate {

    private final int _id;
    private final String _en;
    private final String _ru;

    public FStringTemplate(final StatsSet set) {
        _id = set.getInteger("id");
        _en = set.getString("en");
        _ru = set.getString("ru");
    }

    public final int getId() {
        return _id;
    }

    public final String getEn() {
        return _en;
    }

    public final String getRu() {
        return _ru;
    }
}
