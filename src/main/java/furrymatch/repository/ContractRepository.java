package furrymatch.repository;

import furrymatch.domain.Contract;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Contract entity.
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    @Query(
        "SELECT m, l.firstPet, l.secondPet " +
        "FROM Match m " +
        "JOIN Likee l ON m.firstLiked.id = l.id OR m.secondLiked.id = l.id " +
        "LEFT JOIN m.contract c " +
        "WHERE l.firstPet.id = :currentPetId"
    )
    List<Object[]> findMatchedPetsAnContracts(@Param("currentPetId") Long currentPetId);
}
