package org.example;

import org.apache.zookeeper.*;
import org.apache.zookeeper.admin.ZooKeeperAdmin;
import org.apache.zookeeper.client.ZKClientConfig;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZKClientConfig zkClientConfig = new ZKClientConfig();
        ZooKeeper zk = new ZooKeeperAdmin(String.format("%s:2181", args[0]), 300 * 1000, new MyWatcher(), false, zkClientConfig);
        List<ACL> aclList = new ArrayList<>();
        aclList.add(new ACL(getPermFromString("cdrwa"), new Id("x509", args[1])));
        aclList.add(new ACL(getPermFromString("r"), new Id("world", "anyone")));

        ZKUtil.visitSubTreeDFS(zk, "/hello", false, (rc, p, ctx, name) -> {
            try {
                zk.setACL(p, aclList, -1);
            } catch (KeeperException | InterruptedException ignore) {
            }
        });
        zk.close();
    }
    private static int getPermFromString(String permString) {
        int perm = 0;
        for (int i = 0; i < permString.length(); i++) {
            switch (permString.charAt(i)) {
                case 'r':
                    perm |= ZooDefs.Perms.READ;
                    break;
                case 'w':
                    perm |= ZooDefs.Perms.WRITE;
                    break;
                case 'c':
                    perm |= ZooDefs.Perms.CREATE;
                    break;
                case 'd':
                    perm |= ZooDefs.Perms.DELETE;
                    break;
                case 'a':
                    perm |= ZooDefs.Perms.ADMIN;
                    break;
                default:
                    System.err.println("Unknown perm type: " + permString.charAt(i));
            }
        }
        return perm;
    }
}