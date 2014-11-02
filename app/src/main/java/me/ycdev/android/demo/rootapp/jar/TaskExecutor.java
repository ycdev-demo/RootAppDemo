package me.ycdev.android.demo.rootapp.jar;

import android.content.Context;
import android.os.IBinder;
import android.os.PowerManagerCompat;
import android.os.ServiceManagerCompat;

import java.util.Arrays;

public class TaskExecutor {
    private static final String CMD_REBOOT = "reboot";

    public static void main(String[] args) {
        System.out.println("Received params: " + Arrays.toString(args));
        if (args.length < 1) {
            System.err.println("Usage: TaskExecutor <command> [command parameters]");
            return;
        }

        String cmd = args[0];
        if (cmd.equals(CMD_REBOOT)) {
            reboot();
        }
    }

    private static void reboot() {
        IBinder binder = ServiceManagerCompat.getService(Context.POWER_SERVICE);
        Object service = PowerManagerCompat.asInterface(binder);
        PowerManagerCompat.reboot(service);
    }
}
