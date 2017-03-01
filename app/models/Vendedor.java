package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.Blob;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;

@Entity
public class Vendedor extends GenericModel{

	@Id
	@GeneratedValue
	public long id;
	
	public String nome;
	public String data;
	
	@Column(unique=true)
	public String cpf;
	
	public String email;
	public String endereco;
	public String cidade;
	
	@Column(unique=true)
	public String usuario;
	
	public String senha;
	public String facebook;
	public String twitter;
	public String youtube;
	
	public Blob foto;
	
	@OneToMany(mappedBy="vendedor")
	public List<Cliente> cliente;
	
	@Enumerated(EnumType.STRING)
	public Status termo;
	@Enumerated(EnumType.STRING)
	public Status status;
	
	public long uid;
	public String access_token;
	
	public Vendedor(long uid) {
        this.uid = uid;
    }

    public static Vendedor get(long id) {
        return find("uid", id).first();
    }

    public static Vendedor createNew() {
        long uid = (long)Math.floor(Math.random() * 10000);
        Vendedor user = new Vendedor(uid);
        user.create();
        return user;
    }
	
}
