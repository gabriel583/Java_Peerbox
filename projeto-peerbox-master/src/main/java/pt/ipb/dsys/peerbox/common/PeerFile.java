package pt.ipb.dsys.peerbox.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;

public class PeerFile implements  Comparable<PeerFile> {

    private PeerFileID fileId;

    private byte[] data;

    public PeerFileID getFileId() {
        return fileId;
    }

    public void setFileId(PeerFileID fileId) {
        this.fileId = fileId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return fileId.toString();
    }

    @Override
    public int compareTo(PeerFile o) {
        return this.fileId.getFileId().compareTo(o.fileId.getFileId());
    }
}
