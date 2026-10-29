# Conciliación de pagos pendientes

Un pago que lleva más de 24 horas pendiente ya no se consulta automáticamente mediante `confirmTx`. Su saldo permanece retenido y `POST /pagos/consultar` muestra `requiereRevisionManual: true`.

1. Localizar los casos pendientes por UPC y fecha de envío. Consultar solo los datos necesarios, sin extraer `signed_proveedor`, tokens ni referencias personales:

   ```sql
   SELECT upc, id_servicio_proveedor, id_producto_proveedor, monto_pago,
          fecha_envio, fecha_ultima_confirmacion, codigo_proveedor,
          id_transaccion_proveedor, numero_autorizacion
   FROM gestopago_pagos
   WHERE estatus = 'PENDIENTE'
     AND fecha_envio < now() - interval '24 hours'
   ORDER BY fecha_envio;
   ```

2. Contrastar el UPC y el identificador de transacción con una constancia confiable del proveedor. Un timeout, HTTP 401/403 o ausencia de respuesta **no prueban** que el pago haya fallado.
3. Si no hay constancia concluyente, conservar `PENDIENTE` y la retención, y escalar el caso. No repetir `sendTx` ni modificar saldo o estado directamente en la base de datos.
4. Antes de automatizar un cierre manual se necesita un canal verificable de PuntoRed y una operación interna con autorización, evidencia, auditoría e idempotencia. Ese canal no está disponible en esta integración; por ello la API no expone una resolución manual que pueda mover dinero sin prueba del resultado.

Las pruebas automatizadas cubren rechazo, respuesta incierta, expiración de `confirmTx`, idempotencia y liquidación una sola vez. La confirmación real con PuntoRed requiere acceso al entorno e IP autorizada por el proveedor.
