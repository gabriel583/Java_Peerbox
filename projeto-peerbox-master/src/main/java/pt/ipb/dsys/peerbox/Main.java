package pt.ipb.dsys.peerbox;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.ObjectMessage;
import org.jgroups.View;
import org.jgroups.protocols.PING;
import org.jgroups.protocols.TUNNEL;
import org.jgroups.stack.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.common.Objeto;
import pt.ipb.dsys.peerbox.common.PeerBoxC;
import pt.ipb.dsys.peerbox.jgroups.DefaultProtocols;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.PeerUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static final String CLUSTER_NAME = "PeerBox";

    public String user_name=System.getProperty("user.name", "n/a");


    public static void main(String[] args) {
        PeerUtil.localhostFix();
        try (JChannel channel = new JChannel(DefaultProtocols.gossipRouter())) {
            channel.connect(CLUSTER_NAME);
            channel.setReceiver(new LoggingReceiver());
            String hostname = DnsHelper.getHostName();
            PeerBoxC peerbox = new PeerBoxC(channel);
            peerbox.receiveChunk();
            int i = 1;
            if(PeerUtil.isPeer()){
                while (true);
            } else {
                System.out.println(channel.getAddressAsUUID());
                System.out.println(channel.getAddressAsString());
                while (i == 1){
                    i = Menu(peerbox);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static int Menu(PeerBoxC p) throws Exception {
        Scanner myObj = new Scanner(System.in);
        System.out.println("Chose a option");
        System.out.println("S - save a File \n F - Fetch a File \n D - delete a File \n L - list of Files \n R - retrieve metada \n U - List Users who got a chunk \n Q - quit");
        String options = myObj.nextLine().toLowerCase(Locale.ROOT);
        switch (options){
            case "s":
                myObj = new Scanner(System.in);
                System.out.println("Input the file path and the replicas numbers");
                String path = myObj.nextLine();
                int replicas = myObj.nextInt();
                p.saveFile(path,replicas);
                System.out.println("Done");
                break;
            case "f":
                myObj = new Scanner(System.in);
                System.out.println("Input the path");
                path = myObj.nextLine();
                p.fetchFile(path);
                System.out.println("Done");
                break;
            case "d":
                myObj = new Scanner(System.in);
                System.out.println("Input the path");
                path = myObj.nextLine();
                p.deleteFile(path);
                System.out.println("Done");
                break;
            case "l":
                p.listOfFiles();
                break;
            case "r":
                myObj = new Scanner(System.in);
                System.out.println("Input the path");
                path = myObj.nextLine();
                p.retrieveMetadata(path);
                System.out.println("Done");
                break;
            case "q":
                System.out.println("leaving");
                return 0;
            case "u":
                p.chunkUsersList();
                break;
            default: break;
        }
        return 1;
    }
}