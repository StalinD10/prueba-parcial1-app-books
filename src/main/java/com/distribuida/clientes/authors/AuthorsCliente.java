package com.distribuida.clientes.authors;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class AuthorsCliente {
    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private String firstName;
    @Getter
    @Setter
    private String lastName;
}
