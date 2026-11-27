package com.antonio.miflix.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserRangoTest {
    private final ParserRango parser = new ParserRango();

    @Test
    void parsear_conRangoCerrado_devuelveLosLimitesSolicitados() {
        RangoBytes rango = parser.parsear("bytes=10-19", 100);

        assertEquals(RangoBytes.Estado.PARCIAL, rango.getEstado());
        assertEquals(10, rango.getInicio());
        assertEquals(19, rango.getFin());
    }

    @Test
    void parsear_conRangoAbierto_usaElFinalDelRecurso() {
        RangoBytes rango = parser.parsear("bytes=50-", 100);

        assertEquals(RangoBytes.Estado.PARCIAL, rango.getEstado());
        assertEquals(50, rango.getInicio());
        assertEquals(99, rango.getFin());
    }

    @Test
    void parsear_conSufijo_devuelveLosUltimosBytesSolicitados() {
        RangoBytes rango = parser.parsear("bytes=-20", 100);

        assertEquals(RangoBytes.Estado.PARCIAL, rango.getEstado());
        assertEquals(80, rango.getInicio());
        assertEquals(99, rango.getFin());
    }

    @Test
    void parsear_conFinMayorAlTamano_ajustaElFinalAlUltimoByte() {
        RangoBytes rango = parser.parsear("bytes=90-150", 100);

        assertEquals(RangoBytes.Estado.PARCIAL, rango.getEstado());
        assertEquals(90, rango.getInicio());
        assertEquals(99, rango.getFin());
    }

    @Test
    void parsear_conInicioFueraDeLimites_devuelveNoSatisfacible() {
        assertEquals(RangoBytes.Estado.NO_SATISFACIBLE, parser.parsear("bytes=100-", 100).getEstado());
        assertEquals(RangoBytes.Estado.NO_SATISFACIBLE, parser.parsear("bytes=20-10", 100).getEstado());
        assertEquals(RangoBytes.Estado.NO_SATISFACIBLE, parser.parsear("bytes=-0", 100).getEstado());
    }

    @Test
    void parsear_conCabeceraAusenteOMalformada_devuelveRecursoCompleto() {
        assertEquals(RangoBytes.Estado.COMPLETO, parser.parsear(null, 100).getEstado());
        assertEquals(RangoBytes.Estado.COMPLETO, parser.parsear("bytes=abc-def", 100).getEstado());
        assertEquals(RangoBytes.Estado.COMPLETO, parser.parsear("bytes=0-1,4-5", 100).getEstado());
    }
}