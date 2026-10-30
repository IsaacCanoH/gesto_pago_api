package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Personas;
import com.proyecto.servicios.exception.PersonaNoEncontradaException;
import com.proyecto.servicios.model.EliminaPersonaRequest;
import com.proyecto.servicios.model.PersonaResponse;
import com.proyecto.servicios.model.PersonasRequest;
import com.proyecto.servicios.repositorys.sf.PersonasRepository;
import com.proyecto.servicios.service.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonasServiceImpl implements PersonaService {
    private final PersonasRepository personasRepository;

    @Override
    public PersonaResponse creaPersona(PersonasRequest personasRequest) {
        Personas persona = new Personas();
        persona.setNombre(personasRequest.getNombre());
        persona.setApellidoP(personasRequest.getApellidoP());
        persona.setApellidoMaterno(personasRequest.getApellidoMaterno());
        personasRepository.save(persona);
        return respuesta(persona);
    }

    @Override
    public void eliminaPersona(EliminaPersonaRequest request) {
        Personas persona = personasRepository.findByNombre(request.getNombre())
                .orElseThrow(PersonaNoEncontradaException::new);
        personasRepository.delete(persona);
    }

    @Override
    public PersonaResponse actualizaPersona(PersonasRequest request) {
        Personas persona = personasRepository.findByNombre(request.getNombre())
                .orElseThrow(PersonaNoEncontradaException::new);
        persona.setApellidoP(request.getApellidoP());
        persona.setApellidoMaterno(request.getApellidoMaterno());
        personasRepository.save(persona);
        return respuesta(persona);
    }

    private PersonaResponse respuesta(Personas persona) {
        return new PersonaResponse(persona.getNombre(), persona.getApellidoP(),
                persona.getApellidoMaterno());
    }
}
