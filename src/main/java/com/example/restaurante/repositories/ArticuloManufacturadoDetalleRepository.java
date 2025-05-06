package com.example.restaurante.repositories;

import com.example.restaurante.entities.ArticuloManufacturadoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticuloManufacturadoDetalleRepository extends JpaRepository<ArticuloManufacturadoDetalle, Long> {
    List<ArticuloManufacturadoDetalle> findByArticuloInsumoId(Long articuloInsumoId);
}
