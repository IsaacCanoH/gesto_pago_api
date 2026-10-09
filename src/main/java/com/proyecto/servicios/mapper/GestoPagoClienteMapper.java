package com.proyecto.servicios.mapper;

import com.proyecto.servicios.entity.gestopago.GestoPagoCliente;
import com.proyecto.servicios.entity.gestopago.GestoPagoCuenta;
import com.proyecto.servicios.entity.gestopago.GestoPagoDomicilio;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteRegistroRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoCuentaConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoDomicilioConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoDomicilioParcialRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoDomicilioRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteActualizacionRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteConsultaResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteResumenResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoClienteParcialRequest;

import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GestoPagoClienteMapper {

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "activa", constant = "true")
        @Mapping(target = "fechaCreacion", ignore = true)
        @Mapping(target = "fechaActualizacion", ignore = true)
        GestoPagoCliente toCliente(GestoPagoClienteRegistroRequest request);

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "cliente", ignore = true)
        GestoPagoDomicilio toDomicilio(GestoPagoDomicilioRequest request);

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "curp", ignore = true)
        @Mapping(target = "rfc", ignore = true)
        @Mapping(target = "activa", ignore = true)
        @Mapping(target = "fechaCreacion", ignore = true)
        @Mapping(target = "fechaActualizacion", ignore = true)
        void actualizarCliente(
                        GestoPagoClienteActualizacionRequest request,
                        @MappingTarget GestoPagoCliente cliente);

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "cliente", ignore = true)
        void actualizarDomicilio(
                        GestoPagoDomicilioRequest request,
                        @MappingTarget GestoPagoDomicilio domicilio);

        @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
        @Mapping(target = "id", ignore = true)
        @Mapping(target = "curp", ignore = true)
        @Mapping(target = "rfc", ignore = true)
        @Mapping(target = "activa", ignore = true)
        @Mapping(target = "fechaCreacion", ignore = true)
        @Mapping(target = "fechaActualizacion", ignore = true)
        void actualizarClienteParcial(
                        GestoPagoClienteParcialRequest request,
                        @MappingTarget GestoPagoCliente cliente);

        @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
        @Mapping(target = "id", ignore = true)
        @Mapping(target = "cliente", ignore = true)
        void actualizarDomicilioParcial(
                        GestoPagoDomicilioParcialRequest request,
                        @MappingTarget GestoPagoDomicilio domicilio);

        @Mapping(target = "domicilio", ignore = true)
        @Mapping(target = "cuentas", ignore = true)
        @Mapping(target = "correo", ignore = true)
        GestoPagoClienteConsultaResponse toConsultaCliente(GestoPagoCliente cliente);

        default GestoPagoClienteResumenResponse toResumenCliente(GestoPagoCliente cliente, String correo) {
                return new GestoPagoClienteResumenResponse(
                                cliente.getId(),
                                cliente.getNombre(),
                                cliente.getApellidoPaterno(),
                                cliente.getApellidoMaterno(),
                                correo,
                                cliente.isActiva(),
                                cliente.getFechaCreacion());
        }

        GestoPagoDomicilioConsultaResponse toConsultaDomicilio(GestoPagoDomicilio domicilio);

        GestoPagoCuentaConsultaResponse toConsultaCuenta(GestoPagoCuenta cuenta);
}
