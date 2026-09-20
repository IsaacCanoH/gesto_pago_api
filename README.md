# Sincronización de productos GestoPago

## Objetivo

Se implementó la consulta diaria de productos de GestoPago. La aplicación obtiene un token válido, consume la lista de productos, interpreta la respuesta XML y guarda la información en la base de datos para mantenerla actualizada.

## Solución implementada

- La autenticación se realiza con las credenciales configuradas en la aplicación. El token generado se guarda en `gestopago_tokens` y se reutiliza mientras esté activo; no existe un token fijo en el código fuente.
- La lista se consulta mediante `GET /sistema/service/getProductList.do` enviando el header `Authorization: Bearer <token>`.
- La respuesta XML se convierte a DTOs con JAXB. Los productos se insertan o actualizan por `idProducto` en la tabla `gestopago_productos`.
- También existe un endpoint manual, `POST /gestopago/productos/sincronizar`, y un job programado para ejecutarse diariamente a las 12:00, hora de Ciudad de México.

## Configuración

Las propiedades `gestopago.*` definen la URL base, endpoints, credenciales, horario del job y timeouts de Feign. No se documentan valores reales de credenciales; para un ambiente productivo deben proporcionarse mediante variables de entorno o un gestor de secretos.

## Manejo de errores y concurrencia

Los errores de comunicación, timeout, autenticación y respuestas no exitosas se traducen a excepciones controladas. Si GestoPago responde `401` o `403`, se renueva el token y se reintenta la consulta una sola vez. Los logs registran el inicio, resultado y tipo de error sin imprimir tokens, contraseñas ni solicitudes completas.

Para evitar consultas duplicadas ante solicitudes simultáneas, solo se permite una sincronización a la vez. Las solicitudes adicionales reciben HTTP `429` con un mensaje controlado.

## Decisiones técnicas

Se utilizó OpenFeign para la integración HTTP, JAXB para el XML, MapStruct para el mapeo y JPA/Flyway para la persistencia y migraciones. El token se persiste en base de datos para no autenticar en cada consulta, y la lógica de errores de Feign se centralizó para evitar duplicación.

## Pruebas

Las pruebas unitarias de `GestoPagoProductServiceImpl` cubren la sincronización exitosa, respuesta XML no exitosa, renovación y reintento de token, y timeout de comunicación.

Para ejecutarlas:

```powershell
.\gradlew.bat test
```
