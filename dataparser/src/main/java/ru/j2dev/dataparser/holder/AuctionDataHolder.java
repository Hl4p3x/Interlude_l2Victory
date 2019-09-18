package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.auctiondata.Auction;

import java.util.List;

/**
 * @author : Camelion
 * @date : 25.08.12 18:50
 */
public class AuctionDataHolder extends AbstractHolder {
    private static final AuctionDataHolder ourInstance = new AuctionDataHolder();
    @Element(start = "auction_begin", end = "auction_end")
    private List<Auction> auctions;

    private AuctionDataHolder() {
    }

    public static AuctionDataHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return auctions.size();
    }

    public List<Auction> getAuctions() {
        return auctions;
    }

    @Override
    public void clear() {
    }
}