package ru.j2dev.commons.dao;

import java.io.Serializable;

public interface JdbcDAO<K extends Serializable, E extends JdbcEntity> {
    E load(final K p0);

    void save(final E p0);

    void update(final E p0);

    void saveOrUpdate(final E p0);

    void delete(final E p0);

    JdbcEntityStats getStats();
}
