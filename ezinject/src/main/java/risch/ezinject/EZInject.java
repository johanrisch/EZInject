package risch.ezinject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by johan.risch on 21/08/15.
 */
public class EZInject {
    public static final String INJECT_BIND_MUST_BIND_INTERFACE = " is not an interface";
    public static final String INJECT_BIND_TO_MUST_BE_CLASS = " is an interface";
    public static final String INJECT_BOUND_CLASS_MUST_IMPLEMENT_INTERFACE = " does not implement ";
    public static final String NO_INJECT_CONSTRUCTOR = " has no @Inject annotated methods";
    public static final String FOUND_CYCLIC_DEPENDENCY_STRING = "Found cyclic dependency when creating ";
    private static final String NO_INSTANCE_BOUND = " Has not be bound to an implementation or instance";
    public static boolean verifyDependencies = true;
    HashMap<Class, Class> interfaceToClassMap = new HashMap<>();
    HashMap<Class, Object> interfaceToInstanceMap = new HashMap<>();
    private static EZInject ezInject;
    private static HashMap<Class,Object> singletons = new HashMap<>();

    private EZInject() {
    }

    public static synchronized EZInject getInstance() {
        if (ezInject == null) {
            ezInject = new EZInject();
        }
        return ezInject;
    }


    public static Bind bind(Class interfaze) {
        return new Bind(getInstance(), interfaze);
    }
    private static <T> T create(Class<T> injectee, HashSet<Class> visited){
        if(verifyDependencies && visited.contains(injectee)) throw new RuntimeException(EZInject.FOUND_CYCLIC_DEPENDENCY_STRING + injectee.getName());
        if(verifyDependencies) visited.add(injectee);
        if(!injectee.isInterface() && getInstance().interfaceToInstanceMap.get(injectee) == null) throw new RuntimeException(injectee.getName() + NO_INSTANCE_BOUND);
        if(getInstance().interfaceToInstanceMap.get(injectee) != null) return (T)getInstance().interfaceToInstanceMap.get(injectee);
        Class impl = getInstance().interfaceToClassMap.get(injectee);


        if(singletons.get(injectee) != null) return (T)singletons.get(injectee);
        if(impl == null) throw new RuntimeException(injectee.getName() + " is not bound to any class");

        Constructor[] constructors = impl.getDeclaredConstructors();
        try {
            for(Constructor c : constructors) {
                if(!c.isAnnotationPresent(Inject.class)) continue;

                Class[] types = c.getParameterTypes();
                Object[] args = new Object[types.length];
                for (int i = 0; i < types.length; i++) {
                    args[i] = create(types[i],new HashSet<>(visited));
                }
                T instance = (T) c.newInstance(args);

                if(c.isAnnotationPresent(Singleton.class)) singletons.put(injectee,instance);
                return instance;

            }
            throw new RuntimeException(impl.getName() + NO_INJECT_CONSTRUCTOR);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static <T> T create(Class<T> injectee) {
        return create(injectee,new HashSet<Class>());

    }

    void map(Class interfaze, Class implementation) {
        interfaceToClassMap.put(interfaze, implementation);
    }
    <T> void mapInstance(Class<T> interfaze, T instance) {
        interfaceToInstanceMap.put(interfaze, instance);
    }

    public static synchronized void reset() {
        ezInject = null;
        singletons = new HashMap<>();
    }


}