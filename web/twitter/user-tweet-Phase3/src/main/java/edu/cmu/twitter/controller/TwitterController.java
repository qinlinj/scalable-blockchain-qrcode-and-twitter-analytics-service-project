package edu.cmu.twitter.controller;

import edu.cmu.twitter.model.FetchResult;
import edu.cmu.twitter.model.Tweet;
import edu.cmu.twitter.model.Type;
import edu.cmu.twitter.model.User;
import edu.cmu.twitter.utility.CalculateFinalScores;
import edu.cmu.twitter.utility.HashtagScoreCalculator;
import edu.cmu.twitter.utility.InteractionScoreCalculator;
import edu.cmu.twitter.utility.KeywordsScoreCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class TwitterController {


    private final String TEAM_ID = "CloudQuest";
    private final String TEAM_AWS_ACCOUNT_ID = "146445828406";

    @GetMapping("/twitter")
    public String getData(
            @RequestParam(name="user_id",required=false) Long user_id,
            @RequestParam(name="type",required=false) String type,
            @RequestParam(name="phrase",required=false) String phrase,
            @RequestParam(name="hashtag",required=false) String hashtag
    ) throws SQLException {
        StringBuilder sb = new StringBuilder();

        sb.append(TEAM_ID + "," + TEAM_AWS_ACCOUNT_ID + "\n");

        // check if the request query has all params
        if (user_id == null || type == null || phrase == null || hashtag == null) {
            sb.append("INVALID");
            return sb.toString();
        }

        // check if the type is valid
        try {
            Type.valueOf(type.toLowerCase(Locale.ENGLISH));
        } catch (Exception e) {
            sb.append("INVALID");
            System.out.println("Invalid request: Incorrect 'type' parameter");
            return sb.toString();
        }


//        // Fetch Retweet Tweet Map
//        startTime = System.currentTimeMillis();
//        RetweetTweetMapFetcher retweetFetcher = new RetweetTweetMapFetcher();
//        Map<Long, List<Tweet>> retweetTweetMap = retweetFetcher.fetchRetweetTweetMap(user_id);
//        retweetTweetMap.forEach((key, value) -> {
//            System.out.println("Key: " + key);
//            value.forEach(System.out::println);
//        });
//        endTime = System.currentTimeMillis();
//        timeMeasurements.add("RetweetTweetMapFetcher: " + (endTime - startTime) + " ms");
//
//        // Fetch Reply Tweet Map
//        startTime = System.currentTimeMillis();
//        ReplyTweetMapFetcher replyFetcher = new ReplyTweetMapFetcher();
//        Map<Long, List<Tweet>> replyTweetMap = replyFetcher.fetchReplyTweetMap(user_id);
//        replyTweetMap.forEach((key, value) -> {
//            System.out.println("Key: " + key);
//            value.forEach(System.out::println);
//        });
//        endTime = System.currentTimeMillis();
//        timeMeasurements.add("ReplyTweetMapFetcher: " + (endTime - startTime) + " ms");

        // Fetch Retweet and Reply Tweet Map

        DataMapFetcher tweetAndUserFetcher = new DataMapFetcher();
        FetchResult dataresult = tweetAndUserFetcher.fetchDataMaps(user_id);
        Map<String, Map<Long, List<Tweet>>> replyRetweetTweetMap = dataresult.getTweetMap();
        Map<Long, User> userInfoMap = dataresult.getUserMap();
        User requestUser = dataresult.getTargetUser();


        // Calculate Interaction Scores
        InteractionScoreCalculator interactionCalculator = new InteractionScoreCalculator();
        Map<Long, Double> interactionScores = interactionCalculator.calculateInteractionScores(replyRetweetTweetMap);


        // Calculate Hashtag Scores
        HashtagScoreCalculator hashtagCalculator = new HashtagScoreCalculator();
        Map<Long, Double> hashtagScores = hashtagCalculator.calculateHashtagScores(requestUser, userInfoMap);


        Map<Long, List<Tweet>> transformedMap = transformReplyRetweetMap(replyRetweetTweetMap, type);

        // Calculate Keywords Scores
        KeywordsScoreCalculator keywordsCalculator = new KeywordsScoreCalculator();
        Map<Long, Double> keywordsScores = keywordsCalculator.calculateKeywordsScores(transformedMap, phrase, hashtag);



        // Final score calculation
        CalculateFinalScores finalScoreCalculator = new CalculateFinalScores();
        Map<Long, User> unsortedUsers = finalScoreCalculator.calculateFinalScores(keywordsScores, interactionScores, hashtagScores, userInfoMap, transformedMap);




        return generateResponse(unsortedUsers);
//        return "200";
    }




    public String generateResponse(Map<Long, User> userMap) {
        StringBuilder sb = new StringBuilder();
        sb.append(TEAM_ID).append(",").append(TEAM_AWS_ACCOUNT_ID).append("\n");

        List<User> sortedUsers = userMap.values().stream()
                .sorted(Comparator.comparing(User::getFinalScore, Comparator.reverseOrder())
                        .thenComparing(User::getUserId, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        for (int i = 0; i < sortedUsers.size(); i++) {
            User user = sortedUsers.get(i);
            if (user.getFinalScore() > 0) {
                sb.append(user.getUserId()).append("\t")
                        .append(user.getScreenName()).append("\t")
                        .append(user.getDescription()).append("\t")
                        .append(user.getContactTweetText());

                // Append "\n" if this is not the last user
                if (i < sortedUsers.size() - 1) {
                    sb.append("\n");
                }
            }
        }

        return sb.toString();
    }


    public Set<Long> generateOtherUserIdList(Map<String, Map<Long, List<Tweet>>> replyRetweetTweetMap) {
        Set<Long> otherUserIds = new HashSet<>();

        for (Map<Long, List<Tweet>> tweetMap : replyRetweetTweetMap.values()) {
            otherUserIds.addAll(tweetMap.keySet());
        }

        return otherUserIds;
    }


    private Map<Long, List<Tweet>> transformReplyRetweetMap(Map<String, Map<Long, List<Tweet>>> replyRetweetTweetMap, String type) {
        Map<Long, List<Tweet>> transformedMap = new HashMap<>();

        if ("both".equalsIgnoreCase(type)) {
            // Merge both reply and retweet tweets
            for (Map.Entry<String, Map<Long, List<Tweet>>> entry : replyRetweetTweetMap.entrySet()) {
                entry.getValue().forEach((userId, tweets) ->
                        transformedMap.computeIfAbsent(userId, k -> new ArrayList<>()).addAll(tweets));
            }
        } else {
            // Select either reply or retweet tweets
            Map<Long, List<Tweet>> selectedMap = replyRetweetTweetMap.get(type.toLowerCase());
            if (selectedMap != null) {
                selectedMap.forEach((userId, tweets) ->
                        transformedMap.computeIfAbsent(userId, k -> new ArrayList<>()).addAll(tweets));
            }
        }

        return transformedMap;
    }


    @GetMapping("/")
    public String index(){
        return "Healthy Twitter Service!";
    }

}