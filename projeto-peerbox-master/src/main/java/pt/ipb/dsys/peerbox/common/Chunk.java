package pt.ipb.dsys.peerbox.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.TreeSet;

public class Chunk implements Serializable, Comparable<Chunk> {
    private Integer chunkId;
    private String FileID;
    private byte[] chunk;
    public Chunk(String fileId,int chunkid,byte[] c){
        this.chunkId = chunkid;
        this.FileID = fileId;
        this.chunk = c;
    }
    public Integer getChunkId(){
        return chunkId;
    }
    public String getFileID(){
        return FileID;
    }
    public byte[] getChunk(){return chunk;}

    @Override
    public String toString() {
        return "FileID: " + FileID + ", ChunkID: "+chunkId;
    }

    @Override
    public int compareTo(Chunk o) {
        int c;
        c = this.getFileID().compareTo(o.getFileID());
        if(c == 0){
            c = this.getChunkId().compareTo(o.getChunkId());
        }
        return c;
    }

}
