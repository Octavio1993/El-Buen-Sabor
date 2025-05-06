package com.example.restaurante.services;

import com.example.restaurante.entities.ArticuloInsumo;
import com.example.restaurante.entities.ArticuloManufacturadoDetalle;
import com.example.restaurante.repositories.ArticuloInsumoRepository;
import com.example.restaurante.repositories.ArticuloManufacturadoDetalleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticuloInsumoService {

    private final ArticuloInsumoRepository articuloInsumoRepository;
    private final ArticuloManufacturadoDetalleRepository articuloManufacturadoDetalleRepository;

    @Transactional(readOnly = true)
    public List<ArticuloInsumo> findAll() {
        return articuloInsumoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ArticuloInsumo findById(Long id) {
        return articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Articulo Insumo no encontrado con ID: " + id));
    }

    @Transactional
    public ArticuloInsumo save(ArticuloInsumo articuloInsumo) {
        return articuloInsumoRepository.save(articuloInsumo);
    }

    @Transactional
    public void actualizarStock(ArticuloInsumo articuloInsumo, Integer cantidad) {
        int nuevoStock = articuloInsumo.getStockActual() - cantidad;
        if (nuevoStock < 0) {
            throw new RuntimeException("No hay suficiente stock disponible para " + articuloInsumo.getDenominacion());
        }
        articuloInsumo.setStockActual(nuevoStock);
        articuloInsumoRepository.save(articuloInsumo);
    }

    @Transactional
    public void deleteById(Long id) {
        try {
            // Primero, vamos a buscar si hay alguna relación con ArticuloManufacturadoDetalle
            // y eliminarla explícitamente

            // Esta consulta debería estar en un repositorio específico, pero la incluyo aquí para claridad
            // Obtener la lista de detalles que usan este insumo
            List<ArticuloManufacturadoDetalle> detalles = articuloManufacturadoDetalleRepository.findByArticuloInsumoId(id);

            // Eliminar cada detalle
            for (ArticuloManufacturadoDetalle detalle : detalles) {
                articuloManufacturadoDetalleRepository.delete(detalle);
            }

            // Ahora podemos eliminar el insumo
            articuloInsumoRepository.deleteById(id);

            System.out.println("Insumo eliminado correctamente!");
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el insumo: " + e.getMessage(), e);
        }
    }
}
