package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.apache.commons.lang.RandomStringUtils;
import play.db.jpa.Blob;
import play.db.jpa.GenericModel;

@Entity
public class Cliente extends GenericModel {

    @Id
    @GeneratedValue
    public long id;

    @Column(unique=true)
    public String cpf;
    
    @Column(unique=true)
    public String usuario;
    
    public String nome;
    public String data;
    public String email;
    public String endereco;
    public String cidade;
    public String senha;
    public String facebook;
    public String twitter;
    public String youtube;
    public String qrcode = RandomStringUtils.randomAlphanumeric(30);
    public Blob foto;

    @ManyToOne
    @JoinColumn(name = "vendedor_fk")
    public Vendedor vendedor;

    @Enumerated(EnumType.STRING)
    public Status status;
    @Enumerated(EnumType.STRING)
    public Status termo;
}
