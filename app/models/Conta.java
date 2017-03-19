package models;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import play.db.jpa.Model;

@Entity
public class Conta extends Model{
	
    @Enumerated(EnumType.STRING)
    public Status status;

    @Enumerated(EnumType.STRING)
    public Status aceite;

    @OneToOne
    public Cliente cliente;

    public String debito;
    public String credito;
    public String creditado;
    public String data;
	
}