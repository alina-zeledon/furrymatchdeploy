package furrymatch.web.rest;

import furrymatch.domain.Pet;
import furrymatch.domain.SearchCriteria;
import furrymatch.repository.SearchCriteriaRepository;
import furrymatch.service.PetService;
import furrymatch.service.SearchCriteriaService;
import furrymatch.service.UserService;
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
 * REST controller for managing {@link furrymatch.domain.SearchCriteria}.
 */
@RestController
@RequestMapping("/api")
public class SearchCriteriaResource {

    private final Logger log = LoggerFactory.getLogger(SearchCriteriaResource.class);

    private static final String ENTITY_NAME = "searchCriteria";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SearchCriteriaService searchCriteriaService;

    private final SearchCriteriaRepository searchCriteriaRepository;

    private final UserService userService;

    private final PetService petService;

    public SearchCriteriaResource(
        SearchCriteriaService searchCriteriaService,
        PetService petService,
        SearchCriteriaRepository searchCriteriaRepository,
        UserService userService
    ) {
        this.searchCriteriaService = searchCriteriaService;
        this.searchCriteriaRepository = searchCriteriaRepository;
        this.userService = userService;
        this.petService = petService;
    }

    /**
     * {@code POST  /search-criteria} : Create a new searchCriteria.
     *
     * @param searchCriteria the searchCriteria to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new searchCriteria, or with status {@code 400 (Bad Request)} if the searchCriteria has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/search-criteria")
    public ResponseEntity<SearchCriteria> createSearchCriteria(@Valid @RequestBody SearchCriteria searchCriteria)
        throws URISyntaxException {
        log.debug("REST request to save SearchCriteria : {}", searchCriteria);
        if (searchCriteria.getId() != null) {
            throw new BadRequestAlertException("A new searchCriteria cannot already have an ID", ENTITY_NAME, "idexists");
        }
        SearchCriteria result = searchCriteriaService.save(searchCriteria);
        return ResponseEntity
            .created(new URI("/api/search-criteria/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /search-criteria/:id} : Updates an existing searchCriteria.
     *
     * @param id the id of the searchCriteria to save.
     * @param searchCriteria the searchCriteria to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated searchCriteria,
     * or with status {@code 400 (Bad Request)} if the searchCriteria is not valid,
     * or with status {@code 500 (Internal Server Error)} if the searchCriteria couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/search-criteria/{id}")
    public ResponseEntity<SearchCriteria> updateSearchCriteria(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SearchCriteria searchCriteria
    ) throws URISyntaxException {
        log.debug("REST request to update SearchCriteria : {}, {}", id, searchCriteria);
        if (searchCriteria.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, searchCriteria.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!searchCriteriaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        SearchCriteria result = searchCriteriaService.update(searchCriteria);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, searchCriteria.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /search-criteria/:id} : Partial updates given fields of an existing searchCriteria, field will ignore if it is null
     *
     * @param id the id of the searchCriteria to save.
     * @param searchCriteria the searchCriteria to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated searchCriteria,
     * or with status {@code 400 (Bad Request)} if the searchCriteria is not valid,
     * or with status {@code 404 (Not Found)} if the searchCriteria is not found,
     * or with status {@code 500 (Internal Server Error)} if the searchCriteria couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/search-criteria/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SearchCriteria> partialUpdateSearchCriteria(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SearchCriteria searchCriteria
    ) throws URISyntaxException {
        log.debug("REST request to partial update SearchCriteria partially : {}, {}", id, searchCriteria);
        if (searchCriteria.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, searchCriteria.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!searchCriteriaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SearchCriteria> result = searchCriteriaService.partialUpdate(searchCriteria);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, searchCriteria.getId().toString())
        );
    }

    /**
     * {@code GET  /search-criteria} : get all the searchCriteria.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of searchCriteria in body.
     */
    @GetMapping("/search-criteria")
    public ResponseEntity<List<SearchCriteria>> getAllSearchCriteria(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of SearchCriteria");
        Page<SearchCriteria> page = searchCriteriaService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /search-criteria/:id} : get the "id" searchCriteria.
     *
     * @param id the id of the searchCriteria to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the searchCriteria, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/search-criteria/{id}")
    public ResponseEntity<SearchCriteria> getSearchCriteria(@PathVariable Long id) {
        log.debug("REST request to get SearchCriteria : {}", id);
        Optional<SearchCriteria> searchCriteria = searchCriteriaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(searchCriteria);
    }

    @GetMapping("/search-criteria/petId/{id}")
    public ResponseEntity<SearchCriteria> getSearchCriteriaByPetId(@PathVariable Long id) {
        log.debug("REST request to get SearchCriteria : {}", id);
        Optional<SearchCriteria> searchCriteria = Optional.ofNullable(searchCriteriaService.findByPetId(id));
        return ResponseUtil.wrapOrNotFound(searchCriteria);
    }

    @GetMapping("/search-criteria/user")
    public ResponseEntity<SearchCriteria> getSearchCriteriaUser() {
        Optional<SearchCriteria> searchCriteria = searchCriteriaService.findByOwnerUser();
        return ResponseUtil.wrapOrNotFound(searchCriteria);
    }

    /**
     * {@code DELETE  /search-criteria/:id} : delete the "id" searchCriteria.
     *
     * @param id the id of the searchCriteria to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/search-criteria/{id}")
    public ResponseEntity<Void> deleteSearchCriteria(@PathVariable Long id) {
        log.debug("REST request to delete SearchCriteria : {}", id);
        searchCriteriaService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
