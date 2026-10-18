package p466.lostandfound.claim;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("ITEM_CLAIM")
public class ItemClaim {

    @Id
    private Long id;

    private Long foundItemId;
    private Long studentId;
    private String explanation;
    private String status;
    private LocalDateTime dateSubmitted;

    public ItemClaim() {
    }

    public ItemClaim(Long foundItemId, Long studentId, String explanation) {
        this.foundItemId = foundItemId;
        this.studentId = studentId;
        this.explanation = explanation;
        this.status = "PENDING";
        this.dateSubmitted = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFoundItemId() {
        return foundItemId;
    }

    public void setFoundItemId(Long foundItemId) {
        this.foundItemId = foundItemId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDateSubmitted() {
        return dateSubmitted;
    }

    public void setDateSubmitted(LocalDateTime dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }
}
