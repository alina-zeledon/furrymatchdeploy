package furrymatch.service;

import furrymatch.domain.Match;
import furrymatch.domain.User;
import furrymatch.repository.MatchRepository;
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
 * Service Implementation for managing {@link Match}.
 *
 */
@Service
@Transactional
public class MatchService {

    private final Logger log = LoggerFactory.getLogger(MatchService.class);

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    public MatchService(MatchRepository matchRepository, UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }

    /**
     * Save a match.
     *
     * @param match the entity to save.
     * @return the persisted entity.
     */
    public Match save(Match match) {
        log.debug("Request to save Match : {}", match);
        return matchRepository.save(match);
    }

    /**
     * Update a match.
     *
     * @param match the entity to save.
     * @return the persisted entity.
     */
    public Match update(Match match) {
        log.debug("Request to update Match : {}", match);
        return matchRepository.save(match);
    }

    /**
     * Partially update a match.
     *
     * @param match the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Match> partialUpdate(Match match) {
        log.debug("Request to partially update Match : {}", match);

        return matchRepository
            .findById(match.getId())
            .map(existingMatch -> {
                if (match.getNotifyMatch() != null) {
                    existingMatch.setNotifyMatch(match.getNotifyMatch());
                }
                if (match.getDateMatch() != null) {
                    existingMatch.setDateMatch(match.getDateMatch());
                }

                return existingMatch;
            })
            .map(matchRepository::save);
    }

    /**
     * Get all the matches.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Match> findAll(Pageable pageable) {
        log.debug("Request to get all Matches");
        return matchRepository.findAll(pageable);
    }

    /**
     * Get one match by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Match> findOne(Long id) {
        log.debug("Request to get Match : {}", id);
        return matchRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Long> getCurrentUserPetId() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin).map(user -> Long.valueOf(user.getImageUrl()));
    }

    /**
     * Delete the match by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Match : {}", id);
        matchRepository.deleteById(id);
    }
}
