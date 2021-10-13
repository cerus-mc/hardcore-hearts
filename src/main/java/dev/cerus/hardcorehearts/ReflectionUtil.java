package dev.cerus.hardcorehearts;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import sun.misc.Unsafe;

public class ReflectionUtil {

    private ReflectionUtil() {
    }

    /**
     * Changes the value of a "private final" field. Should work in all Java versions up to Java 16.
     *
     * @param field    The field that you want to overwrite
     * @param instance The object instance
     * @param value    The new value
     */
    public static void setPrivateFinalField(final Field field, final Object instance, final Object value) throws IllegalAccessException, NoSuchFieldException {
        field.setAccessible(true);

        try {
            // Works for everything below Java 12
            final Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(instance, value);
        } catch (final NoSuchFieldException ignored) {
            // Works for Java 12 - 16
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            final Unsafe unsafe = (Unsafe) unsafeField.get(null);
            final long off = unsafe.objectFieldOffset(field);
            unsafe.putObject(instance, off, value);
        }
    }

}
