package nr.client.models;

import java.util.Date;
import lombok.Data;

@Data
public class Producto {
    private String id,nombre,file;
    private Double precio;
    private Date createAt;
    private Categoria categoria;
}
