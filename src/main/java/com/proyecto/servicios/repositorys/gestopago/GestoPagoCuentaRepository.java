package com.proyecto.servicios.repositorys.gestopago;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;

@Repository
public interface GestoPagoCuentaRepository extends JpaRepository<GestoPagoCuenta, Integer> {
    @Query(value = "SELECT lpad(CAST(nextval('gestopago_numero_cuenta_seq') AS text), 20, '0')", nativeQuery = true)
    String generarNumeroCuenta();

    List<GestoPagoCuenta> findByCliente_Id(Integer clienteId);

    Optional<GestoPagoCuenta> findByNumeroCuenta(String numeroCuenta);

    List<GestoPagoCuenta> findByCliente_IdIn(Collection<Integer> clienteIds);

    Page<GestoPagoCuenta> findByEstatusAndCliente_ActivaTrue(
            String estatus, Pageable pageable);
}
