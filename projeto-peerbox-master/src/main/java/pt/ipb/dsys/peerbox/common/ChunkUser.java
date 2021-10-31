package pt.ipb.dsys.peerbox.common;

import org.jgroups.Address;

import java.io.Serializable;
import java.util.LinkedList;

public class ChunkUser implements Comparable<ChunkUser>, Serializable {
    private String user;
    private String fileId;
    private Integer chunkId;
    private Address address;

    public ChunkUser(String u, String fid, Integer cid){
        this.user = u;
        this.fileId = fid;
        this.chunkId = cid;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    public Integer getChunkId() {
        return chunkId;
    }

    public String getFileId() {
        return fileId;
    }

    public String getUser() {
        return user;
    }

    public String toString() {
        return "FileID: " + fileId + ", ChunkID: "+chunkId + ", Usuario: " + user;
    }

    @Override
    public int compareTo(ChunkUser o) {
        int c;
        c = this.getFileId().compareTo(o.getFileId());
        if(c == 0){
            c = this.getChunkId().compareTo(o.getChunkId());
        }if(c == 0){
            c = this.getUser().compareTo(o.getUser());
        }
        return c;
    }
}
