package furrymatch.service;

import furrymatch.domain.Pet;
import furrymatch.domain.SearchCriteria;
import furrymatch.repository.SearchCriteriaRepository;
import furrymatch.repository.UserRepository;
import furrymatch.security.SecurityUtils;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link SearchCriteria}.
 */
@Service
@Transactional
public class SearchCriteriaService {

    private final Logger log = LoggerFactory.getLogger(SearchCriteriaService.class);

    private final SearchCriteriaRepository searchCriteriaRepository;

    private final UserRepository userRepository;

    private final PetService petService;

    private Long selectedPet;

    public SearchCriteriaService(SearchCriteriaRepository searchCriteriaRepository, UserRepository userRepository, PetService petService) {
        this.searchCriteriaRepository = searchCriteriaRepository;
        this.userRepository = userRepository;
        this.petService = petService;
    }

    /**
     * Save a searchCriteria.
     *
     * @param searchCriteria the entity to save.
     * @return the persisted entity.
     */
    public SearchCriteria save(SearchCriteria searchCriteria) {
        log.debug("Request to save SearchCriteria : {}", searchCriteria);
        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                Optional<Pet> pet = petService.findOne(Long.valueOf(user.getImageUrl()));
                searchCriteria.setPet(pet.get());
            });
        return searchCriteriaRepository.save(searchCriteria);
    }

    /**
     * Update a searchCriteria.
     *
     * @param searchCriteria the entity to save.
     * @return the persisted entity.
     */
    public SearchCriteria update(SearchCriteria searchCriteria) {
        log.debug("Request to update SearchCriteria : {}", searchCriteria);
        return searchCriteriaRepository.save(searchCriteria);
    }

    /**
     * Partially update a searchCriteria.
     *
     * @param searchCriteria the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SearchCriteria> partialUpdate(SearchCriteria searchCriteria) {
        log.debug("Request to partially update SearchCriteria : {}", searchCriteria);

        return searchCriteriaRepository
            .findById(searchCriteria.getId())
            .map(existingSearchCriteria -> {
                if (searchCriteria.getFilterType() != null) {
                    existingSearchCriteria.setFilterType(searchCriteria.getFilterType());
                }
                if (searchCriteria.getBreed() != null) {
                    existingSearchCriteria.setBreed(searchCriteria.getBreed());
                }
                if (searchCriteria.getTradePups() != null) {
                    existingSearchCriteria.setTradePups(searchCriteria.getTradePups());
                }
                if (searchCriteria.getSex() != null) {
                    existingSearchCriteria.setSex(searchCriteria.getSex());
                }
                if (searchCriteria.getPedigree() != null) {
                    existingSearchCriteria.setPedigree(searchCriteria.getPedigree());
                }
                if (searchCriteria.getTradeMoney() != null) {
                    existingSearchCriteria.setTradeMoney(searchCriteria.getTradeMoney());
                }
                if (searchCriteria.getProvice() != null) {
                    existingSearchCriteria.setProvice(searchCriteria.getProvice());
                }
                if (searchCriteria.getCanton() != null) {
                    existingSearchCriteria.setCanton(searchCriteria.getCanton());
                }
                if (searchCriteria.getDistrict() != null) {
                    existingSearchCriteria.setDistrict(searchCriteria.getDistrict());
                }
                if (searchCriteria.getObjective() != null) {
                    existingSearchCriteria.setObjective(searchCriteria.getObjective());
                }

                return existingSearchCriteria;
            })
            .map(searchCriteriaRepository::save);
    }

    /**
     * Get all the searchCriteria.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<SearchCriteria> findAll(Pageable pageable) {
        log.debug("Request to get all SearchCriteria");
        return searchCriteriaRepository.findAll(pageable);
    }

    /**
     * Get one searchCriteria by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SearchCriteria> findOne(Long id) {
        log.debug("Request to get SearchCriteria : {}", id);
        return searchCriteriaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public SearchCriteria findByPetId(Long id) {
        log.debug("Request to get SearchCriteria : {}", id);
        return searchCriteriaRepository.findByPetId(id);
    }

    @Transactional(readOnly = true)
    public Optional<SearchCriteria> findByOwnerUser() {
        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                selectedPet = Long.valueOf(user.getImageUrl());
            });
        return searchCriteriaRepository.findByOwnerUser(selectedPet);
    }

    /**
     * Delete the searchCriteria by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete SearchCriteria : {}", id);
        searchCriteriaRepository.deleteById(id);
    }
}
