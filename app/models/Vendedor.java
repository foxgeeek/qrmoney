package models;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import play.db.jpa.GenericModel;

@Entity
public class Vendedor extends GenericModel{

    @Id
    @GeneratedValue
    public long id;

    @Column(unique=true)
    public String cpf;

    @Column(unique=true)
    public String email;
    
    public String nome;
    public String data;
    public String endereco;
    public String cidade;
    public String latitude;
    public String longitude;
    public String senha;
    public String usuario;
    public String foto;

    @OneToMany(mappedBy="vendedor")
    public List<Cliente> cliente;

    @Enumerated(EnumType.STRING)
    public Status termo;
    @Enumerated(EnumType.STRING)
    public Status status;
			
}