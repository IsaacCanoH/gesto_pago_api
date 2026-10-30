package com.proyecto.servicios.repositorys.gestopago;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import com.proyecto.servicios.entity.gestopago.GestoPagoPago;
import com.proyecto.servicios.entity.gestopago.GestoPagoEstadoPago;

@Repository
public interface GestoPagoPagoRepository extends JpaRepository<GestoPagoPago, Long> {
    Optional<GestoPagoPago> findByUpc(String upc);

    long countByEstatusAndFechaEnvioBefore(GestoPagoEstadoPago estatus, Instant fechaLimite);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pago from GestoPagoPago pago where pago.id = :id")
    Optional<GestoPagoPago> findByIdParaActualizar(@Param("id") Long id);

    @Query("select pago.id from GestoPagoPago pago where pago.estatus = :estatus "
            + "and pago.fechaEnvio between :desde and :hasta "
            + "and (pago.fechaUltimaConfirmacion is null or pago.fechaUltimaConfirmacion <= :reintento) "
            + "order by pago.fechaEnvio asc")
    List<Long> buscarPendientesDeConfirmacion(
            @Param("estatus") GestoPagoEstadoPago estatus,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta,
            @Param("reintento") Instant reintento,
            Pageable limite);

    @Query("select pago.id from GestoPagoPago pago where pago.estatus = :estatus "
            + "and pago.fechaEnvio is null and pago.fechaCreacion <= :limite "
            + "order by pago.fechaCreacion asc")
    List<Long> buscarReservasSinEnvio(
            @Param("estatus") GestoPagoEstadoPago estatus,
            @Param("limite") Instant limite,
            Pageable cantidad);
}
