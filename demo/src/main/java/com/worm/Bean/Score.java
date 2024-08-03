package com.worm.Bean;

public class Score {
    private int time;
    private String province;
    private String major;
    private int minScore;
    private int maxScore;
    private double averageScore;

    public Score() {}

    public Score(int time, String province, String major, int minScore, int maxScore, double averageScore) {
        this.time = time;
        this.province = province;
        this.major = major;
        this.minScore = province.equals("上海") ? makeScoreSame(minScore) : minScore;
        this.maxScore = province.equals("上海") ? makeScoreSame(maxScore) : maxScore;
        this.averageScore = province.equals("上海") ? makeScoreSame(averageScore) : averageScore;
    }

    public int makeScoreSame(int score) {
        double ret = ((double)score * 660.0) / 750.0;
        return (int)Math.round(ret);
    }

    public double makeScoreSame(double score) {
        return (score * 660.0) / 750.0;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public int getMinScore() {
        return minScore;
    }

    public void setMinScore(int minScore) {
        this.minScore = minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    @Override
    public String toString() {
        return "Score{" +
                "time=" + time +
                ", province='" + province + '\'' +
                ", major='" + major + '\'' +
                ", minScore=" + minScore +
                ", maxScore=" + maxScore +
                ", averageScore=" + averageScore +
                '}';
    }
}
