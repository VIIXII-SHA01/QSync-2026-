package android.bignerdranch.com.qsync;

import java.util.ArrayList;
import java.util.List;

public class Kiosk {
    private String id;
    private String name;
    private String nameLowercase;
    private String date;
    private String description;
    private String creatorId;
    private List<String> hostIds;
    private String status;
    private int dailyLimit;
    private int currentTransaction;
    private int availableNumber;
    private long createdAt;

    public Kiosk() {
        // Required for Firestore
        this.hostIds = new ArrayList<>();
    }

    public Kiosk(String id, String name, String date, String description, String creatorId, String status, int dailyLimit, int currentTransaction, int availableNumber, long createdAt) {
        this.id = id;
        this.name = name;
        this.nameLowercase = name != null ? name.toLowerCase() : null;
        this.date = date;
        this.description = description;
        this.creatorId = creatorId;
        this.status = status;
        this.dailyLimit = dailyLimit;
        this.currentTransaction = currentTransaction;
        this.availableNumber = availableNumber;
        this.createdAt = createdAt;
        this.hostIds = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        this.nameLowercase = name != null ? name.toLowerCase() : null;
    }
    
    public String getNameLowercase() { return nameLowercase; }
    public void setNameLowercase(String nameLowercase) { this.nameLowercase = nameLowercase; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public List<String> getHostIds() {
        if (hostIds == null) hostIds = new ArrayList<>();
        return hostIds;
    }
    public void setHostIds(List<String> hostIds) { this.hostIds = hostIds; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(int dailyLimit) { this.dailyLimit = dailyLimit; }
    
    public int getCurrentTransaction() { return currentTransaction; }
    public void setCurrentTransaction(int currentTransaction) { this.currentTransaction = currentTransaction; }
    
    public int getAvailableNumber() { return availableNumber; }
    public void setAvailableNumber(int availableNumber) { this.availableNumber = availableNumber; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
