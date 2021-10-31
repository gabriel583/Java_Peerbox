package pt.ipb.dsys.peerbox.common;

import org.jgroups.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.DnsHelper;
import pt.ipb.dsys.peerbox.Main;
import pt.ipb.dsys.peerbox.util.PeerUtil;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

//import static pt.ipb.dsys.peerbox.Main.gossipRouter;

public class PeerBoxC implements PeerBox{

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private int block_size = PeerBox.BLOCK_SIZE;

    public String user_name= System.getProperty("user.name", "n/a") + InetAddress.getLocalHost().toString();

    TreeSet<Chunk> chunksgeral=new TreeSet<Chunk>();

    TreeSet<PeerFile> peerFiles = new TreeSet<>();

    TreeSet<ChunkUser> chunkusers =new TreeSet<ChunkUser>();

    public JChannel channel;

    public PeerBoxC(JChannel c) throws UnknownHostException {
        this.channel = c;
    }

    @Override
    public PeerFileID save(String path, int replicas) throws Exception,PeerBoxException {
        try {
            File f = new File(path);
            String fileName = generateHash(f.getName());
            int partCounter = 0;
            PeerFileID fileID = new PeerFileID(fileName);
            byte[] array = Files.readAllBytes(Paths.get(path));
            int start = 0;
            //List<byte[]> result = new ArrayList<byte[]>();
            while (start < array.length) {
                int end = Math.min(array.length, start + block_size);
                partCounter++;
                Chunk chunks = new Chunk(fileName, partCounter, Arrays.copyOfRange(array, start, end));
                //result.add(Arrays.copyOfRange(array, start, end));
                start += block_size;
                fileID.chunks.add(chunks);
            }
            fileID.setNumChunks(partCounter);
            for (Chunk c : fileID.chunks) {
                for (int i = 0; i < replicas; i++) {
                    Objeto o = new Objeto("send", c);
                    ObjectMessage message = new ObjectMessage(null, o);
                    channel.send(message);
                    channel.setDiscardOwnMessages(true); // bloquear propria mensagem
                }
            }
            TreeSet<Chunk> a = fileID.chunks;
            fileID.chunks.removeAll(a);
            return fileID;
        } catch (NoSuchFileException e){
            System.out.println("The file has been moved, renamed or deleted.");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String generateHash(String filename) throws NoSuchAlgorithmException {
        String plaintext = filename;
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(plaintext.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String hashtext = bigInt.toString(16);
        return hashtext;
    }
    public void fetchFile(String path) throws Exception {
        File f = new File(path);
        String fileName = generateHash(f.getName());
        PeerFile peerFile = null;
        for(PeerFile p: peerFiles){
            if(p.getFileId().getFileId().equals(fileName)){
                peerFile = fetch(p.getFileId());
            }
        }
        try (FileOutputStream fos = new FileOutputStream(path)) {
            assert peerFile != null;
            fos.write(peerFile.getData());
        } catch (Exception e){
            System.out.println("File does not exist in the PeerBox");
        }
    }

    public void saveFile(String path,int replicas) throws Exception {
        PeerFile peerFile = new PeerFile();
        peerFile.setFileId(save(path, replicas));
        if(peerFile.getFileId() != null) {
            peerFiles.add(peerFile);
        }
    }

    public void listOfFiles(){
        System.out.println(peerFiles);
    }

    public void retrieveMetadata(String path) throws NoSuchAlgorithmException {
        File f = new File(path);
        String fileName = generateHash(f.getName());
        for(PeerFile p: peerFiles){
            if(p.getFileId().getFileId().equals(fileName)){
                System.out.println(p.toString());
            }
        }
    }

    @Override
    public PeerFile fetch(PeerFileID id) throws Exception {
        int numchunks = id.getNumChunks();
        int contador = 0;
        //requisitar chunks
        try {
            while (contador != numchunks) {
                for (int i = 1; i < numchunks + 1; i++) {
                    for (Chunk chunk : id.chunks) {
                        if (chunk.getChunkId() == i) { //verifica se o objeto tem o chunk
                            break;
                        }
                    }
                    //se não existir pede aos que tem o chunk
                    for (ChunkUser chunkuser : chunkusers) {
                        if (chunkuser.getFileId().equals(id.getFileId())) {
                            if (chunkuser.getChunkId() == i) {
                                ChunkUser novo = new ChunkUser(chunkuser.getUser(),chunkuser.getFileId(),chunkuser.getChunkId());
                                Objeto o = new Objeto("request", novo);
                                ObjectMessage message2 = new ObjectMessage(chunkuser.getAddress(), o);
                                channel.send(message2);
                            }
                        }
                    }
                }
                contador = id.chunks.size();
                Thread.sleep(100);
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            for (Chunk c : id.chunks) {
                os.write(c.getChunk(), 0, c.getChunk().length);
            }
            PeerFile peerFile = null;
            for (PeerFile p : peerFiles) {
                if (p.getFileId().getFileId().equals(id.getFileId())) {
                    p.setData(os.toByteArray());
                    peerFile = p;
                }
            }
            return peerFile;
        } catch (NotSerializableException e){
            System.out.println("Dado não Seriavel");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void delete(PeerFileID id) throws Exception {
        for (ChunkUser cu: chunkusers) {
            if(cu.getFileId().equals(id.getFileId())){
                ChunkUser novo = new ChunkUser(cu.getUser(), cu.getFileId(), cu.getChunkId());
                Objeto o = new Objeto("delete", novo);
                ObjectMessage message3 = new ObjectMessage(cu.getAddress(), o);
                channel.send(message3);
            }
        }
        chunkusers.removeIf(chunkUser -> chunkUser.getFileId().equals(id.getFileId()));
        peerFiles.removeIf(peerFile -> peerFile.getFileId().getFileId().equals(id.getFileId()));
    }

    public void WriteObjectToFile(Object serObj,String filepath) {
        try {
            FileOutputStream fileOut = new FileOutputStream(filepath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serObj);
            objectOut.close();
            System.out.println("The Object  was succesfully written to a file");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chunkUsersList(){
        System.out.println(chunkusers);
    }

    public void deleteFile(String fn) throws Exception {
        File f = new File(fn);
        String filename = generateHash(f.getName());
        for(PeerFile p: peerFiles){
            if(p.getFileId().getFileId().equals(filename)){
                delete(p.getFileId());
            }
        }
    }


    public void receiveChunk(){
        Object o = new Object();
        channel.setReceiver(new Receiver() {
            @Override
            public void receive(Message msg) {
                logger.info("Message from {} to {}: {}", msg.src(), msg.dest(), msg.getObject());
                //logger.info(msg.getObject());
                if(msg.getObject().toString().equals("send")) { // = receber chunk
                    Chunk a = (Chunk)((Objeto)msg.getObject()).getObjeto();
                    chunksgeral.add(a);
                    //WriteObjectToFile(a, "C:\\"+a.toString());
                    ChunkUser chunkusers = new ChunkUser(user_name,a.getFileID(),a.getChunkId());
                    Objeto o = new Objeto("recebido", chunkusers);
                    ObjectMessage message = new ObjectMessage(msg.getSrc(), o);
                    try {
                        channel.send(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    logger.info(channel.getAddressAsString() +" got a chunk from " + msg.src().toString());
                } else if (msg.getObject().toString().equals("request")){ //requisitar chunk é enviado um chunkuser
                    ChunkUser cl = (ChunkUser) ((Objeto)msg.getObject()).getObjeto();
                    for (Chunk c: chunksgeral) {
                        if(c.getFileID().equals(cl.getFileId())){
                            if (c.getChunkId().equals(cl.getChunkId())){
                                Objeto o = new Objeto("resend", c);
                                ObjectMessage message = new ObjectMessage(msg.getSrc(), o);
                                try {
                                    channel.send(message);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                logger.info("resend the chunk");
                            }
                        }
                    }
                }else if(msg.getObject().toString().equals("recebido")){
                    ChunkUser cu = (ChunkUser) ((Objeto)msg.getObject()).getObjeto();
                    cu.setAddress(msg.src());
                    chunkusers.add(cu);
                }else if(msg.getObject().toString().equals("resend")){
                    Chunk c = (Chunk) ((Objeto)msg.getObject()).getObjeto();
                    for(PeerFile p: peerFiles){
                        if(p.getFileId().getFileId().equals(c.getFileID())){
                            p.getFileId().chunks.add(c);
                        }
                    }
                } else if(msg.getObject().toString().equals("delete")) {
                    ChunkUser cu = (ChunkUser) ((Objeto) msg.getObject()).getObjeto();
                    chunksgeral.removeIf(c -> c.getFileID().equals(cu.getFileId()));
                    logger.info("Deleted chunks");
                }
            }
        });
    }
}
