package com.proyecto.servicios.repositorys.gestopago;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.servicios.entity.gestopago.GestoPagoDomicilio;

@Repository 
public interface GestoPagoDomicilioRepository extends JpaRepository<GestoPagoDomicilio, Integer> {
    Optional<GestoPagoDomicilio> findByCliente_Id(Integer clienteId);   

    List<GestoPagoDomicilio> findByCliente_IdIn(Collection<Integer> clienteIds);
}
