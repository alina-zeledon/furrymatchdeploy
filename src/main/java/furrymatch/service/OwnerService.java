package furrymatch.service;

import furrymatch.domain.Owner;
import furrymatch.repository.OwnerRepository;
import furrymatch.repository.UserRepository;
import furrymatch.security.SecurityUtils;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Owner}.
 */
@Service
@Transactional
public class OwnerService {

    private final Logger log = LoggerFactory.getLogger(OwnerService.class);

    private final OwnerRepository ownerRepository;

    private final UserRepository userRepository;

    private Long selectedPet;

    public OwnerService(OwnerRepository ownerRepository, UserRepository userRepository) {
        this.ownerRepository = ownerRepository;
        this.userRepository = userRepository;
    }

    /**
     * Save a owner.
     *
     * @param owner the entity to save.
     * @return the persisted entity.
     */
    public Owner save(Owner owner) {
        log.debug("Request to save Owner : {}", owner);
        return ownerRepository.save(owner);
    }

    /**
     * Update a owner.
     *
     * @param owner the entity to save.
     * @return the persisted entity.
     */
    public Owner update(Owner owner) {
        log.debug("Request to update Owner : {}", owner);
        return ownerRepository.save(owner);
    }

    /**
     * Partially update a owner.
     *
     * @param owner the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Owner> partialUpdate(Owner owner) {
        log.debug("Request to partially update Owner : {}", owner);

        return ownerRepository
            .findById(owner.getId())
            .map(existingOwner -> {
                if (owner.getFirstName() != null) {
                    existingOwner.setFirstName(owner.getFirstName());
                }
                if (owner.getSecondName() != null) {
                    existingOwner.setSecondName(owner.getSecondName());
                }
                if (owner.getFirstLastName() != null) {
                    existingOwner.setFirstLastName(owner.getFirstLastName());
                }
                if (owner.getSecondLastName() != null) {
                    existingOwner.setSecondLastName(owner.getSecondLastName());
                }
                if (owner.getPhoneNumber() != null) {
                    existingOwner.setPhoneNumber(owner.getPhoneNumber());
                }
                if (owner.getPhoto() != null) {
                    existingOwner.setPhoto(owner.getPhoto());
                }
                if (owner.getIdentityNumber() != null) {
                    existingOwner.setIdentityNumber(owner.getIdentityNumber());
                }
                if (owner.getAddress() != null) {
                    existingOwner.setAddress(owner.getAddress());
                }
                if (owner.getProvince() != null) {
                    existingOwner.setProvince(owner.getProvince());
                }
                if (owner.getCanton() != null) {
                    existingOwner.setCanton(owner.getCanton());
                }
                if (owner.getDistrict() != null) {
                    existingOwner.setDistrict(owner.getDistrict());
                }
                if (owner.getOtp() != null) {
                    existingOwner.setOtp(owner.getOtp());
                }
                if (owner.getCreatedAt() != null) {
                    existingOwner.setCreatedAt(owner.getCreatedAt());
                }
                if (owner.getUpdatedAt() != null) {
                    existingOwner.setUpdatedAt(owner.getUpdatedAt());
                }

                return existingOwner;
            })
            .map(ownerRepository::save);
    }

    /**
     * Get all the owners.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Owner> findAll(Pageable pageable) {
        log.debug("Request to get all Owners");
        return ownerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Owner> findUserChats() {
        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                selectedPet = Long.valueOf(user.getImageUrl());
            });
        return ownerRepository.findUserPetChats(selectedPet);
    }

    /**
     * Get one owner by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Owner> findOne(Long id) {
        log.debug("Request to get Owner : {}", id);
        return ownerRepository.findById(id);
    }

    /**
     * Delete the owner by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Owner : {}", id);
        ownerRepository.deleteById(id);
    }
}
