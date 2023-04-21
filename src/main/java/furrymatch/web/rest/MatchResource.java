package furrymatch.web.rest;

import furrymatch.domain.Match;
import furrymatch.domain.User;
import furrymatch.repository.MatchRepository;
import furrymatch.service.MatchService;
import furrymatch.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
 * REST controller for managing {@link furrymatch.domain.Match}.
 */
@CrossOrigin
@RestController
@RequestMapping("/api")
public class MatchResource {

    private final Logger log = LoggerFactory.getLogger(MatchResource.class);

    private static final String ENTITY_NAME = "match";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MatchService matchService;

    private final MatchRepository matchRepository;

    public MatchResource(MatchService matchService, MatchRepository matchRepository) {
        this.matchService = matchService;
        this.matchRepository = matchRepository;
    }

    /**
     * {@code POST  /matches} : Create a new match.
     *
     * @param match the match to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new match, or with status {@code 400 (Bad Request)} if the match has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/matches")
    public ResponseEntity<Match> createMatch(@RequestBody Match match) throws URISyntaxException {
        log.debug("REST request to save Match : {}", match);
        if (match.getId() != null) {
            throw new BadRequestAlertException("A new match cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Match result = matchService.save(match);
        return ResponseEntity
            .created(new URI("/api/matches/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /matches/:id} : Updates an existing match.
     *
     * @param id the id of the match to save.
     * @param match the match to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated match,
     * or with status {@code 400 (Bad Request)} if the match is not valid,
     * or with status {@code 500 (Internal Server Error)} if the match couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/matches/{id}")
    public ResponseEntity<Match> updateMatch(@PathVariable(value = "id", required = false) final Long id, @RequestBody Match match)
        throws URISyntaxException {
        log.debug("REST request to update Match : {}, {}", id, match);
        if (match.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, match.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!matchRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Match result = matchService.update(match);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, match.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /matches/:id} : Partial updates given fields of an existing match, field will ignore if it is null
     *
     * @param id the id of the match to save.
     * @param match the match to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated match,
     * or with status {@code 400 (Bad Request)} if the match is not valid,
     * or with status {@code 404 (Not Found)} if the match is not found,
     * or with status {@code 500 (Internal Server Error)} if the match couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/matches/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Match> partialUpdateMatch(@PathVariable(value = "id", required = false) final Long id, @RequestBody Match match)
        throws URISyntaxException {
        log.debug("REST request to partial update Match partially : {}, {}", id, match);
        if (match.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, match.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!matchRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Match> result = matchService.partialUpdate(match);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, match.getId().toString())
        );
    }

    /**
     * {@code GET  /matches} : get all the matches.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of matches in body.
     */
    @GetMapping("/matches")
    public ResponseEntity<List<Match>> getAllMatches(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Matches");
        Page<Match> page = matchService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/matches/user/current")
    public ResponseEntity<Long> getCurrentUserPetId() {
        Optional<Long> petIdOpt = matchService.getCurrentUserPetId();
        if (petIdOpt.isPresent()) {
            return ResponseEntity.ok().body(petIdOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * {@code GET  /matches/:id} : get the "id" match.
     *
     * @param id the id of the match to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the match, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/matches/{id}")
    public ResponseEntity<Match> getMatch(@PathVariable Long id) {
        log.debug("REST request to get Match : {}", id);
        Optional<Match> match = matchService.findOne(id);
        return ResponseUtil.wrapOrNotFound(match);
    }

    /**
     * {@code DELETE  /matches/:id} : delete the "id" match.
     *
     * @param id the id of the match to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/matches/{id}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        log.debug("REST request to delete Match : {}", id);
        matchService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
