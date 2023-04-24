package furrymatch.service;

import furrymatch.domain.Likee;
import furrymatch.domain.Match;
import furrymatch.repository.LikeeRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Likee}.
 */
@Service
@Transactional
public class LikeeService {

    private final Logger log = LoggerFactory.getLogger(LikeeService.class);

    private final LikeeRepository likeeRepository;

    private final UserService userService;
    private final MatchService matchService;

    public LikeeService(LikeeRepository likeeRepository, UserService userService, MatchService matchService) {
        this.likeeRepository = likeeRepository;
        this.userService = userService;
        this.matchService = matchService;
    }

    /**
     * Save a likee.
     *
     * @param likee the entity to save.
     * @return the persisted entity.
     */
    public Likee save(Likee likee) {
        log.debug("Request to save Likee : {}", likee);
        return likeeRepository.save(likee);
    }

    public void isMatch(Likee likee) {
        boolean isMatch = checkIfBothPetsLikedEachOther(likee.getSecondPet().getId(), likee.getFirstPet().getId());
        if (isMatch) {
            //log.debug("Match is being saved in the backend!");
            Match newMatch = new Match();
            newMatch.setNotifyMatch(true);
            newMatch.setDateMatch(LocalDate.now());
            newMatch.setFirstLiked(likee);
            newMatch.setSecondLiked(
                likeeRepository.findByFirstPetIdAndSecondPetId(likee.getSecondPet().getId(), likee.getFirstPet().getId()).orElse(null)
            );
            matchService.save(newMatch);
        }
    }

    public Long checkIfMatch(Likee likee) {
        boolean isMatch = checkIfBothPetsLikedEachOther(likee.getSecondPet().getId(), likee.getFirstPet().getId());
        if (isMatch) {
            log.debug("Match is being saved in the backend!");
            Match newMatch = new Match();
            newMatch.setNotifyMatch(true);
            newMatch.setDateMatch(LocalDate.now());
            newMatch.setFirstLiked(likee);
            newMatch.setSecondLiked(
                likeeRepository.findByFirstPetIdAndSecondPetId(likee.getSecondPet().getId(), likee.getFirstPet().getId()).orElse(null)
            );
            Match savedMatch = matchService.save(newMatch);
            return savedMatch.getId();
        }
        return null;
    }

    /**
     * Update a likee.
     *
     * @param likee the entity to save.
     * @return the persisted entity.
     */
    public Likee update(Likee likee) {
        log.debug("Request to update Likee : {}", likee);
        return likeeRepository.save(likee);
    }

    /**
     * Partially update a likee.
     *
     * @param likee the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Likee> partialUpdate(Likee likee) {
        log.debug("Request to partially update Likee : {}", likee);

        return likeeRepository
            .findById(likee.getId())
            .map(existingLikee -> {
                if (likee.getLikeState() != null) {
                    existingLikee.setLikeState(likee.getLikeState());
                }

                return existingLikee;
            })
            .map(likeeRepository::save);
    }

    /**
     * Get all the likees.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Likee> findAll(Pageable pageable) {
        log.debug("Request to get all Likees");
        return likeeRepository.findAll(pageable);
    }

    /**
     * Get one likee by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Likee> findOne(Long id) {
        log.debug("Request to get Likee : {}", id);
        return likeeRepository.findById(id);
    }

    /**
     * Delete the likee by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Likee : {}", id);
        likeeRepository.deleteById(id);
    }

    public boolean checkIfBothPetsLikedEachOther(Long firstPetId, Long secondPetId) {
        Optional<Likee> likee1 = likeeRepository.findByFirstPetIdAndSecondPetId(firstPetId, secondPetId);
        Optional<Likee> likee2 = likeeRepository.findByFirstPetIdAndSecondPetId(secondPetId, firstPetId);
        return likee1.isPresent() && likee2.isPresent();
    }
}
