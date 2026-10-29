package com.proyecto.servicios.repositorys.gestopago;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;

@Repository
public interface GestoPagoCuentaRepository extends JpaRepository<GestoPagoCuenta, Integer>,
        JpaSpecificationExecutor<GestoPagoCuenta> {
    @Query(value = "SELECT lpad(CAST(nextval('gestopago_numero_cuenta_seq') AS text), 20, '0')", nativeQuery = true)
    String generarNumeroCuenta();

    List<GestoPagoCuenta> findByCliente_Id(Integer clienteId);

    Optional<GestoPagoCuenta> findByNumeroCuenta(String numeroCuenta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cuenta from GestoPagoCuenta cuenta where cuenta.numeroCuenta = :numeroCuenta")
    Optional<GestoPagoCuenta> findByNumeroCuentaParaRetener(@Param("numeroCuenta") String numeroCuenta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cuenta from GestoPagoCuenta cuenta where cuenta.id = :id")
    Optional<GestoPagoCuenta> findByIdParaLiquidar(@Param("id") Integer id);

    List<GestoPagoCuenta> findByCliente_IdIn(Collection<Integer> clienteIds);

}
