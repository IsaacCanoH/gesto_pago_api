package com.proyecto.servicios.mapper;

import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GestoPagoProductoMapper {

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "leyenda", source = "legend")
        @Mapping(target = "activo", ignore = true)
        @Mapping(target = "fechaCreacion", ignore = true)
        @Mapping(target = "fechaActualizacion", ignore = true)
        GestoPagoProducto toEntity(
                        GestoPagoProductListResponse.Producto producto);

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "leyenda", source = "legend")
        @Mapping(target = "activo", ignore = true)
        @Mapping(target = "fechaCreacion", ignore = true)
        @Mapping(target = "fechaActualizacion", ignore = true)
        void updateEntity(
                        GestoPagoProductListResponse.Producto producto,
                        @MappingTarget GestoPagoProducto entity);

        GestoPagoProductoResponse toResponse(GestoPagoProducto producto);

        List<GestoPagoProductoResponse> toResponseList(
                        List<GestoPagoProducto> productos);
}
