package com.distribuida.rest;

import com.distribuida.clientes.authors.AuthorRestProxy;
import com.distribuida.clientes.authors.AuthorsCliente;
import com.distribuida.db.Book;
import com.distribuida.dtos.BookDto;
import com.distribuida.servicios.BookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@OpenAPIDefinition(
        info = @Info(
                title = "BookRest",
                version = "1.0.0",
                contact = @Contact(
                        name = "Stalin David Sandoval Sacoto"
                )
        )
)

public class BookRest {

    @Inject
    BookRepository bookService;

    @RestClient
    @Inject
    AuthorRestProxy proxyAuthor;

    /**
     * GET          /books/{id}     buscar un libro por ID
     * GET          /books          listar todos
     * POST         /books          insertar
     * PUT/PATCH    /books/{id}     actualizar
     * DELETE       /books/{id}     eliminar
     */

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar libro por el ID",
            description = "Se necesita el ID para buscarlo - GET")
    @APIResponse(responseCode = "200", description = "Se encontro el libro",
            content = @Content(mediaType = "application.json", schema = @Schema(implementation = Book.class)))
    @APIResponse(responseCode = "500", description = "Error del servidor, algo salio mal al procesar la solicitud")
    @APIResponse(responseCode = "400", description = "Solicitud Incorrecta")
    @RequestBody(
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Book.class),
                    examples = @ExampleObject(
                            name = "Buscar libros por id",
                            summary = "ID"
                    )))
    public Response findById(@PathParam("id") Integer id) {
        Optional<Book> ret = bookService.findById(id);

        if (ret.isPresent()) {
            return Response.ok(ret.get()).build();
        } else {
            String msg = String.format("Book[id=%d] not found.", id);

            return Response.status(Response.Status.NOT_FOUND)
                    .entity(msg)
                    .build();
        }
    }


    @GET
    @Operation(summary = "Listar todos los libros",
            description = "No se necesita algún parametro para listar - GET")
    @APIResponse(responseCode = "200", description = "Se encontro los libros",
            content = @Content(mediaType = "application.json", schema = @Schema(implementation = Book.class)))
    @APIResponse(responseCode = "500", description = "Error del servidor, algo salio mal al procesar la solicitud")
    @APIResponse(responseCode = "400", description = "Solicitud Incorrecta")
    @RequestBody(
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Book.class),
                    examples = @ExampleObject(
                            name = "Buscar todos los libros"

                    )))
    public List<Book> findAll() {
        System.out.println("Buscando todos");
        return bookService.findAll();
    }


    @GET
    @Path("/all")
    @Operation(summary = "Lista de libros completa",
            description = "Entrega una lista completa de libros, con su author - GET")
    @APIResponse(responseCode = "200", description = "Se encontro los libros con sus autores",
            content = @Content(mediaType = "application.json", schema = @Schema(implementation = Book.class)))
    @APIResponse(responseCode = "500", description = "Error del servidor, algo salio mal al procesar la solicitud")
    @APIResponse(responseCode = "400", description = "Solicitud Incorrecta")
    @RequestBody(
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Book.class),
                    examples = @ExampleObject(
                            name = "Books findAll Completo"
                    )))
    public List<BookDto> findAllCompleto() throws Exception {
        var books = bookService.findAll();
        List<BookDto> ret = books.stream()
                .map(s -> {
                    System.out.println("*********buscando " + s.getId());

                    AuthorsCliente author = proxyAuthor.findById(s.getId().longValue());
                    return new BookDto(
                            s.getId(),
                            s.getIsbn(),
                            s.getTitle(),
                            s.getAuthor(),
                            s.getPrice(),
                            String.format("%s, %s", author.getLastName(), author.getFirstName())
                    );
                })
                .collect(Collectors.toList());

        return ret;
    }

    @POST
    @Operation(summary = "Crear un libro")
    @APIResponse(description = "Crea un libro y lo manda en una peticion - POST")
    @APIResponse(responseCode = "200", description = "Se inserto el libro",
            content = @Content(mediaType = "application.json", schema = @Schema(implementation = Book.class)))
    @APIResponse(responseCode = "500", description = "Error del servidor, algo salio mal al procesar la solicitud")
    @APIResponse(responseCode = "400", description = "Solicitud Incorrecta")
    @RequestBody(
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Book.class),
                    examples = @ExampleObject(
                            name = "Insertar Books"
                    )))
    public void insert(Book book) {
        bookService.insert(book);
    }


    @PUT
    @Path("/{id}")
    @Operation(summary = "Actualizar un libro")
    @APIResponse(description = "Actualiza un libro por medio de su ID - PUT")
    @APIResponse(responseCode = "200", description = "Se actualizo el libro",
            content = @Content(mediaType = "application.json", schema = @Schema(implementation = Book.class)))
    @APIResponse(responseCode = "500", description = "Error del servidor, algo salio mal al procesar la solicitud")
    @APIResponse(responseCode = "400", description = "Solicitud Incorrecta")
    @RequestBody(
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Book.class),
                    examples = @ExampleObject(
                            name = "Actualizar books",
                            summary = "ID"
                    )))
    public void update(Book book, @PathParam("id") Integer id) {
        book.setId(id);

        bookService.update(book);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar un libro",
            description = "Elimina un libro por buscando por su id - DELETE")
    @APIResponse(responseCode = "200", description = "Se elimino el libro",
            content = @Content(mediaType = "application.json", schema = @Schema(implementation = Book.class)))
    @APIResponse(responseCode = "500", description = "Error del servidor, algo salio mal al procesar la solicitud")
    @APIResponse(responseCode = "400", description = "Solicitud Incorrecta")
    @RequestBody(
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Book.class),
                    examples = @ExampleObject(
                            name = "Eliminar Books por ID"
                    )))

    public void delete(@PathParam("id") Integer id) {
        bookService.delete(id);
    }
}
