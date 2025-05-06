package com.example.restaurante.services;

import com.example.restaurante.entities.ArticuloInsumo;
import com.example.restaurante.entities.ArticuloManufacturado;
import com.example.restaurante.entities.ArticuloManufacturadoDetalle;
import com.example.restaurante.repositories.ArticuloManufacturadoDetalleRepository;
import com.example.restaurante.repositories.ArticuloManufacturadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticuloManufacturadoService {

    private final ArticuloManufacturadoRepository articuloManufacturadoRepository;
    private final ArticuloManufacturadoDetalleRepository detalleRepository;
    private final ArticuloInsumoService articuloInsumoService;

    @Transactional(readOnly = true)
    public List<ArticuloManufacturado> findAll() {
        return articuloManufacturadoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ArticuloManufacturado findById(Long id) {
        return articuloManufacturadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Articulo Manufacturado no encontrado con ID: " + id));
    }

    @Transactional
    public ArticuloManufacturado crearArticuloManufacturado(ArticuloManufacturado articuloManufacturado) {
        // 1. Guardar el artículo manufacturado
        ArticuloManufacturado savedArticulo = articuloManufacturadoRepository.save(articuloManufacturado);

        /*
        // 2. Actualizar el stock de insumos
        for (ArticuloManufacturadoDetalle detalle : articuloManufacturado.getDetalles()) {
            detalle.setArticuloManufacturado(savedArticulo);
            detalleRepository.save(detalle);

            // Actualizar stock del insumo
            ArticuloInsumo insumo = detalle.getArticuloInsumo();
            articuloInsumoService.actualizarStock(insumo, detalle.getCantidad());
        }
         */

        return savedArticulo;
    }

    public void deleteById(Long id) {
        articuloManufacturadoRepository.deleteById(id);
    }
}