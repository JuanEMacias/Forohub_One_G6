package com.aluracursos.forohub.controller;

import com.aluracursos.forohub.DTO.RegistrodeRespuesta;
import com.aluracursos.forohub.DTO.RespuestasData;
import com.aluracursos.forohub.model.Respuesta;
import com.aluracursos.forohub.model.Topic;
import com.aluracursos.forohub.repository.IRespuestaRepository;
import com.aluracursos.forohub.repository.ITopicoRepository;
import com.aluracursos.forohub.service.RespuestaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/respuesta")
@SecurityRequirement(name = "bearer-key")
public class RespuestaController {

    @Autowired
    private RespuestaService respuestaService;

    @Autowired
    private IRespuestaRepository respuestaRepo;

    @Autowired
    private ITopicoRepository topicoRepo;

    @SuppressWarnings("rawtypes")
    @PostMapping("/registrar")
    public ResponseEntity registrarRespuesta(@RequestBody @Valid RegistrodeRespuesta datosRegistroRespuesta, UriComponentsBuilder uriComponentsBuilder) {

        Respuesta respuesta = respuestaService.registrarServicio(datosRegistroRespuesta);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        RespuestasData datosRespuesta = new RespuestasData(
                respuesta.getId(),
                respuesta.getMensaje(),
                respuesta.getFechaCreacion().format(formatter),
                respuesta.getSolucion(),
                respuesta.getAutor().getNombre(),
                respuesta.getAutor().getPerfil(),
                respuesta.getTopico().getTitulo());

        URI url = uriComponentsBuilder.path("/respuesta/{id}").buildAndExpand(respuesta.getId()).toUri();

        return ResponseEntity.created(url).body(datosRespuesta);
    }

    @GetMapping("/listar")
    public ResponseEntity<Page<RespuestasData>> listarRespuestas(
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.ASC) Pageable paginacion) {

        Page<Respuesta> respuestas = respuestaRepo.findByStatusTrue(paginacion);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Page<RespuestasData> datosRespuesta = respuestas.map(respuesta
                -> new RespuestasData(
                        respuesta.getId(),
                        respuesta.getMensaje(),
                        respuesta.getFechaCreacion().format(formatter),
                        respuesta.getSolucion(),
                        respuesta.getAutor().getNombre(),
                        respuesta.getAutor().getPerfil(),
                        respuesta.getTopico().getTitulo()
                )
        );

        return ResponseEntity.ok().body(datosRespuesta);
    }

    @GetMapping("/listarPorTopico/{topicoId}")
    public ResponseEntity<?> listarRespuestaPorTopico(
            @PathVariable Long topicoId,
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.ASC) Pageable pageable) {

        if (topicoId != null) {
            Page<Respuesta> respuestas = respuestaService.getRespuestasPorTopico(topicoId, pageable);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            if (!respuestas.isEmpty()) {
                Page<RespuestasData> datosRespuesta = respuestas.map(respuesta -> new RespuestasData(
                        respuesta.getId(),
                        respuesta.getMensaje(),
                        respuesta.getFechaCreacion().format(formatter),
                        respuesta.getSolucion(),
                        respuesta.getAutor().getNombre(),
                        respuesta.getAutor().getPerfil(),
                        respuesta.getTopico().getTitulo()
                ));
                return ResponseEntity.ok().body(datosRespuesta);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Error, no hay resultados para su búsqueda");
            }
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error, no hay resultados para su búsqueda");
        }
    }

    @SuppressWarnings("rawtypes")
    @PutMapping("/actualizar")
    @Transactional
    public ResponseEntity actualizarTopico(@RequestBody @Valid RegistrodeRespuesta.DatosActualizarRespuestas datosActualizarRespuesta) {

        Optional<Respuesta> respOptional = respuestaRepo.findById(datosActualizarRespuesta.id());

        if (respOptional.isPresent()) {
            Respuesta respuesta = respOptional.get();
            respuesta.actualizarDatos(datosActualizarRespuesta);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return ResponseEntity.ok(new RespuestasData(
                    respuesta.getId(),
                    respuesta.getMensaje(),
                    respuesta.getFechaCreacion().format(formatter),
                    respuesta.getSolucion(),
                    respuesta.getAutor().getNombre(),
                    respuesta.getAutor().getPerfil(),
                    respuesta.getTopico().getTitulo()));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error, no hay resultados para su búsqueda");
        }
    }

    @SuppressWarnings("rawtypes")
    @DeleteMapping("/eliminar/{id}")
    @Transactional
    public ResponseEntity eliminarRespuesta(@PathVariable Long id) {
        Optional<Respuesta> respuestaOptional = respuestaRepo.findById(id);
        if (respuestaOptional.isPresent()) {
            Respuesta respuesta = respuestaOptional.get();
            respuestaRepo.deleteById(respuesta.getId());
            return ResponseEntity.ok().body("Operación exitosa");
        }

        return ResponseEntity.noContent().build();
    }

    @SuppressWarnings("rawtypes")
    @DeleteMapping("/baja/{id}")
    @Transactional
    public ResponseEntity darDeBajaRespuesta(@PathVariable Long id) {
        Optional<Respuesta> respuestaOptional = respuestaRepo.findById(id);
        if (respuestaOptional.isPresent()) {
            Respuesta respuesta = respuestaOptional.get();
            respuesta.desactivarRespuesta();
            return ResponseEntity.ok().body("Operación exitosa");
        }
        return ResponseEntity.noContent().build();
    }

   
    @SuppressWarnings("rawtypes")
    @GetMapping("/solucion/{id}")
    @Transactional
    public ResponseEntity marcarComoSolucion(@PathVariable Long id) {
        Optional<Respuesta> respuestaOptional = respuestaRepo.findById(id);
        if (respuestaOptional.isPresent()) {
            Respuesta respuesta = respuestaOptional.get();
            Long idTopico = respuesta.getTopico().getId();
            Topic topico = topicoRepo.getReferenceById(idTopico);
            if (topico.getEstaSolucionado().equals(Boolean.FALSE)) {
                respuesta.darRespuestaComoSolucion();
                topico.setEstaSolucionado(Boolean.TRUE);
                return ResponseEntity.ok().body("Operación exitosa");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Error, no hay resultados para su búsqueda");
            }
        }
        return ResponseEntity.noContent().build();
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/detalle/{id}")
    public ResponseEntity retornarDatosRespuesta(@PathVariable Long id) {
        Optional<Respuesta> optionalResp = respuestaRepo.findById(id);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if (optionalResp.isPresent()) {
            Respuesta respuesta = optionalResp.get();
            var datosRespuesta = new RespuestasData(
                    respuesta.getId(),
                    respuesta.getMensaje(),
                    respuesta.getFechaCreacion().format(formatter),
                    respuesta.getSolucion(),
                    respuesta.getAutor().getNombre(),
                    respuesta.getAutor().getPerfil(),
                    respuesta.getTopico().getTitulo());
            return ResponseEntity.ok(datosRespuesta);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error, no hay resultados para su búsqueda");
        }
    }
}