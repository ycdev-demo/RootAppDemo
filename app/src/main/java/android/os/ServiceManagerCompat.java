package android.os;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServiceManagerCompat {
    private static final String CLASSNAME_SERVICE_MANAGER= "android.os.ServiceManager";

    private static Class<?> sServiceManagerClass;
    private static Method sGetServiceMethod;

    static {
        try {
            sServiceManagerClass = Class.forName(CLASSNAME_SERVICE_MANAGER, false, Thread.currentThread().getContextClassLoader());
            sGetServiceMethod = sServiceManagerClass.getMethod("getService",  new Class[] { String.class});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static IBinder getService(Object name) {
        if (sGetServiceMethod != null) {
            try {
                Method localMethod = sGetServiceMethod;
                Object[] arrayOfObject = new Object[] { name };
                Object ret = localMethod.invoke(null, arrayOfObject);
                return (IBinder) ret;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
