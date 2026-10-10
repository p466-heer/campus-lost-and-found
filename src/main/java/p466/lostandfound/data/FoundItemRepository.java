package p466.lostandfound.data;

import org.springframework.data.repository.CrudRepository;
import p466.lostandfound.FoundItem;

public interface FoundItemRepository extends CrudRepository<FoundItem, Long> {
}
