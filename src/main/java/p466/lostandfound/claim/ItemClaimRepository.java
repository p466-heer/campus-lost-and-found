package p466.lostandfound.claim;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jdbc.repository.query.Query;
import java.util.List;
import java.util.Optional;

public interface ItemClaimRepository extends CrudRepository<ItemClaim, Long> {

    List<ItemClaim> findByFoundItemId(Long foundItemId);

    List<ItemClaim> findByStudentId(Long studentId);

    List<ItemClaim> findByStatus(String status);

    @Query("SELECT * FROM \"ITEM_CLAIM\" WHERE id = :id FOR UPDATE")
    Optional<ItemClaim> findByIdForUpdate(Long id);
}