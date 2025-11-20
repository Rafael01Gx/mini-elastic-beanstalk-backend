package com.elasticbeanstalk.mini_elastic_beanstalk.util;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class NetworkUtils {

    public String generateNetworkName(String serverId) {
        return "network-server-" + serverId;
    }

    public boolean isValidPort(int port) {
        return port >= 1 && port <= 65535;
    }

    public boolean isValidPortRange(int from, int to) {
        return isValidPort(from) && isValidPort(to) && from <= to;
    }

    public boolean isValidIp(String ip) {
        try {
            InetAddress.getByName(ip);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public boolean isValidCidr(String cidr) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            return false;
        }

        try {
            int prefix = Integer.parseInt(parts[1]);
            return isValidIp(parts[0]) && prefix >= 0 && prefix <= 32;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}