package nr.reactor.models;

public class UsuConCom {

    private Usuario usuario;

    private Comentarios comentarios;

    public UsuConCom(Usuario usuario, Comentarios comentarios) {
        this.usuario = usuario;
        this.comentarios = comentarios;
    }

    @Override
    public String toString() {
        return "UsuConCom [ usuario=" + usuario + "- comentarios=" + comentarios + "]";
    }

}
