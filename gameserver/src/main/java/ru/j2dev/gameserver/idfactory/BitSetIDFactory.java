package ru.j2dev.gameserver.idfactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.math.PrimeFinder;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

public class BitSetIDFactory extends IdFactory {
    private static Logger LOGGER = LoggerFactory.getLogger(BitSetIDFactory.class);

    private BitSet _freeIds;
    private AtomicInteger _freeIdCount;
    private AtomicInteger _nextFreeId;

    protected class BitSetCapacityCheck extends RunnableImpl {
        @Override
        public void runImpl() {
            if (reachingBitSetCapacity())
                increaseBitSetCapacity();
        }
    }

    protected BitSetIDFactory() {
        super();
        initialize();

        ThreadPoolManager.getInstance().scheduleAtFixedRate(new BitSetCapacityCheck(), 30000, 30000);
    }

    private void initialize() {
        try {
            _freeIds = new BitSet(PrimeFinder.nextPrime(100000));
            _freeIds.clear();
            _freeIdCount = new AtomicInteger(FREE_OBJECT_ID_SIZE);

            for (int usedObjectId : extractUsedObjectIDTable()) {
                int objectID = usedObjectId - FIRST_OID;
                if (objectID < 0) {
                    LOGGER.warn("Object ID " + usedObjectId + " in DB is less than minimum ID of " + FIRST_OID);
                    continue;
                }
                _freeIds.set(usedObjectId - FIRST_OID);
                _freeIdCount.decrementAndGet();
            }

            _nextFreeId = new AtomicInteger(_freeIds.nextClearBit(0));
            _initialized = true;

            LOGGER.info("IDFactory: " + _freeIds.size() + " id's available.");
        } catch (Exception e) {
            _initialized = false;
            LOGGER.error("BitSet ID Factory could not be initialized correctly: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized void releaseId(int objectID) {
        if ((objectID - FIRST_OID) > -1) {
            _freeIds.clear(objectID - FIRST_OID);
            _freeIdCount.incrementAndGet();
        } else
            LOGGER.warn("BitSet ID Factory: release objectID " + objectID + " failed (< " + FIRST_OID + ")");
    }

    @Override
    public synchronized int getNextId() {
        int newID = _nextFreeId.get();
        _freeIds.set(newID);
        _freeIdCount.decrementAndGet();

        int nextFree = _freeIds.nextClearBit(newID);

        if (nextFree < 0)
            nextFree = _freeIds.nextClearBit(0);

        if (nextFree < 0) {
            if (_freeIds.size() < FREE_OBJECT_ID_SIZE)
                increaseBitSetCapacity();
            else
                throw new NullPointerException("Ran out of valid Id's.");
        }

        _nextFreeId.set(nextFree);

        return newID + FIRST_OID;
    }

    @Override
    public synchronized int size() {
        return _freeIdCount.get();
    }

    protected synchronized int usedIdCount() {
        return (size() - FIRST_OID);
    }

    protected synchronized boolean reachingBitSetCapacity() {
        return PrimeFinder.nextPrime(usedIdCount() * 11 / 10) > _freeIds.size();
    }

    protected synchronized void increaseBitSetCapacity() {
        BitSet newBitSet = new BitSet(PrimeFinder.nextPrime(usedIdCount() * 11 / 10));
        newBitSet.or(_freeIds);
        _freeIds = newBitSet;
    }
}