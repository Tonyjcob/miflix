package com.antonio.miflix.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigTest {
    private static final Map<String, String> VARIABLES_VALIDAS = Map.of(
            "DB_URL", "jdbc:mysql://localhost/miflix",
            "DB_USER", "usuario",
            "DB_PASSWORD", "clave",
            "MEDIA_PATH", "media");

    @ParameterizedTest
    @MethodSource("variablesObligatorias")
    void crear_conVariableFaltante_lanzaExcepcionConMensajeClaro(String variableFaltante) {
        Map<String, String> variables = new HashMap<>(VARIABLES_VALIDAS);
        variables.remove(variableFaltante);

        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> new AppConfig(variables));

        assertTrue(excepcion.getMessage().contains(variableFaltante));
    }

    private static Stream<String> variablesObligatorias() {
        return Stream.of("DB_URL", "DB_USER", "DB_PASSWORD", "MEDIA_PATH");
    }
}