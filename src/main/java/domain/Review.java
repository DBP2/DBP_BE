package domain;

import java.time.LocalDateTime;
import java.util.List;

public class Review {
    private Long reviewId;
    private Long tradeId;
    private String writerId;
    private String targetId;
    private double rating;
    private List<String> tags;
    private LocalDateTime regDate;

    public String getTagsAsString() {
        if (tags == null || tags.isEmpty()) return "";
        return String.join(",", tags);
    }

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public Long getTradeId() { return tradeId; }
    public void setTradeId(Long tradeId) { this.tradeId = tradeId; }

    public String getWriterId() { return writerId; }
    public void setWriterId(String writerId) { this.writerId = writerId; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public LocalDateTime getRegDate() { return regDate; }
    public void setRegDate(LocalDateTime regDate) { this.regDate = regDate; }
}