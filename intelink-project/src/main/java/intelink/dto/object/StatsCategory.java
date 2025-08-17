package intelink.dto.object;

import java.util.List;

public class StatsCategory {
    private String category;
    private long totalClicks;
    private List<StatEntry> data;
    // Constructor
    public StatsCategory(String category, long totalClicks, List<StatEntry> data) {
        this.category = category;
        this.totalClicks = totalClicks;
        this.data = data;
    }

    // Getters and setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(long totalClicks) {
        this.totalClicks = totalClicks;
    }

    public List<StatEntry> getData() {
        return data;
    }

    public void setData(List<StatEntry> data) {
        this.data = data;
    }
}
