
package p466.lostandfound.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import p466.lostandfound.data.FoundItemRepository;
import p466.lostandfound.user.AppUserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FoundItemController.class)
class FoundItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FoundItemRepository foundItemRepository;

    @MockitoBean
    private AppUserRepository userRepository;

    @Test
    void shouldShowFoundItemsPage() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("foundItems"))
                .andExpect(model().attributeExists("items"));
    }

    @Test
    void shouldShowReportForm() throws Exception {
        mockMvc.perform(get("/items/report"))
                .andExpect(status().isOk())
                .andExpect(view().name("reportItem"))
                .andExpect(model().attributeExists("foundItem"));
    }

    @Test
    void shouldRejectEmptyReport() throws Exception {
        mockMvc.perform(post("/items/report")
                        .param("name", "")
                        .param("description", "")
                        .param("foundLocation", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("reportItem"))
                .andExpect(model().hasErrors());
    }
}
