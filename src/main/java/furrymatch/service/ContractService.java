package furrymatch.service;

import furrymatch.domain.Contract;
import furrymatch.repository.ContractRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Contract}.
 */
@Service
@Transactional
public class ContractService {

    private final Logger log = LoggerFactory.getLogger(ContractService.class);

    private final ContractRepository contractRepository;

    public ContractService(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    /**
     * Save a contract.
     *
     * @param contract the entity to save.
     * @return the persisted entity.
     */
    public Contract save(Contract contract) {
        log.debug("Request to save Contract : {}", contract);
        return contractRepository.save(contract);
    }

    /**
     * Update a contract.
     *
     * @param contract the entity to save.
     * @return the persisted entity.
     */
    public Contract update(Contract contract) {
        log.debug("Request to update Contract : {}", contract);
        return contractRepository.save(contract);
    }

    /**
     * Partially update a contract.
     *
     * @param contract the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Contract> partialUpdate(Contract contract) {
        log.debug("Request to partially update Contract : {}", contract);

        return contractRepository
            .findById(contract.getId())
            .map(existingContract -> {
                if (contract.getTradeMoney() != null) {
                    existingContract.setTradeMoney(contract.getTradeMoney());
                }
                if (contract.getTradePups() != null) {
                    existingContract.setTradePups(contract.getTradePups());
                }
                if (contract.getPedigree() != null) {
                    existingContract.setPedigree(contract.getPedigree());
                }
                if (contract.getOtherNotes() != null) {
                    existingContract.setOtherNotes(contract.getOtherNotes());
                }
                if (contract.getContractDate() != null) {
                    existingContract.setContractDate(contract.getContractDate());
                }

                return existingContract;
            })
            .map(contractRepository::save);
    }

    /**
     * Get all the contracts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Contract> findAll(Pageable pageable) {
        log.debug("Request to get all Contracts");
        return contractRepository.findAll(pageable);
    }

    /**
     *  Get all the contracts where Match is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Contract> findAllWhereMatchIsNull() {
        log.debug("Request to get all contracts where Match is null");
        return StreamSupport
            .stream(contractRepository.findAll().spliterator(), false)
            .filter(contract -> contract.getMatch() == null)
            .collect(Collectors.toList());
    }

    /**
     * Get one contract by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Contract> findOne(Long id) {
        log.debug("Request to get Contract : {}", id);
        return contractRepository.findById(id);
    }

    /**
     * Delete the contract by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Contract : {}", id);
        contractRepository.deleteById(id);
    }

    public List<Object[]> findMatchedPetsWithNoContract(Long currentPetId) {
        return contractRepository.findMatchedPetsAnContracts(currentPetId);
    }
}
