package furrymatch.web.rest;

import furrymatch.domain.Pet;
import furrymatch.domain.SearchCriteria;
import furrymatch.repository.PetRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link furrymatch.domain.Pet}.
 */
@RestController
@RequestMapping("/api")
public class PetResource {

    private final Logger log = LoggerFactory.getLogger(PetResource.class);

    private static final String ENTITY_NAME = "pet";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PetService petService;

    private final UserService userService;

    private final PetRepository petRepository;

    private final SearchCriteriaService searchCriteriaService;

    public PetResource(
        PetService petService,
        PetRepository petRepository,
        UserService userService,
        SearchCriteriaService searchCriteriaService
    ) {
        this.petService = petService;
        this.petRepository = petRepository;
        this.userService = userService;
        this.searchCriteriaService = searchCriteriaService;
    }

    /**
     * {@code POST  /pets} : Create a new pet.
     *
     * @param pet the pet to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new pet, or with status {@code 400 (Bad Request)} if the pet has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/pets")
    public ResponseEntity<Pet> createPet(@Valid @RequestBody Pet pet) throws URISyntaxException {
        log.debug("REST request to save Pet : {}", pet);
        if (pet.getId() != null) {
            throw new BadRequestAlertException("A new pet cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Pet result = petService.save(pet);
        Long id = userService.getUserWithAuthorities().get().getId();
        userService.updateUserSelectedPet(result.getId(), id);
        return ResponseEntity
            .created(new URI("/api/pets/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /*
    @PostMapping("/custom-search")
    public ResponseEntity<List<Pet>> searchPets(@RequestBody SearchCriteria searchCriteria) throws URISyntaxException  {
        if (searchCriteria.getId() == null) {
            throw new BadRequestAlertException("Search Criteria ID cannot be null", ENTITY_NAME, "idexists");
        }
        log.debug("REST request to get a page of Pets based on search criteria");

        List<Pet> pets = petService.searchPets(searchCriteria);
        return ResponseEntity.ok().body(pets);
    }
*/

    /**
     * {@code PUT  /pets/:id} : Updates an existing pet.
     *
     * @param id the id of the pet to save.
     * @param pet the pet to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated pet,
     * or with status {@code 400 (Bad Request)} if the pet is not valid,
     * or with status {@code 500 (Internal Server Error)} if the pet couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/pets/{id}")
    public ResponseEntity<Pet> updatePet(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody Pet pet)
        throws URISyntaxException {
        log.debug("REST request to update Pet : {}, {}", id, pet);
        if (pet.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, pet.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!petRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Pet result = petService.update(pet);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, pet.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /pets/:id} : Partial updates given fields of an existing pet, field will ignore if it is null
     *
     * @param id the id of the pet to save.
     * @param pet the pet to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated pet,
     * or with status {@code 400 (Bad Request)} if the pet is not valid,
     * or with status {@code 404 (Not Found)} if the pet is not found,
     * or with status {@code 500 (Internal Server Error)} if the pet couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/pets/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Pet> partialUpdatePet(@PathVariable(value = "id", required = false) final Long id, @NotNull @RequestBody Pet pet)
        throws URISyntaxException {
        log.debug("REST request to partial update Pet partially : {}, {}", id, pet);
        if (pet.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, pet.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!petRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Pet> result = petService.partialUpdate(pet);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, pet.getId().toString())
        );
    }

    /**
     * {@code GET  /pets} : get all the pets.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of pets in body.
     */
    @GetMapping("/pets")
    public ResponseEntity<List<Pet>> getAllPets(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        // log.debug("REST request to get a page of Pets");
        // Page<Pet> page = petService.findAll(pageable);
        Long id = userService.getUserWithAuthorities().get().getId();
        List<Pet> page = petService.findAll(id);
        //HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(),page);
        return ResponseEntity.ok().body(page);
    }

    @GetMapping("/pets/search")
    public ResponseEntity<List<Pet>> searchPets(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get pets based on search criteria");
        //String petId = userService.getUserWithAuthorities().get().getImageUrl();
        //Current Pet In Session
        String petId = String.valueOf(petService.getCurrentUserPetId());
        System.out.println("******************************************");
        System.out.println("PET ID FROM searchPets function " + petId);
        Long ownerId = userService.getUserWithAuthorities().get().getId();
        log.debug("PET ID: ", petId);
        SearchCriteria searchCriteria = searchCriteriaService.findByPetId(Long.valueOf(petId));
        List<Pet> pets = petService.searchPets(searchCriteria, ownerId);
        return ResponseEntity.ok().body(pets);
    }

    /**
     * {@code GET  /pets/:id} : get the "id" pet.
     *
     * @param id the id of the pet to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the pet, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/pets/{id}")
    public ResponseEntity<Pet> getPet(@PathVariable Long id) {
        log.debug("REST request to get Pet : {}", id);
        Optional<Pet> pet = petService.findOne(id);
        return ResponseUtil.wrapOrNotFound(pet);
    }

    /**
     * {@code DELETE  /pets/:id} : delete the "id" pet.
     *
     * @param id the id of the pet to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/pets/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        log.debug("REST request to delete Pet : {}", id);
        petService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @GetMapping("/pets/current")
    public ResponseEntity<Long> getPetInSession() {
        Optional<Long> petIdOpt = petService.getPetId();
        System.out.println("PET ID FROM BACKEND " + petIdOpt);
        if (petIdOpt.isPresent()) {
            return ResponseEntity.ok().body(petIdOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
