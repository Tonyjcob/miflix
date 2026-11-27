package com.antonio.miflix.dao;

import com.antonio.miflix.exception.ClaveDuplicadaException;
import com.antonio.miflix.exception.DataAccessException;
import com.antonio.miflix.exception.IntegridadReferencialException;
import com.antonio.miflix.exception.ValidacionDatosException;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class SQLExceptionTraductorTest {
    @Test
    void traducir_conError1062_devuelveClaveDuplicadaConEntidad() {
        SQLException sqlException = excepcion(1062);

        ClaveDuplicadaException traducida = assertInstanceOf(ClaveDuplicadaException.class,
                SQLExceptionTraductor.traducir(sqlException, "género"));

        assertEquals("Ya existe un/a género con esos datos", traducida.getMessage());
        assertSame(sqlException, traducida.getCause());
    }

    @Test
    void traducir_conRestriccionTituloAnio_devuelveMensajeDeTituloDuplicado() {
        SQLException sqlException = new SQLException("uq_pelicula_titulo_anio", "23000", 1062);

        ClaveDuplicadaException traducida = assertInstanceOf(ClaveDuplicadaException.class,
                SQLExceptionTraductor.traducir(sqlException, "película"));

        assertEquals("Ya existe una película con el mismo título y año.", traducida.getMessage());
        assertSame(sqlException, traducida.getCause());
    }

    @Test
    void traducir_conRestriccionRutaArchivo_devuelveMensajeDeRutaDuplicada() {
        SQLException sqlException = new SQLException("uq_pelicula_ruta_archivo", "23000", 1062);

        ClaveDuplicadaException traducida = assertInstanceOf(ClaveDuplicadaException.class,
                SQLExceptionTraductor.traducir(sqlException, "película"));

        assertEquals("Ya existe una película con esa ruta de archivo.", traducida.getMessage());
        assertSame(sqlException, traducida.getCause());
    }

    @Test
    void traducir_conDuplicadoSinRestriccionReconocida_usaMensajeGenerico() {
        SQLException sqlException = new SQLException("uq_pelicula_otra", "23000", 1062);

        ClaveDuplicadaException traducida = assertInstanceOf(ClaveDuplicadaException.class,
                SQLExceptionTraductor.traducir(sqlException, "película"));

        assertEquals("Ya existe un/a película con esos datos", traducida.getMessage());
        assertSame(sqlException, traducida.getCause());
    }

    @Test
    void traducir_conError1451_devuelveIntegridadReferencialPorDependencias() {
        SQLException sqlException = excepcion(1451);

        IntegridadReferencialException traducida = assertInstanceOf(IntegridadReferencialException.class,
                SQLExceptionTraductor.traducir(sqlException, "género"));

        assertSame(sqlException, traducida.getCause());
    }

    @Test
    void traducir_conError1452_devuelveIntegridadReferencialPorReferenciaInexistente() {
        SQLException sqlException = excepcion(1452);

        IntegridadReferencialException traducida = assertInstanceOf(IntegridadReferencialException.class,
                SQLExceptionTraductor.traducir(sqlException, "género"));

        assertSame(sqlException, traducida.getCause());
    }

    @Test
    void traducir_conError3819_devuelveValidacionDatosException() {
        SQLException sqlException = excepcion(3819);

        ValidacionDatosException traducida = assertInstanceOf(ValidacionDatosException.class,
                SQLExceptionTraductor.traducir(sqlException, "género"));

        assertSame(sqlException, traducida.getCause());
    }

    @Test
    void traducir_conErrorDesconocido_devuelveDataAccessExceptionGenerica() {
        SQLException sqlException = excepcion(9999);

        DataAccessException traducida = SQLExceptionTraductor.traducir(sqlException, "género");

        assertEquals(DataAccessException.class, traducida.getClass());
        assertEquals("No se pudo completar la operación sobre género.", traducida.getMessage());
        assertSame(sqlException, traducida.getCause());
    }

    private static SQLException excepcion(int codigoError) {
        return new SQLException("Error simulado", "23000", codigoError);
    }
}