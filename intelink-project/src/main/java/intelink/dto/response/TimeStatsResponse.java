package intelink.dto.response;

import intelink.dto.object.StatsCategory;

import java.util.List;

public class TimeStatsResponse {
    private StatsCategory hourly;
    private StatsCategory daily;
    private StatsCategory monthly;

    // Constructor
    public TimeStatsResponse(StatsCategory hourly, StatsCategory daily, StatsCategory monthly) {
        this.hourly = hourly;
        this.daily = daily;
        this.monthly = monthly;
    }

    // Getters and setters
    public StatsCategory getHourly() {
        return hourly;
    }

    public void setHourly(StatsCategory hourly) {
        this.hourly = hourly;
    }

    public StatsCategory getDaily() {
        return daily;
    }

    public void setDaily(StatsCategory daily) {
        this.daily = daily;
    }

    public StatsCategory getMonthly() {
        return monthly;
    }

    public void setMonthly(StatsCategory monthly) {
        this.monthly = monthly;
    }

    public TimeStatsResponse(StatsCategory hourly) {
        this.hourly = hourly;
    }
}




