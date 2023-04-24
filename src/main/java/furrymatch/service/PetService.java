package furrymatch.service;

import furrymatch.domain.Owner;
import furrymatch.domain.Pet;
import furrymatch.domain.Photo;
import furrymatch.domain.SearchCriteria;
import furrymatch.repository.*;
import furrymatch.security.SecurityUtils;
import java.time.LocalDate;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Pet}.
 */
@Service
@Transactional
public class PetService {

    private final Logger log = LoggerFactory.getLogger(PetService.class);

    private final PetRepository petRepository;

    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;

    private final SearchPetRepository searchPetRepository;

    public PetService(
        PetRepository petRepository,
        PhotoRepository photoRepository,
        UserRepository userRepository,
        OwnerRepository ownerRepository,
        SearchPetRepository searchPetRepository
    ) {
        this.petRepository = petRepository;
        this.photoRepository = photoRepository;
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.searchPetRepository = searchPetRepository;
    }

    /**
     * Save a pet.
     *
     * @param pet the entity to save.
     * @return the persisted entity.
     */
    public Pet save(Pet pet) {
        log.debug("Request to save Pet : {}", pet);

        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                Optional<Owner> optionalOwner = ownerRepository.findById(user.getId());
                Owner owner = optionalOwner.get();
                pet.setOwner(owner);
                System.out.println(owner);
                log.debug("Changed Information for User: {}", user);
            });

        petRepository.save(pet);

        if (pet.getPhotos() != null) {
            LocalDate currentDate = LocalDate.now();
            pet
                .getPhotos()
                .forEach(photo -> {
                    photo.setPet(pet);
                    photo.setId(null);
                    if (photo.getUploadDate() == null) {
                        photo.setUploadDate(currentDate);
                    }
                    photoRepository.save(photo);
                });
        }
        return pet;
    }

    /**
     * Update a pet.
     *
     * @param pet the entity to save.
     * @return the persisted entity.
     */
    public Pet update(Pet pet) {
        log.debug("Request to update Pet : {}", pet);

        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                Optional<Owner> optionalOwner = ownerRepository.findById(user.getId());
                Owner owner = optionalOwner.get();
                pet.setOwner(owner);
                System.out.println(owner);
                log.debug("Changed Information for User: {}", user);
            });

        petRepository.save(pet);

        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + pet.getPhotos());
        if (pet.getPhotos() != null) {
            LocalDate currentDate = LocalDate.now();
            pet
                .getPhotos()
                .forEach(photo -> {
                    photo.setPet(pet);
                    System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + photo);
                    if (photo.getUploadDate() == null) {
                        photo.setUploadDate(currentDate);
                    }
                    photoRepository.save(photo); // Save photo (create or update)
                });
        }
        return pet;
    }

    /* public Pet update(Pet pet, List<Photo> photos) {
        log.debug("Request to update Pet : {}", pet);
        Pet updatedPed = petRepository.save(pet);

        //Create and update the Photo entity
        // Iterate through the photos and save them
        if (photos != null) {
            LocalDate currentDate = LocalDate.now();
            for (Photo photo : photos) {
                photo.setPet(updatedPed);
                if (photo.getUploadDate() == null){
                    photo.setUploadDate(currentDate);
                }
                photoRepository.save(photo);
            }
        }
        return updatedPed;
    }*/

    /**
     * Partially update a pet.
     *
     * @param pet the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Pet> partialUpdate(Pet pet) {
        log.debug("Request to partially update Pet : {}", pet);

        return petRepository
            .findById(pet.getId())
            .map(existingPet -> {
                if (pet.getName() != null) {
                    existingPet.setName(pet.getName());
                }
                if (pet.getPetType() != null) {
                    existingPet.setPetType(pet.getPetType());
                }
                if (pet.getDescription() != null) {
                    existingPet.setDescription(pet.getDescription());
                }
                if (pet.getSex() != null) {
                    existingPet.setSex(pet.getSex());
                }
                if (pet.getTradeMoney() != null) {
                    existingPet.setTradeMoney(pet.getTradeMoney());
                }
                if (pet.getTradePups() != null) {
                    existingPet.setTradePups(pet.getTradePups());
                }
                if (pet.getPedigree() != null) {
                    existingPet.setPedigree(pet.getPedigree());
                }
                if (pet.getDesireAmmount() != null) {
                    existingPet.setDesireAmmount(pet.getDesireAmmount());
                }

                return existingPet;
            })
            .map(petRepository::save);
    }

    /**
     * Get all the pets.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    /* public Page<Pet> findAll(Pageable pageable) {
        log.debug("Request to get all Pets");
        return petRepository.findAll(pageable);
    }*/
    public List<Pet> findAll(Long id) {
        log.debug("Request to get all Pets");
        List<Pet> pets = petRepository.findAllByOwnerID(id);
        ArrayList<Pet> newPets = new ArrayList<>();
        pets.forEach(pet -> {
            Set<Photo> set = new HashSet<>(photoRepository.findAllPhotosByPetID(pet.getId()));
            pet.setPhotos(set);
            newPets.add(pet);
        });
        return newPets;
    }

    public List<Pet> searchPets(SearchCriteria searchCriteria, Long ownerId) {
        // log.debug("Request to get all Pets based on search criteria");
        List<Pet> pets = searchPetRepository.searchPets(searchCriteria, ownerId);

        // System.out.println("Pets returned by searchPetRepository.searchPets():");
        pets.forEach(System.out::println);
        ArrayList<Pet> newPets = new ArrayList<>();
        pets.forEach(pet -> {
            Set<Photo> petPhotos = pet.getPhotos();
            if (petPhotos == null) {
                petPhotos = new HashSet<>();
            }
            List<Photo> photosByPetID = photoRepository.findAllPhotosByPetID(pet.getId());
            petPhotos.addAll(photosByPetID);
            pet.setPhotos(petPhotos);
            newPets.add(pet);
            // System.out.println("List of pets" + pet);
        });
        return newPets;
    }

    /**
     * Get one pet by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Pet> findOne(Long id) {
        log.debug("Request to get Pet : {}", id);
        return petRepository.findById(id);
    }

    /**
     * Delete the pet by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Pet : {}", id);
        petRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Long getCurrentUserPetId() {
        return SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .map(user -> Long.valueOf(user.getImageUrl()))
            .orElseThrow(() -> new IllegalStateException("User pet ID not found"));
    }

    @Transactional(readOnly = true)
    public Optional<Long> getPetId() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin).map(user -> Long.valueOf(user.getImageUrl()));
    }
}
