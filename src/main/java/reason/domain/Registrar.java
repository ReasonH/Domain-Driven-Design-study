package reason.domain;

import java.util.Collection;

public interface Registrar {
    void init();

    void add(Class<?> entryPointClass, EntryPoint newObject);

    EntryPoint get(Class<?> entryPointClass, String objectName);

    Collection<? extends EntryPoint> getAll(Class<?> entryPointClass);

    EntryPoint delete(Class<?> entryPointClass, String objectName);
}