package edu.cmu.twitter.model;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Date;

public class User {
    Long userId;
    String screenName;
    String description;
    String contactTweetText;
    double finalScore;
    private JSONArray hashtagCounts;
    public User(Long userId, String screenName, String description, String contactTweetText, double finalScore, JSONArray hashtagCounts) {
        this.userId = userId;
        this.screenName = screenName;
        this.description = description;
        this.contactTweetText = contactTweetText;
        this.finalScore = finalScore;
        this.hashtagCounts = hashtagCounts;
    }
    public static Date parseDate(String dateStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONArray getHashtagCounts() {
        return hashtagCounts;
    }

    public void setHashtagCounts(JSONArray hashtagCounts) {
        this.hashtagCounts = hashtagCounts;
    }

    public Long getUserId() {
        return userId;
    }

    public double getFinalScore() {
        return finalScore;
    }
    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getDescription() {
        return description;
    }

    public String getContactTweetText() {
        return contactTweetText;
    }

    public void setContactTweetText(String contactTweetText) {
        this.contactTweetText = contactTweetText;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", screenName='" + screenName + '\'' +
                ", description='" + description + '\'' +
                ", contactTweetText='" + contactTweetText + '\'' +
                ", finalScore=" + finalScore +
                ", hashtagCounts=" + hashtagCounts +
                '}';
    }
}
