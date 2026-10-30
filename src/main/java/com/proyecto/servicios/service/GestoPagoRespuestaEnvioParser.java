package com.proyecto.servicios.service;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.stereotype.Component;

import com.proyecto.servicios.exception.GestoPagoExternalResponseException;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaEnvio;
import com.proyecto.servicios.model.gestopago.GestoPagoRespuestaConfirmacion;
import com.proyecto.servicios.model.gestopago.GestoPagoSendTxXmlResponse;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

@Component
public class GestoPagoRespuestaEnvioParser {
    public GestoPagoRespuestaEnvio parsear(String xml) {
        GestoPagoSendTxXmlResponse response = leer(xml);
        String codigo = response.getMensaje().getCodigo().trim();
        if ("01".equals(codigo)
                && (response.getIdTx() == null || response.getIdTx().isBlank()
                        || response.getNumeroAutorizacion() == null
                        || response.getNumeroAutorizacion().isBlank())) {
            throw new GestoPagoExternalResponseException(
                    "GestoPago confirmó el pago sin identificadores de operación");
        }
        return new GestoPagoRespuestaEnvio(
                codigo,
                response.getMensaje().getTexto(),
                response.getIdTx(),
                response.getNumeroAutorizacion());
    }

    public GestoPagoRespuestaConfirmacion parsearConfirmacion(String xml) {
        GestoPagoSendTxXmlResponse response = leer(xml);
        return new GestoPagoRespuestaConfirmacion(
                response.getMensaje().getCodigo().trim(),
                response.getMensaje().getTexto(),
                response.getMensaje().getIdTx() != null
                        ? response.getMensaje().getIdTx() : response.getIdTx(),
                response.getNumeroAutorizacion());
    }

    private GestoPagoSendTxXmlResponse leer(String xml) {
        if (xml == null || xml.isBlank() || xml.length() > 65_536) {
            throw new GestoPagoExternalResponseException("GestoPago devolvió una respuesta de pago vacía o inválida");
        }

        XMLStreamReader reader = null;
        try {
            XMLInputFactory factory = XMLInputFactory.newFactory();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            reader = factory.createXMLStreamReader(new StringReader(xml));
            GestoPagoSendTxXmlResponse response = (GestoPagoSendTxXmlResponse)
                    JAXBContext.newInstance(GestoPagoSendTxXmlResponse.class)
                            .createUnmarshaller().unmarshal(reader);

            if (response.getMensaje() == null || response.getMensaje().getCodigo() == null
                    || response.getMensaje().getCodigo().isBlank()) {
                throw new GestoPagoExternalResponseException("GestoPago no devolvió el código del pago");
            }
            return response;
        } catch (JAXBException | XMLStreamException e) {
            throw new GestoPagoExternalResponseException("No fue posible interpretar el XML del pago", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignored) {
                    // La respuesta ya fue procesada.
                }
            }
        }
    }
}
