package domain;

import java.time.LocalDateTime;
import java.util.List;

public class SafeZoneRecord {
    private Long recordId;
    private Long tradeId;
    private String recorderId;
    private Long safeZoneId;
    private String recordAction;
    private LocalDateTime recordedAt;
    private SafeZone safeZone;
    private List<String> photoUrls;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public String getRecorderId() {
        return recorderId;
    }

    public void setRecorderId(String recorderId) {
        this.recorderId = recorderId;
    }

    public Long getSafeZoneId() {
        return safeZoneId;
    }

    public void setSafeZoneId(Long safeZoneId) {
        this.safeZoneId = safeZoneId;
    }

    public String getRecordAction() {
        return recordAction;
    }

    public void setRecordAction(String recordAction) {
        this.recordAction = recordAction;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public SafeZone getSafeZone() {
        return safeZone;
    }

    public void setSafeZone(SafeZone safeZone) {
        this.safeZone = safeZone;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }
}
