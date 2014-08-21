package android.os;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PowerManagerCompat {
    private static final String CLASSNAME_IPOWERMANAGER = "android.os.IPowerManager";
    private static final String CLASSNAME_IPOWERMANAGER_STUB = "android.os.IPowerManager$Stub";

    private static final int API_VERSION_1 = 1;
    private static final int API_VERSION_2 = 2;

    private static Class<?> sIPowerManagerClass;
    private static Method sAsInterfaceMethod;
    private static Method sRebootMethod;
    private static int sRebootMethodVersion;

    static {
        try {
            Class<?> clazz = Class.forName(CLASSNAME_IPOWERMANAGER_STUB);
            sIPowerManagerClass = Class.forName(CLASSNAME_IPOWERMANAGER);
            sAsInterfaceMethod = clazz.getMethod("asInterface", IBinder.class);
            try {
                // void reboot(String reason);
                sRebootMethod = sIPowerManagerClass.getMethod("reboot", String.class);
                sRebootMethodVersion = API_VERSION_1;
            } catch (Exception e) {
                // void reboot(boolean confirm, String reason, boolean wait);
                sRebootMethod = sIPowerManagerClass.getMethod("reboot",
                        boolean.class, String.class, boolean.class);
                sRebootMethodVersion = API_VERSION_2;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sAsInterfaceMethod = null;
            sIPowerManagerClass = null;
            sRebootMethod = null;
        }
    }

    public static void reboot(Object servie) {
        if (servie != null && sRebootMethod != null) {
            try {
                Method localMethod = sRebootMethod;
                if (sRebootMethodVersion == API_VERSION_1) {
                    localMethod.invoke(servie, "reboot");
                } else if (sRebootMethodVersion == API_VERSION_2) {
                    localMethod.invoke(servie, false, "reboot", false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Object asInterface(IBinder binder) {
        if (sAsInterfaceMethod != null) {
            try {
                Method localMethod = sAsInterfaceMethod;
                return localMethod.invoke(null, binder);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}