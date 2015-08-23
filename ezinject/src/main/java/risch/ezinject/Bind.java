package risch.ezinject;


public class Bind<T>{
    private final Class interfaze;
    private final EZInject ezInject;

    public Bind(EZInject ezInject, Class<T> interfaze){
        this.interfaze = interfaze;
        this.ezInject = ezInject;
    }


    public EZInject to(Class<? extends T> implementation){
        validate(implementation,interfaze);
        ezInject.map(interfaze,implementation);
        return ezInject;
    }
    public EZInject to(T instance){
        ezInject.mapInstance(interfaze, instance);
        return ezInject;
    }


    public static boolean implementsInterface(Class implementation, Class interfaze){
        boolean hasInterface = false;
        for(Class c : implementation.getInterfaces()){
            if(c.getName().equalsIgnoreCase(interfaze.getName())){
                hasInterface= true;
                break;
            }
        }
        return hasInterface;
    }


    private void validate(Class implementation, Class interfaze){
        if (!interfaze.isInterface()) {
            throw new RuntimeException(interfaze.getName() + EZInject.INJECT_BIND_MUST_BIND_INTERFACE);
        }
        if(implementation.isInterface()) {
            throw new RuntimeException(implementation.getName() + EZInject.INJECT_BIND_TO_MUST_BE_CLASS);
        }
        if(!implementsInterface(implementation,interfaze)){
            throw new RuntimeException(implementation.getName() + EZInject.INJECT_BOUND_CLASS_MUST_IMPLEMENT_INTERFACE + interfaze.getName());
        }
    }
}