package org.ostrya.presencepublisher.mqtt;

import androidx.annotation.WorkerThread;

import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class LocalAddressChecker {
    private static final String TAG = "LocalAddressChecker";

    @WorkerThread
    public static boolean isLocalAddress(String host) {
        if (host.endsWith(".local")) {
            return true;
        }
        try {
            InetAddress address = InetAddress.getByName(host);
            if (address.isSiteLocalAddress()
                    || address.isLinkLocalAddress()
                    || address.isMulticastAddress()) {
                return true;
            }
            if (address instanceof Inet4Address) {
                Inet4Address address4 = (Inet4Address) address;
                // according to AI, CGNAT is also considered "local"
                return address4.getAddress()[0] == 100 && ((address4.getAddress()[1] & 0xC0) == 64);
            } else if (address instanceof Inet6Address) {
                Inet6Address address6 = (Inet6Address) address;
                // not sure if it's correct, but the source of Inet6Address.isSiteLocalAddress()
                // seems
                // to check the deprecated site-local range (RFC 3879), so let's check the ULA range
                // (RFC 4193) manually
                return (address6.getAddress()[0] & 0xFF) == 0xFC
                        || (address6.getAddress()[0] & 0xFF) == 0xFD;
            }
        } catch (UnknownHostException e) {
            DatabaseLogger.i(TAG, "Could not resolve address, assuming local", e);
            return true;
        }
        return false;
    }
}
