package pt.ipb.dsys.peerbox.common;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.TreeSet;

public class PeerFileID implements Serializable {
    public PeerFileID(String fid){
        this.fileId = fid;
    }
    TreeSet<Chunk> chunks=new TreeSet<Chunk>();
    private String fileId;
    private int numChunks;

    public int getNumChunks() {
        return numChunks;
    }

    public void setNumChunks(int numChunks) {
        this.numChunks = numChunks;
    }

    public String getFileId() {
        return fileId;
    }
    // Members will depend on the metadata specific to your implementation


    @Override
    public String toString() {
        return "PeerFileID{" +
                ", fileId='" + fileId + '\'' +
                ", Numero de Chunks=" + numChunks +
                ", chunks: " + chunks +
                '}';
    }
}
