package intelink.utils;

public class EnumUtils {
    public static <T extends Enum<T>> T fromString(Class<T> enumClass, String value) {
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid enum value -> Class: " + enumClass.getName() + " - Value: " + value);
        }
    }
}
