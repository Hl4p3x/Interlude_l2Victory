package ru.j2dev.dataparser.annotations.factory;
/**
 * @author : Camelion
 * @date : 24.08.12  22:57
 */

/**
 * Реализация фабрики по умолчанию (всегда используется тип аннотированного
 * поля)
 */
public class DefaultFactory implements IObjectFactory<Object> {
    private Class<?> clazz;

    /**
     * Устанавливает класс, из которого будут создаваться объекты. Данный метод
     * гарантировано вызывается перед createObjectFor, если используется
     * NoneFactory
     *
     * @param clazz
     */
    @Override
    public void setFieldClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object createObjectFor(StringBuilder data) throws IllegalAccessException, InstantiationException {
        return clazz.newInstance();
    }
}