package com.proyecto.servicios.repositorys.gestopago;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;

@Repository
public interface GestoPagoClienteRepository extends JpaRepository<GestoPagoCliente, Integer> {
    boolean existsByCurp(String curp);

    boolean existsByRfc(String rfc);

    Optional<GestoPagoCliente> findByCurp(String curp);

    Optional<GestoPagoCliente> findByRfc(String rfc);

    Page<GestoPagoCliente> findByActivaTrue(Pageable pageable);

    Page<GestoPagoCliente> findByFechaCreacionBetween(
            LocalDate desde,
            LocalDate hasta,
            Pageable pageable);

}
