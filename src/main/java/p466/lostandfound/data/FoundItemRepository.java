package p466.lostandfound.data;

import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import p466.lostandfound.FoundItem;

public interface FoundItemRepository extends CrudRepository<FoundItem, Long> {

    @Query("SELECT * FROM \"FOUND_ITEM\" WHERE id = :id FOR UPDATE")
    Optional<FoundItem> findByIdForUpdate(Long id);

}
