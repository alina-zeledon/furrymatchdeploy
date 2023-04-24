package furrymatch.web.rest;

import furrymatch.domain.Likee;
import furrymatch.repository.LikeeRepository;
import furrymatch.service.LikeeService;
import furrymatch.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link furrymatch.domain.Likee}.
 */
@RestController
@RequestMapping("/api")
public class LikeeResource {

    private final Logger log = LoggerFactory.getLogger(LikeeResource.class);

    private static final String ENTITY_NAME = "likee";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final LikeeService likeeService;

    private final LikeeRepository likeeRepository;

    public LikeeResource(LikeeService likeeService, LikeeRepository likeeRepository) {
        this.likeeService = likeeService;
        this.likeeRepository = likeeRepository;
    }

    /**
     * {@code POST  /likees} : Create a new likee.
     *
     * @param likee the likee to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new likee, or with status {@code 400 (Bad Request)} if the likee has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/likees")
    public ResponseEntity<Likee> createLikee(@Valid @RequestBody Likee likee) throws URISyntaxException {
        log.debug("REST request to save Likee : {}", likee);
        if (likee.getId() != null) {
            throw new BadRequestAlertException("A new likee cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Likee result = likeeService.save(likee);
        //likeeService.isMatch(likee);
        return ResponseEntity
            .created(new URI("/api/likees/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /likees/:id} : Updates an existing likee.
     *
     * @param id the id of the likee to save.
     * @param likee the likee to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated likee,
     * or with status {@code 400 (Bad Request)} if the likee is not valid,
     * or with status {@code 500 (Internal Server Error)} if the likee couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/likees/{id}")
    public ResponseEntity<Likee> updateLikee(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody Likee likee)
        throws URISyntaxException {
        log.debug("REST request to update Likee : {}, {}", id, likee);
        if (likee.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, likee.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!likeeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Likee result = likeeService.update(likee);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, likee.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /likees/:id} : Partial updates given fields of an existing likee, field will ignore if it is null
     *
     * @param id the id of the likee to save.
     * @param likee the likee to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated likee,
     * or with status {@code 400 (Bad Request)} if the likee is not valid,
     * or with status {@code 404 (Not Found)} if the likee is not found,
     * or with status {@code 500 (Internal Server Error)} if the likee couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/likees/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Likee> partialUpdateLikee(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Likee likee
    ) throws URISyntaxException {
        log.debug("REST request to partial update Likee partially : {}, {}", id, likee);
        if (likee.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, likee.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!likeeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Likee> result = likeeService.partialUpdate(likee);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, likee.getId().toString())
        );
    }

    /**
     * {@code GET  /likees} : get all the likees.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of likees in body.
     */
    @GetMapping("/likees")
    public ResponseEntity<List<Likee>> getAllLikees(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Likees");
        Page<Likee> page = likeeService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /likees/:id} : get the "id" likee.
     *
     * @param id the id of the likee to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the likee, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/likees/{id}")
    public ResponseEntity<Likee> getLikee(@PathVariable Long id) {
        log.debug("REST request to get Likee : {}", id);
        Optional<Likee> likee = likeeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(likee);
    }

    @GetMapping("/likees/isMatch/{firstPetId}/{secondPetId}")
    public ResponseEntity<Boolean> isMatch(@PathVariable Long firstPetId, @PathVariable Long secondPetId) {
        log.debug("REST request to check if both pets liked each other: {}, {}", firstPetId, secondPetId);
        boolean isMatch = likeeService.checkIfBothPetsLikedEachOther(firstPetId, secondPetId);
        return ResponseEntity.ok().body(isMatch);
    }

    /**
     * {@code DELETE  /likees/:id} : delete the "id" likee.
     *
     * @param id the id of the likee to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/likees/{id}")
    public ResponseEntity<Void> deleteLikee(@PathVariable Long id) {
        log.debug("REST request to delete Likee : {}", id);
        likeeService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @PostMapping("/likees/match")
    public ResponseEntity<Long> createLikeMatch(@Valid @RequestBody Likee likee) throws URISyntaxException {
        log.debug("REST request to save Likee : {}", likee);
        if (likee.getId() != null) {
            throw new BadRequestAlertException("A new likee cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Likee result = likeeService.save(likee);
        Long matchId = likeeService.checkIfMatch(likee);
        return ResponseEntity
            .created(new URI("/api/likee/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(matchId);
    }
}
