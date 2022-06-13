package com.kihz.utils;

import com.kihz.Constants;
import com.kihz.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtil {
    private static final Map<Class<?>, Class<?>> PRIMITIVES = new HashMap<>();
    private static final Map<Class<?>, Class<?>> REPLACE = new HashMap<>();
    private static final Map<String, Class<?>> classCacheMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Field>> fieldCacheMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Constructor<?>> emptyConstructorCache = new ConcurrentHashMap<>();

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    public static final String NMS_VERSION = "v1_16_R3";

    private static final MethodHandles.Lookup METHOD_LOOKUPS = MethodHandles.lookup();

    private static final String OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NMS_PREFIX = OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    /**
     * Execute a method of a given class and object
     *
     * @param obj        The object to execute the method on.
     * @param type       The class which owns the method.
     * @param methodName The method name to run.
     * @param args       The arguments to run the method with.
     * @return result
     */
    public static Object exec(Object obj, Class<?> type, String methodName, Object... args) {
        Class<?>[] argTypes = getClasses(true, args); // Generate a list of the classes used to Liget the method.

        try {
            Method m = type.getDeclaredMethod(methodName, argTypes);
            m.setAccessible(true);
            return m.invoke(obj, args);
        } catch (Exception e) {
            e.printStackTrace();
            Core.logInfo("Failed to execute reflected method %s!", methodName);
        }
        return null;
    }

    /**
     * Execute a method of a given class.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static Object exec(Object obj, String methodName, Object... args) {
        if (obj instanceof Class<?>) { // Static.
            return exec(null, (Class<?>) obj, methodName, args);
        } else {
            return exec(obj, obj.getClass(), methodName, args);
        }
    }

    /**
     * Construct a class with no arguments.
     *
     * @param clazz The class to create an instance of.
     * @return object
     */
    @SuppressWarnings("unchecked")
    public static <T> T construct(Class<T> clazz) {
        Utils.verifyNotNull(clazz, "Tried to construct null class.");

        try {
            Constructor<T> emptyConstructor = (Constructor<T>) emptyConstructorCache.get(clazz);
            if (emptyConstructor == null)
                emptyConstructorCache.put(clazz, emptyConstructor = clazz.getDeclaredConstructor(EMPTY_CLASS_ARRAY));

            return emptyConstructor.newInstance(EMPTY_OBJECT_ARRAY);
        } catch (Exception e) {
            Core.logInfo("Could not construct %s, does it have a no-args constructor?", Utils.getSimpleName(clazz));
            throw new RuntimeException(e);
        }
    }

    /**
     * Construct a class with the given arguments.
     * Creates a fair bit of objects.
     *
     * @param clazz The class to create an instance of.
     * @param args  The arguments to construct the class with.
     * @return object
     */
    public static <T> T constructWithArgs(Class<T> clazz, Object... args) {
        Utils.verifyNotNull(clazz, "Tried to construct null class.");
        Class[] argTypes = getClasses(false, args);

        try {
            return clazz.getDeclaredConstructor(argTypes).newInstance(args);
        } catch (Exception e) {
            Core.logInfo("Could not construct %s. (%d Arguments)", Utils.getSimpleName(clazz), args.length);
            throw new RuntimeException(e);
        }
    }

    /**
     * Force construct a class with default or null values, if possible.
     * As of 1-17-2019, this only is ever used to construct EulerAngle, stored in ArmorPose.
     *
     * @param clazz The class to construct.
     * @return constructed
     */
    @SuppressWarnings("unchecked")
    public static <T> T forceConstruct(Class<T> clazz) {
        try {
            if(clazz.isAssignableFrom(Byte.class) || clazz.isAssignableFrom(byte.class) ) {
                return (T) (Byte) (byte) 0;
            } else if(clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(short.class) ) {
                return (T) (Short) (short) 0;
            } else if(clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(int.class) ) {
                return (T) (Integer) 0;
            } else if(clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(long.class) ) {
                return (T) (Long) 0L;
            } else if(clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(float.class) ) {
                return (T) (Float) 0.0F;
            } else if(clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(double.class) ) {
                return (T) (Double) 0.0D;
            } else if(clazz.isAssignableFrom(Boolean.class) || clazz.isAssignableFrom(boolean.class) ) {
                return (T) (Boolean) false;
            }

            Constructor c = null;
            int minParams = Integer.MAX_VALUE;
            for (Constructor cls : clazz.getConstructors()) {
                if (cls.getParameterCount() < minParams) {
                    minParams = cls.getParameterCount();
                    c = cls;
                }
            }

            if (minParams == Integer.MAX_VALUE) {
                Core.logInfo("Failed to force-construct %s. No constructor found.", clazz.getSimpleName());
                return null;
            }

            Object[] args = new Object[minParams];
            for (int i = 0; i < minParams; i++) {
                Class<?> cls = c.getParameterTypes()[i];
                if (ReflectionUtil.getNumbers().contains(cls)) {
                    args[i] = 0;
                } else if (Boolean.class.isAssignableFrom(cls)) {
                    args[i] = false;
                }
            }

            return (T) c.newInstance(args);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            ex.printStackTrace();
            Core.logInfo("Failed to force-construct %s!", Utils.getSimpleName(clazz));
            return null;
        }
    }

    /**
     * Get an array of classes from the arrays of the objects supplied.
     *
     * @param objects The objects to get classes from..
     * @return classes
     */
    private static Class<?>[] getClasses(boolean allowNull, Object... objects) {
        if (objects == null || objects.length == 0)
            return EMPTY_CLASS_ARRAY;

        Class<?>[] classes = new Class<?>[objects.length];
        for (int i = 0; i < objects.length; i++) {
            Object current = objects[i];

            if (current != null) {
                classes[i] = getReplacedClass(current.getClass());
            } else if (!allowNull) {
                throw new GeneralException("Tried to get the class of a null object!");
            }
        }

        return classes;
    }

    /**
     * Set a field in an object.
     *
     * @param o       The object to set the field in.
     * @param from    The class the field is defined in.
     * @param varName The name of the field.
     * @param val     The value to set.
     */
    public static void setField(Object o, Class<?> from, String varName, Object val) {
        try {
            findField(from, varName).set(o, val);
        } catch (Exception e) {
            e.printStackTrace();
            Core.logInfo("Failed to set field '%s'.", varName);
        }
    }

    /**
     * Set a field value in an object.
     *
     * @param o       The object to modify.
     * @param varName The name of the field to change.
     * @param val     The new value to set.
     */
    public static void setField(Object o, String varName, Object val) {
        setField(o, o.getClass(), varName, val);
    }

    /**
     * Set a static field in an object.
     *
     * @param clazz   The class the static field is defined in.
     * @param varName The name of the static field.
     * @param newVal  The value to set.
     */
    public static void setStaticField(Class<?> clazz, String varName, Object newVal) {
        setField(null, clazz, varName, newVal);
    }

    /**
     * Get a static value from a class.
     *
     * @param clazz The class which owns the field
     * @param field The name of the field to get.
     * @return value
     */
    public static Object getField(Class<?> clazz, String field) {
        return getField(null, clazz, field);
    }

    /**
     * Gets a value from an object.
     *
     * @param o     The object to get the value from.
     * @param field The name of the field to get.
     * @return fieldValue
     */
    public static Object getField(Object o, String field) {
        return getField(o, o.getClass(), field);
    }

    public static Object getField(Object o, Class clazz, String fieldName) {
        try {
            return findField(clazz, fieldName).get(o);
        } catch (Exception e) {
            throw new GeneralException(e, "Failed to get value from field '" + fieldName + "'.");
        }
    }

    public static Object getFieldIncludeParents(Object o, String fieldName) {
        return getFieldIncludeParents(o, o.getClass(), fieldName);
    }

    /**
     * Gets a value from an object including all parent classes.
     *
     * @param o     The object to get the value from.
     * @param field The name of the field to get.
     * @return fieldValue
     */
    public static Object getFieldIncludeParents(Object o, Class clazz, String field) {
        try {
            return findFieldIncludeParents(clazz, field).get(o);
        } catch (Exception e) {
            throw new GeneralException(e, "Failed to get value from field '" + field + "'.");
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        Field field = cacheFields(clazz).get(fieldName);
        Utils.verifyNotNull(field, "Unknown field '" + fieldName + "' in " + Utils.getSimpleName(clazz) + ".");
        return field;
    }


    /**
     * Search for a field in a class and its parents.
     *
     * @param clazz     The class to search.
     * @param fieldName The name of the field to get.
     * @return field - Throws an exception if not found.
     */
    public static Field findFieldIncludeParents(Class<?> clazz, String fieldName) {
        while (!clazz.equals(Object.class)) {
            Field field = cacheFields(clazz).get(fieldName);
            if (field != null)
                return field;

            clazz = clazz.getSuperclass();
        }

        throw new RuntimeException("Unknown field '" + fieldName + "' in " + Utils.getSimpleName(clazz) + " (or its parents).");
    }

    /**
     * Gets an NMS class by the given name.
     */
    public static Class<?> getNMS(String clazz) {
        return getClass("net.minecraft.server." + Constants.NMS_VERSION + '.' + clazz);
    }

    /**
     * Gets a CB class by the given name.
     */
    public static Class<?> getCraftBukkit(String clazz) {
        return getClass("org.bukkit.craftbukkit." + Constants.NMS_VERSION + '.' + clazz);
    }

    /**
     * Get a list of all inherited and non-inherited fields.
     *
     * @param cls The class to get fields from.
     * @return fields
     */
    public static List<Field> getAllFields(Class<?> cls) {
        List<Field> fields = getJustClassFields(cls);

        Class<?> superClass = cls.getSuperclass();
        if (superClass != null)
            fields.addAll(getAllFields(superClass));
        return fields;
    }

    private static List<Field> getJustClassFields(Class<?> cls) {
        List<Field> fields = new ArrayList<>();
        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            fields.add(field);
        }

        return fields;
    }

    private static Map<String, Field> cacheFields(Class<?> cls) {
        Map<String, Field> fieldMap = fieldCacheMap.get(cls);
        if (fieldMap != null)
            return fieldMap;

        fieldMap = new HashMap<>();
        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            fieldMap.put(field.getName(), field);
        }
        fieldCacheMap.put(cls, fieldMap);

        return fieldMap;
    }

    /**
     * Get a minecraft class in the net.minecraft.server package
     * @param name
     * @return
     */
    public static Class<?> getMinecraftClass(String name) {
        return getClass(NMS_PREFIX + "." + name);
    }

    /**
     * Get a class by its path.
     *
     * @param path The path of the class to get.
     * @return class
     */
    public static Class<?> getClass(String path) {
        try {
            Class<?> resultClass = classCacheMap.get(path);

            if (resultClass == null) {
                String cutName = path.split("<")[0]; // Remove any generics.
                if (cutName.startsWith("class ")) // Remove the "class " prefix that prepends the classpath when gotten from Type.
                    cutName = cutName.substring("class ".length());

                classCacheMap.put(path, resultClass = Class.forName(cutName));
            }

            return resultClass;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a class by its type.
     *
     * @param type The type to get the class from.
     * @return class
     */
    public static Class<?> getClass(Type type) {
        return getClass(type.toString());
    }

    /**
     * Get the generic type of a field.
     * If there is no generic type, it will return null.
     *
     * @param f The field which has a generic type.
     * @return generic
     */
    public static Class<?> getGenericType(Field f) {
        return f != null && f.getGenericType() instanceof ParameterizedType ?
                getClass(((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]) : null;
    }

    public static Collection<Class<?>> getNumbers() {
        return PRIMITIVES.values();
    }

    public static Class<?> getPrimitive(Class<?> type) {
        return PRIMITIVES.getOrDefault(type, type);
    }

    public static Class<?> getReplacedClass(Class<?> type) {
        return REPLACE.getOrDefault(type, getPrimitive(type));
    }

    /**
     * Set an object's field using method handles
     * @param o The object we want to modify
     * @param fieldName The field name we want to modify
     */
    public static void getFieldMethodHandle(Object o, String fieldName) {
        try {
            getGetterHandle(o.getClass(), fieldName).invoke();
        } catch (Throwable e) {
            e.printStackTrace();
            Core.logInfo("Failed to GET field using method handles for field %s in %s.", fieldName, o.getClass().getSimpleName());
        }
    }

    /**
     * Set an object's field using method handles
     * @param o The object we want to modify
     * @param fieldName The field name we want to modify
     * @param newValue The new value for the field
     */
    public static void setFieldMethodHandle(Object o, String fieldName, Object newValue) {
        try {
            getSetterHandle(o.getClass(), fieldName).invoke(o, newValue);
        } catch (Throwable e) {
            e.printStackTrace();
            Core.logInfo("Failed to set field using method handles for field %s in %s.", fieldName, o.getClass().getSimpleName());
        }
    }

    /**
     * Returns a getter method handle for a given filed in a given class.
     * @param clazz The class to get the lookup for
     * @param name The name to get the lookup for
     * @return getter
     */
    private static MethodHandle getGetterHandle(Class<?> clazz, String name) {
        try {
            return METHOD_LOOKUPS.unreflectGetter(findField(clazz, name));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Core.logInfo("Failed to get getter method lookup for field %s in class %s.", name, clazz.getSimpleName());
            return null;
        }
    }

    /**
     * Returns a setter method handle for a given filed in a given class.
     * @param clazz The class to get the lookup for
     * @param name The name to get the lookup for
     * @return setter
     */
    private static MethodHandle getSetterHandle(Class<?> clazz, String name) {
        try {
            return METHOD_LOOKUPS.unreflectSetter(findField(clazz, name));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Core.logInfo("Failed to get setter method lookup for field %s in class %s.", name, clazz.getSimpleName());
            return null;
        }
    }

    static {
        PRIMITIVES.put(Boolean.class, Boolean.TYPE);
        PRIMITIVES.put(Byte.class, Byte.TYPE);
        PRIMITIVES.put(Short.class, Short.TYPE);
        PRIMITIVES.put(Integer.class, Integer.TYPE);
        PRIMITIVES.put(Long.class, Long.TYPE);
        PRIMITIVES.put(Float.class, Float.TYPE);
        PRIMITIVES.put(Double.class, Double.TYPE);
    }
}
