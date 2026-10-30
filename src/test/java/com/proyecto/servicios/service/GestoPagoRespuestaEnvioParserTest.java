package com.proyecto.servicios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.proyecto.servicios.exception.GestoPagoExternalResponseException;

class GestoPagoRespuestaEnvioParserTest {
    private final GestoPagoRespuestaEnvioParser parser = new GestoPagoRespuestaEnvioParser();

    @Test
    void interpretaExitoSoloConCodigo01() {
        var resultado = parser.parsear("""
                <RESPONSE><ID_TX>291620941</ID_TX><NUM_AUTORIZACION>100074211</NUM_AUTORIZACION>
                <MENSAJE><CODIGO>01</CODIGO><TEXTO>Operacion realizada con exito</TEXTO></MENSAJE></RESPONSE>
                """);

        assertTrue(resultado.exitosa());
        assertEquals("291620941", resultado.idTransaccion());
        assertEquals("100074211", resultado.numeroAutorizacion());
    }

    @Test
    void noConfundeHttp200ConExitoDelProveedor() {
        var resultado = parser.parsear("""
                <RESPONSE><MENSAJE><CODIGO>02</CODIGO><TEXTO>Saldo insuficiente</TEXTO></MENSAJE></RESPONSE>
                """);

        assertFalse(resultado.exitosa());
        assertEquals("02", resultado.codigo());
        assertEquals("Saldo insuficiente", resultado.mensaje());
        assertFalse(resultado.requiereConfirmacion());
    }

    @Test
    void codigo06NoSeTrataComoExitoNuevo() {
        var resultado = parser.parsear("""
                <RESPONSE><MENSAJE><CODIGO>06</CODIGO><TEXTO>Transaccion aplicada anteriormente</TEXTO>
                </MENSAJE></RESPONSE>
                """);

        assertFalse(resultado.exitosa());
        assertTrue(resultado.requiereConfirmacion());
    }

    @Test
    void codigoNoDocumentadoTambienRequiereConfirmacion() {
        var resultado = parser.parsear("<RESPONSE><MENSAJE><CODIGO>55</CODIGO></MENSAJE></RESPONSE>");

        assertFalse(resultado.exitosa());
        assertFalse(resultado.rechazada());
        assertTrue(resultado.requiereConfirmacion());
    }

    @Test
    void rechazaXmlSinCodigoYEntidadesExternas() {
        assertThrows(GestoPagoExternalResponseException.class,
                () -> parser.parsear("<RESPONSE><MENSAJE/></RESPONSE>"));
        assertThrows(GestoPagoExternalResponseException.class,
                () -> parser.parsear("""
                        <!DOCTYPE RESPONSE [<!ENTITY externo SYSTEM "file:///etc/passwd">]>
                        <RESPONSE><MENSAJE><CODIGO>&externo;</CODIGO></MENSAJE></RESPONSE>
                        """));
    }

    @Test
    void exitoSinIdentificadoresQuedaPendienteDeConfirmacion() {
        assertThrows(GestoPagoExternalResponseException.class,
                () -> parser.parsear("""
                        <RESPONSE><MENSAJE><CODIGO>01</CODIGO><TEXTO>Correcto</TEXTO></MENSAJE></RESPONSE>
                        """));
    }

    @Test
    void confirmacion06AplicadaUsaIdTxAnidado() {
        var resultado = parser.parsearConfirmacion("""
                <RESPONSE><NUM_AUTORIZACION>100024988</NUM_AUTORIZACION>
                <MENSAJE><CODIGO>06</CODIGO><TEXTO>Aplicada</TEXTO>
                <ID_TX>291437188</ID_TX></MENSAJE></RESPONSE>
                """);

        assertTrue(resultado.aplicada());
        assertEquals("291437188", resultado.idTransaccion());
    }

    @Test
    void confirmacion70LiberaY06SinDatosSigueIncierta() {
        var rechazada = parser.parsearConfirmacion("""
                <RESPONSE><NUM_AUTORIZACION>0</NUM_AUTORIZACION>
                <MENSAJE><CODIGO>70</CODIGO><TEXTO>No aplicada</TEXTO>
                <ID_TX>-1</ID_TX></MENSAJE></RESPONSE>
                """);
        var incompleta = parser.parsearConfirmacion("""
                <RESPONSE><MENSAJE><CODIGO>06</CODIGO></MENSAJE></RESPONSE>
                """);

        assertTrue(rechazada.noAplicada());
        assertFalse(incompleta.aplicada());
        assertFalse(incompleta.noAplicada());
    }
}
