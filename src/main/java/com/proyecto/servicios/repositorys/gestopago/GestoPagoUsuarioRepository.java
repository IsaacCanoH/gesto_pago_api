package com.proyecto.servicios.repositorys.gestopago;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyecto.servicios.entity.gestopago.GestoPagoUsuario;

@Repository
public interface GestoPagoUsuarioRepository extends JpaRepository<GestoPagoUsuario, Integer> {
    @Query("select case when count(u) > 0 then true else false end "
            + "from GestoPagoUsuario u where lower(u.correo) = lower(?1)")
    boolean existsByCorreoIgnoreCase(String correo);

    @EntityGraph(attributePaths = "cliente")
    @Query("select u from GestoPagoUsuario u where lower(u.correo) = lower(?1)")
    Optional<GestoPagoUsuario> findByCorreoIgnoreCase(String correo);

    @Override
    @EntityGraph(attributePaths = "cliente")
    Optional<GestoPagoUsuario> findById(Integer id);

    Optional<GestoPagoUsuario> findByCliente_Id(Integer clienteId);

    List<GestoPagoUsuario> findByCliente_IdIn(Collection<Integer> clienteIds);
}
