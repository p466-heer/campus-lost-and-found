package p466.lostandfound.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import p466.lostandfound.FoundItem;
import p466.lostandfound.data.FoundItemRepository;
import jakarta.validation.Valid;
import org.springframework.validation.Errors;


@Controller
public class FoundItemController {

    private final FoundItemRepository foundItemRepository;

    public FoundItemController(FoundItemRepository foundItemRepository) {
        this.foundItemRepository = foundItemRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String showItems(Model model) {
        model.addAttribute("items", foundItemRepository.findAll());
        return "foundItems";
    }

    @GetMapping("/items/report")
    public String showReportForm(Model model) {
        model.addAttribute("foundItem", new FoundItem());
        return "reportItem";
    }

    @PostMapping("/items/report")
    public String reportItem(@Valid @ModelAttribute FoundItem foundItem, Errors errors) {
        if (errors.hasErrors()) {
            return "reportItem";
        }

        foundItem.setDateReported(java.time.LocalDateTime.now());
        foundItemRepository.save(foundItem);
        return "redirect:/items";
    }

}
