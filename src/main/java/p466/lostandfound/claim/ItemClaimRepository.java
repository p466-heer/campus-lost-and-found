package p466.lostandfound.claim;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ItemClaimRepository extends CrudRepository<ItemClaim, Long> {

    List<ItemClaim> findByFoundItemId(Long foundItemId);

    List<ItemClaim> findByStudentId(Long studentId);

    List<ItemClaim> findByStatus(String status);
}
