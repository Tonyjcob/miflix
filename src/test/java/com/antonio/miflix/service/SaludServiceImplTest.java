package com.antonio.miflix.service;

import com.antonio.miflix.dao.SaludDAO;
import com.antonio.miflix.exception.DataAccessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaludServiceImplTest {
    @Mock
    private SaludDAO saludDAO;

    @InjectMocks
    private SaludServiceImpl saludService;

    @Test
    void estaDisponible_conBaseDatosDisponible_devuelveVerdadero() {
        when(saludDAO.comprobarConexion()).thenReturn(true);

        assertTrue(saludService.estaDisponible());
    }

    @Test
    void estaDisponible_conBaseDatosCaida_devuelveFalso() {
        when(saludDAO.comprobarConexion())
                .thenThrow(new DataAccessException("Fallo de conexión.", new RuntimeException()));

        assertFalse(saludService.estaDisponible());
    }
}