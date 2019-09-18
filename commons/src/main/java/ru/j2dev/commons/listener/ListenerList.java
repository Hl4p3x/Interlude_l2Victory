package ru.j2dev.commons.listener;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * Класс реализующий список слушателей для каждого типа интерфейса.
 *
 * @param <T> базовый интерфейс слушателя
 * @author G1ta0
 */
@SuppressWarnings("unchecked")
public class ListenerList<T> {
    private final Collection<Listener<T>> listeners = new CopyOnWriteArraySet<>();

    public Collection<Listener<T>> getListeners() {
        return Collections.unmodifiableCollection(listeners);
    }

    public <L> Collection<L> getListeners(final Class<L> clazz) {
        return listeners.stream().filter(clazz::isInstance).map(listener -> (L) listener).collect(Collectors.toList());
    }

    /**
     * Добавить слушатель в список
     *
     * @param listener
     * @return возвращает true, если слушатель был добавлен
     */
    public boolean add(final Listener<T> listener) {
        return listeners.add(listener);
    }

    /**
     * Удалить слушатель из списока
     *
     * @param listener
     * @return возвращает true, если слушатель был удален
     */
    public boolean remove(final Listener<T> listener) {
        return listeners.remove(listener);
    }
}
