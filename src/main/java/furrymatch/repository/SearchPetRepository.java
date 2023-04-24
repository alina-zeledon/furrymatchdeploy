package furrymatch.repository;

import furrymatch.domain.*;
import furrymatch.domain.enumeration.PetType;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import org.springframework.stereotype.Repository;

@Repository
public class SearchPetRepository {

    private final EntityManager em;

    public SearchPetRepository(EntityManager em) {
        this.em = em;
    }

    public List<Pet> searchPets(SearchCriteria filters, Long ownerId) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Pet> criteriaQueryPet = criteriaBuilder.createQuery(Pet.class);
        Root<Pet> petRoot = criteriaQueryPet.from(Pet.class);

        List<Predicate> predicates = new ArrayList<>();
        System.out.print("Filters" + filters);

        // Join with SearchCriteria
        Join<Pet, SearchCriteria> searchCriteriaJoin = petRoot.join("searchCriteria", JoinType.LEFT);

        if (filters.getObjective() != null) {
            Predicate objectivePredicate = criteriaBuilder.equal(searchCriteriaJoin.get("objective"), filters.getObjective());
            predicates.add(objectivePredicate);
        }

        if (filters.getFilterType() != null) {
            PetType petType = PetType.valueOf(filters.getFilterType());
            Predicate petTypePredicate = criteriaBuilder.equal(petRoot.get("petType"), petType);
            predicates.add(petTypePredicate);
        }

        if (filters.getSex() != null) {
            Predicate sexPredicate = criteriaBuilder.equal(petRoot.get("sex"), filters.getSex());
            predicates.add(sexPredicate);
        }

        if (filters.getBreed() != null) {
            Long breedId = Long.valueOf(filters.getBreed());
            Breed breed = new Breed();
            breed.setId(breedId);
            Predicate breedPredicate = criteriaBuilder.equal(petRoot.get("breed"), breed);
            predicates.add(breedPredicate);
        }

        if (filters.getTradePups() != null) {
            Predicate tradePupsPredicate = criteriaBuilder.equal(petRoot.get("tradePups"), filters.getTradePups());
            predicates.add(tradePupsPredicate);
        }

        if (filters.getPedigree() != null) {
            Predicate pedigreePredicate = criteriaBuilder.equal(petRoot.get("pedigree"), filters.getPedigree());
            predicates.add(pedigreePredicate);
        }

        //join condition between Pet and Owner
        Join<Pet, Owner> ownerJoin = petRoot.join("owner");
        if (filters.getProvice() != null) {
            Predicate provincePredicate = criteriaBuilder.equal(ownerJoin.get("provice"), filters.getProvice());
            predicates.add(provincePredicate);
        }
        if (filters.getCanton() != null) {
            Predicate cantonPredicate = criteriaBuilder.equal(ownerJoin.get("canton"), filters.getCanton());
            predicates.add(cantonPredicate);
        }
        if (filters.getDistrict() != null) {
            Predicate districtPredicate = criteriaBuilder.equal(ownerJoin.get("district"), filters.getDistrict());
            predicates.add(districtPredicate);
        }

        // Exclude pets belonging to the current owner
        Predicate excludeCurrentOwnerPredicate = criteriaBuilder.notEqual(ownerJoin.get("id"), ownerId);
        predicates.add(excludeCurrentOwnerPredicate);

        // Exclude pets that have already been liked by the current user's pet
        Subquery<Long> likedPetsSubquery = criteriaQueryPet.subquery(Long.class);
        Root<Likee> likeRoot = likedPetsSubquery.from(Likee.class);
        likedPetsSubquery.select(likeRoot.get("secondPet").get("id"));
        likedPetsSubquery.where(criteriaBuilder.equal(likeRoot.get("firstPet").get("id"), filters.getPet().getId()));

        Predicate notLikedByUserPetPredicate = petRoot.get("id").in(likedPetsSubquery).not();
        predicates.add(notLikedByUserPetPredicate);

        criteriaQueryPet.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        TypedQuery<Pet> petQuery = em.createQuery(criteriaQueryPet);

        return petQuery.getResultList();
    }
}
