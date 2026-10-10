package p466.lostandfound.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;

import p466.lostandfound.FoundItem;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
class FoundItemRepositoryTest {

    @Autowired
    private FoundItemRepository foundItemRepository;

    @Test
    void shouldSaveAndFindItem() {
        FoundItem item = new FoundItem(
                "Water Bottle",
                "Blue water bottle",
                "Wells Library"
        );

        FoundItem savedItem = foundItemRepository.save(item);

        assertNotNull(savedItem.getId());

        FoundItem foundItem = foundItemRepository
                .findById(savedItem.getId())
                .orElseThrow();

        assertEquals("Water Bottle", foundItem.getName());
        assertEquals("Blue water bottle", foundItem.getDescription());
        assertEquals("Wells Library", foundItem.getFoundLocation());
    }
}
