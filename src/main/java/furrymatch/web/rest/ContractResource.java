package furrymatch.web.rest;

import furrymatch.domain.*;
import furrymatch.repository.ContractRepository;
import furrymatch.repository.UserRepository;
import furrymatch.security.SecurityUtils;
import furrymatch.service.*;
import furrymatch.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
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
 * REST controller for managing {@link furrymatch.domain.Contract}.
 */
@RestController
@RequestMapping("/api")
public class ContractResource {

    private final Logger log = LoggerFactory.getLogger(ContractResource.class);

    private static final String ENTITY_NAME = "contract";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ContractService contractService;

    private final ContractRepository contractRepository;

    private final MatchService matchService;

    private final MailService mailService;

    private final OwnerService ownerService;

    private final UserService userService;

    private final UserRepository userRepository;

    private Match match;

    private Owner owner2;

    public ContractResource(
        ContractService contractService,
        ContractRepository contractRepository,
        MatchService matchService,
        MailService mailService,
        OwnerService ownerService,
        UserService userService,
        UserRepository userRepository
    ) {
        this.contractService = contractService;
        this.contractRepository = contractRepository;
        this.matchService = matchService;
        this.mailService = mailService;
        this.ownerService = ownerService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * {@code POST  /contracts} : Create a new contract.
     *
     * @param contract the contract to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new contract, or with status {@code 400 (Bad Request)} if the contract has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/contracts")
    public ResponseEntity<Contract> createContract(@RequestBody Contract contract) throws URISyntaxException {
        log.debug("REST request to save Contract : {}", contract);
        if (contract.getId() != null) {
            throw new BadRequestAlertException("A new contract cannot already have an ID", ENTITY_NAME, "idexists");
        }
        User user = userService.getUserWithAuthorities().get();
        Owner owner1 = ownerService.findOne(user.getId()).get();

        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(userr -> {
                owner2 =
                    ownerService
                        .findOne(
                            Long.valueOf(
                                (userr.getLastName()).substring((userr.getLastName()).indexOf(",") + 1, (userr.getLastName()).indexOf("-"))
                            )
                        )
                        .get();
                match = matchService.findOne(Long.valueOf((userr.getLastName()).substring(0, (userr.getLastName()).indexOf(",")))).get();
            });

        String other = contract.getOtherNotes() + ";" + owner1.getId() + ";1";
        contract.setOtherNotes(other);
        Contract result = contractService.save(contract);

        match.setContract(result);
        matchService.update(match);

        mailService.sendContractMail(owner1, owner2, contract, user.getEmail());
        return ResponseEntity
            .created(new URI("/api/contracts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @GetMapping("/contracts/sendEmail/{id}")
    public ResponseEntity<Contract> sendContract(@PathVariable(value = "id", required = false) final Long id) throws URISyntaxException {
        User user = userService.getUserWithAuthorities().get();
        Owner owner1 = ownerService.findOne(user.getId()).get();

        Contract contract = contractService.findOne(id).get();

        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(userr -> {
                owner2 =
                    ownerService
                        .findOne(
                            Long.valueOf(
                                (userr.getLastName()).substring((userr.getLastName()).indexOf(",") + 1, (userr.getLastName()).indexOf("-"))
                            )
                        )
                        .get();
            });

        User user2 = userService.findOne(owner2.getId()).get();

        String other = (contract.getOtherNotes()).substring(0, contract.getOtherNotes().indexOf(';')) + ";" + owner1.getId() + ";2";
        contract.setOtherNotes(other);

        Contract result = contractService.update(contract);

        mailService.sendContractMail(owner1, owner2, contract, user2.getEmail());
        return ResponseEntity.ok().body(contract);
    }

    /**
     * {@code PUT  /contracts/:id} : Updates an existing contract.
     *
     * @param id the id of the contract to save.
     * @param contract the contract to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated contract,
     * or with status {@code 400 (Bad Request)} if the contract is not valid,
     * or with status {@code 500 (Internal Server Error)} if the contract couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/contracts/{id}")
    public ResponseEntity<Contract> updateContract(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Contract contract
    ) throws URISyntaxException {
        log.debug("REST request to update Contract : {}, {}", id, contract);
        if (contract.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, contract.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!contractRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        User user = userService.getUserWithAuthorities().get();
        Owner owner1 = ownerService.findOne(user.getId()).get();

        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(userr -> {
                owner2 =
                    ownerService
                        .findOne(
                            Long.valueOf(
                                (userr.getLastName()).substring((userr.getLastName()).indexOf(",") + 1, (userr.getLastName()).indexOf("-"))
                            )
                        )
                        .get();
            });

        String other = contract.getOtherNotes() + ";" + owner1.getId() + ";1";
        contract.setOtherNotes(other);

        Contract result = contractService.update(contract);

        mailService.sendContractMail(owner1, owner2, contract, user.getEmail());
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, contract.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /contracts/:id} : Partial updates given fields of an existing contract, field will ignore if it is null
     *
     * @param id the id of the contract to save.
     * @param contract the contract to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated contract,
     * or with status {@code 400 (Bad Request)} if the contract is not valid,
     * or with status {@code 404 (Not Found)} if the contract is not found,
     * or with status {@code 500 (Internal Server Error)} if the contract couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/contracts/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Contract> partialUpdateContract(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Contract contract
    ) throws URISyntaxException {
        log.debug("REST request to partial update Contract partially : {}, {}", id, contract);
        if (contract.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, contract.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!contractRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Contract> result = contractService.partialUpdate(contract);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, contract.getId().toString())
        );
    }

    /**
     * {@code GET  /contracts} : get all the contracts.
     *
     * @param pageable the pagination information.
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of contracts in body.
     */
    @GetMapping("/contracts")
    public ResponseEntity<List<Contract>> getAllContracts(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        @RequestParam(required = false) String filter
    ) {
        if ("match-is-null".equals(filter)) {
            log.debug("REST request to get all Contracts where match is null");
            return new ResponseEntity<>(contractService.findAllWhereMatchIsNull(), HttpStatus.OK);
        }
        log.debug("REST request to get a page of Contracts");
        Page<Contract> page = contractService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /contracts/:id} : get the "id" contract.
     *
     * @param id the id of the contract to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the contract, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/contracts/{id}")
    public ResponseEntity<Contract> getContract(@PathVariable Long id) {
        log.debug("REST request to get Contract : {}", id);
        Optional<Contract> contract = contractService.findOne(id);
        return ResponseUtil.wrapOrNotFound(contract);
    }

    /**
     * {@code DELETE  /contracts/:id} : delete the "id" contract.
     *
     * @param id the id of the contract to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/contracts/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        log.debug("REST request to delete Contract : {}", id);
        contractService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @GetMapping("/contracts/matched-pets-no-contract/{currentPetId}")
    public ResponseEntity<List<Object[]>> getMatchedPetsWithNoContract(@PathVariable Long currentPetId) {
        List<Object[]> matchedPets = contractService.findMatchedPetsWithNoContract(currentPetId);
        return ResponseEntity.ok().body(matchedPets);
    }
}
