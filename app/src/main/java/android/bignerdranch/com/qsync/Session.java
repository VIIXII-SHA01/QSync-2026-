package android.bignerdranch.com.qsync;

import com.google.firebase.Timestamp;

public class Session {
    private String id;
    private String userId;
    private String userName;
    private String kioskId;
    private String kioskName;
    private long number;
    private String status;
    private Timestamp timestamp;

    public Session() {
        // Required for Firestore
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getKioskId() { return kioskId; }
    public void setKioskId(String kioskId) { this.kioskId = kioskId; }

    public String getKioskName() { return kioskName; }
    public void setKioskName(String kioskName) { this.kioskName = kioskName; }

    public long getNumber() { return number; }
    public void setNumber(long number) { this.number = number; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
