package intelink.dto.object;

public class StatEntry {
    private String time;
    private long clicks;

    // Constructor
    public StatEntry(String time, long clicks) {
        this.time = time;
        this.clicks = clicks;
    }

    // Getters and setters
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getClicks() {
        return clicks;
    }

    public void setClicks(long clicks) {
        this.clicks = clicks;
    }
}