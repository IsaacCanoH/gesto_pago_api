package com.proyecto.servicios.repositorys.gestopago;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;

@Repository
public interface GestoPagoClienteRepository extends JpaRepository<GestoPagoCliente, Integer>,
        JpaSpecificationExecutor<GestoPagoCliente> {
    boolean existsByCurp(String curp);

    boolean existsByRfc(String rfc);

}
