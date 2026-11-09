package service.dto;

public class ChatRoomDetail {
    private Long chatRoomId;
    private Long tradeId;
    private String tradeStatus;
    private String myRole;
    private String partnerId;
    private String partnerName;
    private boolean partnerVerified;
    private int partnerTrustScore;
    private Long postId;
    private String postTitle;
    private int price;
    private String thumbnailUrl;

    public Long getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public String getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(String tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public String getMyRole() {
        return myRole;
    }

    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public boolean isPartnerVerified() {
        return partnerVerified;
    }

    public void setPartnerVerified(boolean partnerVerified) {
        this.partnerVerified = partnerVerified;
    }

    public int getPartnerTrustScore() {
        return partnerTrustScore;
    }

    public void setPartnerTrustScore(int partnerTrustScore) {
        this.partnerTrustScore = partnerTrustScore;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
