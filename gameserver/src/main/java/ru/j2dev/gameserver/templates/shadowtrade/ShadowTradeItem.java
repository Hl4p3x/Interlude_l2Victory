package ru.j2dev.gameserver.templates.shadowtrade;

/**
 * Created by JunkyFunky
 * on 18.01.2018 21:57
 * group j2dev
 */
public class ShadowTradeItem {

    private int itemId;
    private int count;
    private int priceItemId;
    private int priceCount;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPriceCount() {
        return priceCount;
    }

    public void setPriceCount(int priceCount) {
        this.priceCount = priceCount;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getPriceItemId() {
        return priceItemId;
    }

    public void setPriceItemId(int priceItemId) {
        this.priceItemId = priceItemId;
    }
}
