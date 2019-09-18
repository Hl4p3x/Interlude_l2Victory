package ru.j2dev.commons.dao;

public interface JdbcEntityStats {
    long getLoadCount();

    long getInsertCount();

    long getUpdateCount();

    long getDeleteCount();
}
