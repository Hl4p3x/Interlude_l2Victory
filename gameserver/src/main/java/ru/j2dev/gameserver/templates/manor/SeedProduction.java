package ru.j2dev.gameserver.templates.manor;

public class SeedProduction {
    final int _seedId;
    final long _price;
    long _residual;
    long _sales;

    public SeedProduction(final int id) {
        _seedId = id;
        _sales = 0L;
        _price = 0L;
        _sales = 0L;
    }

    public SeedProduction(final int id, final long amount, final long price, final long sales) {
        _seedId = id;
        _residual = amount;
        _price = price;
        _sales = sales;
    }

    public int getId() {
        return _seedId;
    }

    public long getCanProduce() {
        return _residual;
    }

    public void setCanProduce(final long amount) {
        _residual = amount;
    }

    public long getPrice() {
        return _price;
    }

    public long getStartProduce() {
        return _sales;
    }
}
