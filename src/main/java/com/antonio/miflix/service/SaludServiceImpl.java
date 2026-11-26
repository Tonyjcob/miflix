package com.antonio.miflix.service;

import com.antonio.miflix.dao.SaludDAO;
import com.antonio.miflix.exception.DataAccessException;
import jakarta.inject.Inject;

public class SaludServiceImpl implements SaludService {
    private final SaludDAO saludDAO;

    @Inject
    public SaludServiceImpl(SaludDAO saludDAO) {
        this.saludDAO = saludDAO;
    }

    @Override
    public boolean estaDisponible() {
        try {
            return saludDAO.comprobarConexion();
        } catch (DataAccessException excepcion) {
            return false;
        }
    }
}