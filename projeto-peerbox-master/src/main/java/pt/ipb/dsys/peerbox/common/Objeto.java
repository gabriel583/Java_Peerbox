package pt.ipb.dsys.peerbox.common;

import java.io.Serializable;

public class Objeto implements Serializable {
    public Objeto(String a, Object o){
        this.acao = a;
        this.objeto = o;
    }
    private String acao;
    private Object objeto;

    public Object getObjeto() {
        return objeto;
    }

    @Override
    public String toString() {
        return this.acao;
    }
}
