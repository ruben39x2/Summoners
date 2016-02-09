package info.summoners.app.rest.lol.util;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import info.summoners.app.rest.lol.summonersApp.R;

// LoLDataParser.java

// A parser for the answers from the server - static methods that receive a JSONObject with the
// game info and return other types.

// NOTE: This methods are not general, the JSONObject that they receive depends on how each
// activity manipulates the data that received from the Riot server.

public class LoLDataParser {

    public static String getChampionId(Context context, JSONObject game) {
        Integer champ;
        try {
            champ = game.getInt("championId");
            return champ.toString();
        } catch (JSONException e) {
            return context.getString(R.string.unknown);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getCreateDate(Context context, JSONObject game) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            // Parse dates (they come as Long; secs since Epoch).
            Long longDate = game.getLong("createDate");
            Date date = new Date(longDate);
            return sdf.format(date);
        } catch (Exception e) {
            return context.getString(R.string.unknown);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getGameMode(Context context, JSONObject game) {
        try {
            String mode = game.getString("gameMode");
            if (mode.equals("CLASSIC")) return context.getString(R.string.classic);
            if (mode.equals("ODIN")) return context.getString(R.string.odin);
            if (mode.equals("ARAM")) return context.getString(R.string.aram);
            if (mode.equals("TUTORIAL")) return context.getString(R.string.tutorial);
            if (mode.equals("ONEFORALL")) return context.getString(R.string.one_for_all);
            if (mode.equals("ASCENSION")) return context.getString(R.string.ascension);
            if (mode.equals("FIRSTBLOOD")) return context.getString(R.string.first_blood);
            if (mode.equals("KINGPORO")) return context.getString(R.string.king_poro);
            return context.getString(R.string.unknown);
        } catch (JSONException e) {
            return context.getString(R.string.unknown);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    // This method return a especially adapted String for the List of games in the
    // ShowSummonerActivity, it is, preceded by "-" in case it's not Matched game.
    public static String getGameType(Context context, JSONObject game) {
        try {
            String mode = game.getString("gameType");
            if (mode.equals("CUSTOM_GAME")) return " - " + context.getString(R.string.custom) + " ";
            if (mode.equals("MATCHED_GAME")) return "";
            if (mode.equals("TUTORIAL_GAME"))
                return " - " + context.getString(R.string.tutorial_match) + " ";
            return " - " + context.getString(R.string.unknown) + " ";
        } catch (JSONException e) {
            return " - " + context.getString(R.string.unknown) + " ";
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getGameSubtype(Context context, JSONObject game) {
        try {
            String mode = game.getString("subType");
            if (mode.equals("NORMAL")) return context.getString(R.string.normal_game);
            if (mode.equals("BOT")) return context.getString(R.string.bot);
            if (mode.equals("RANKED_SOLO_5x5")) return context.getString(R.string.ranked_solo_5x5);
            if (mode.equals("RANKED_PREMADE_3x3"))
                return context.getString(R.string.ranked_premade_3x3);
            if (mode.equals("RANKED_PREMADE_5x5"))
                return context.getString(R.string.ranked_premade_5x5);
            if (mode.equals("ODIN_UNRANKED")) return context.getString(R.string.odin_game);
            if (mode.equals("RANKED_TEAM_3x3")) return context.getString(R.string.ranked_team_3x3);
            if (mode.equals("RANKED_TEAM_5x5")) return context.getString(R.string.ranked_team_5x5);
            if (mode.equals("NORMAL_3x3")) return context.getString(R.string.normal_3x3);
            if (mode.equals("BOT_3x3")) return context.getString(R.string.bot_3x3);
            if (mode.equals("CAP_5x5")) return context.getString(R.string.cap_5x5);
            if (mode.equals("ARAM_UNRANKED_5x5"))
                return context.getString(R.string.aram_unranked_5x5);
            if (mode.equals("NONE")) return context.getString(R.string.unknown);
            if (mode.equals("ONEFORALL_5x5")) return context.getString(R.string.oneforall_5x5);
            if (mode.equals("URF")) return context.getString(R.string.urf);
            if (mode.equals("URF_BOT")) return context.getString(R.string.bot_urf);
            if (mode.equals("NIGHTMARE_BOT")) return context.getString(R.string.nightmare_bots);
            if (mode.equals("ASCENSION")) return context.getString(R.string.ascension_game);
            if (mode.equals("HEXAKILL")) return context.getString(R.string.hexakill);
            if (mode.equals("KING_PORO")) return context.getString(R.string.king_poro_game);
            if (mode.equals("COUNTER_PICK")) return context.getString(R.string.counter_pick);
            return context.getString(R.string.unknown);
        } catch (JSONException e) {
            return context.getString(R.string.unknown);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Returns the KDA already formatted.
    public static String getKDA(JSONObject game) {
        Integer kills, deaths, assists;
        try {
            kills = game.getJSONObject("stats").getInt("championsKilled");
        } catch (JSONException e) {
            kills = 0;
        }
        try {
            deaths = game.getJSONObject("stats").getInt("numDeaths");
        } catch (JSONException e) {
            deaths = 0;
        }
        try {
            assists = game.getJSONObject("stats").getInt("assists");
        } catch (JSONException e) {
            assists = 0;
        }
        return kills + "/" + deaths + "/" + assists;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getKills(JSONObject game) {
        Integer num;
        try {
            num = game.getJSONObject("stats").getInt("championsKilled");
        } catch (JSONException e) {
            num = 0;
        }
        return num.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getDeaths(JSONObject game) {
        Integer num;
        try {
            num = game.getJSONObject("stats").getInt("numDeaths");
        } catch (JSONException e) {
            num = 0;
        }
        return num.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getAssists(JSONObject game) {
        Integer num;
        try {
            num = game.getJSONObject("stats").getInt("assists");
        } catch (JSONException e) {
            num = 0;
        }
        return num.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getLargestMultikill(Context context, JSONObject game) {
        try {
            int largestDick = game.getJSONObject("stats").getInt("largestMultiKill");
            if (largestDick == 2) return " - " + context.getString(R.string.double_kill);
            if (largestDick == 3) return " - " + context.getString(R.string.triple_kill);
            if (largestDick == 4) return " - " + context.getString(R.string.quadra_kill);
            if (largestDick == 5) return " - " + context.getString(R.string.pentakill);
            return "";
        } catch (JSONException e) {
            return "";
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getLargestMultikillSimpleFormat(JSONObject game) {
        try {
            Integer largestDick = game.getJSONObject("stats").getInt("largestMultiKill");
            return largestDick.toString();
        } catch (JSONException e) {
            return "0";
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getVictory(Context context, JSONObject game) {
        try {
            boolean win = game.getJSONObject("stats").getBoolean("win");
            if (win)
                return context.getString(R.string.victory);
            else if (!win)
                return context.getString(R.string.defeat);
            else
                return context.getString(R.string.unknown);
        } catch (JSONException e) {
            return context.getString(R.string.unknown);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getLargestKillingspree(JSONObject game) {
        try {
            Integer largestDick = game.getJSONObject("stats").getInt("largestKillingSpree");
            return largestDick.toString();
        } catch (JSONException e) {
            return "0";
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getTurretsDestroyed(JSONObject game) {
        try {
            Integer largest = game.getJSONObject("stats").getInt("turretsKilled");
            return largest.toString();
        } catch (JSONException e) {
            return "0";
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getInhibitorsDestroyed(JSONObject game) {
        try {
            Integer largest = game.getJSONObject("stats").getInt("barracksKilled");
            return largest.toString();
        } catch (JSONException e) {
            return "0";
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getMinionsKilled(JSONObject game) {
        try {
            Integer largest = game.getJSONObject("stats").getInt("minionsKilled");
            return largest.toString();
        } catch (JSONException e) {
            return "0";
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getNeutralMinionsKilled(JSONObject game) {
        try {
            Integer largest = game.getJSONObject("stats").getInt("neutralMinionsKilled");
            return largest.toString();
        } catch (JSONException e) {
            return "0";
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getGoldEarned(JSONObject game) {
        try {
            Integer largest = game.getJSONObject("stats").getInt("goldEarned");
            return largest.toString();
        } catch (JSONException e) {
            return "0";
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getChampionLevel(Context context, JSONObject game) {
        try {
            Integer largest = game.getJSONObject("stats").getInt("level");
            return largest.toString();
        } catch (JSONException e) {
            return context.getString(R.string.unknown);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getTimePlayed(Context context, JSONObject game) {
        try {
            Integer secs = game.getJSONObject("stats").getInt("timePlayed");
            Integer minutes = secs / 60;
            Integer mod = secs % 60;
            String modString = mod.toString();
            if (modString.length() == 1) modString = "0" + modString;
            return minutes + ":" + modString;
        } catch (JSONException e) {
            return context.getString(R.string.unknown);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getIPEarned(Context context, JSONObject game) {
        try {
            Integer largest = game.getInt("ipEarned");
            return largest.toString();
        } catch (JSONException e) {
            return context.getString(R.string.unknown);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getItem0(JSONObject game) {
        Integer num;
        try {
            num = game.getJSONObject("stats").getInt("item0");
        } catch (JSONException e) {
            num = -1;
        }
        return num.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getItem1(JSONObject game) {
        Integer num;
        try {
            num = game.getJSONObject("stats").getInt("item1");
        } catch (JSONException e) {
            num = -1;
        }
        return num.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getItem2(JSONObject game) {
        Integer num;
        try {
            num = game.getJSONObject("stats").getInt("item2");
        } catch (JSONException e) {
            num = -1;
        }
        return num.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getItem3(JSONObject game) {
        Integer num;
        try {
            num = game.getJSONObject("stats").getInt("item3");
        } catch (JSONException e) {
            num = -1;
        }
        return num.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getItem4(JSONObject game) {
        Integer num;
        try {
            num = game.getJSONObject("stats").getInt("item4");
        } catch (JSONException e) {
            num = -1;
        }
        return num.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getItem5(JSONObject game) {
        Integer num;
        try {
            num = game.getJSONObject("stats").getInt("item5");
        } catch (JSONException e) {
            num = -1;
        }
        return num.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getItem6(JSONObject game) {
        Integer num;
        try {
            num = game.getJSONObject("stats").getInt("item6");
        } catch (JSONException e) {
            num = -1;
        }
        return num.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getSpell1(JSONObject game) {
        try {
            Integer spell;
            spell = game.getInt("spell1");
            return spell.toString();

        } catch (JSONException e) {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getSpell2(JSONObject game) {
        try {
            Integer spell;
            spell = game.getInt("spell2");
            return spell.toString();

        } catch (JSONException e) {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static List<String> getTeam100(JSONObject game, Long summonerId) {
        List<String> summoners = new LinkedList<>();
        try {
            JSONArray fellows = game.getJSONArray("fellowPlayers");
            for (int i = 0; i < fellows.length(); i++)
                // Add the summoner to the list if he/she belongs to team 100.
                if (fellows.getJSONObject(i).getInt("teamId") == 100) {
                    summoners.add(String.valueOf(fellows.getJSONObject(i).getLong("summonerId")));
                }
            // Finally, check if our summoner belongs to team 100.
            if (game.getInt("teamId") == 100) summoners.add(summonerId.toString());
            return summoners;
        } catch (JSONException e) {
            return new LinkedList<>();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static List<String> getTeam200(JSONObject game, Long summonerId) {
        List<String> summoners = new LinkedList<>();
        try {
            JSONArray fellows = game.getJSONArray("fellowPlayers");
            for (int i = 0; i < fellows.length(); i++)
                // Add the summoner to the list if he/she belongs to team 200.
                if (fellows.getJSONObject(i).getInt("teamId") == 200) {
                    summoners.add(String.valueOf(fellows.getJSONObject(i).getLong("summonerId")));
                }
            // Finally, check if our summoner belongs to team 200.
            if (game.getInt("teamId") == 200) summoners.add(summonerId.toString());
            return summoners;
        } catch (JSONException e) {
            return new LinkedList<>();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static Long getCurrentGameLength(JSONObject currentGame) {
        try {
            return currentGame.getLong("gameLength");
        } catch (JSONException e) {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getGameQueueConfigId(Context context, JSONObject currentGame) {
        try {
            Long mode = currentGame.getLong("gameQueueConfigId");
            if (mode.equals(Long.valueOf(0))) return context.getString(R.string.custom);
            if (mode.equals(Long.valueOf(2))) return context.getString(R.string.blind_pick);
            if (mode.equals(Long.valueOf(8))) return context.getString(R.string.normal_3x3);
            if (mode.equals(Long.valueOf(14))) return context.getString(R.string.draft_pick);
            if (mode.equals(Long.valueOf(4))) return context.getString(R.string.ranked_solo);
            if (mode.equals(Long.valueOf(61))) return context.getString(R.string.team_builder);
            if (mode.equals(Long.valueOf(65))) return context.getString(R.string.random_pick);
            if ((mode.equals(Long.valueOf(7))) ||
                    (mode.equals(Long.valueOf(31))) ||
                    (mode.equals(Long.valueOf(32))) ||
                    (mode.equals(Long.valueOf(33))) ||
                    (mode.equals(Long.valueOf(25))) ||
                    (mode.equals(Long.valueOf(52))) ||
                    (mode.equals(Long.valueOf(83))) ||
                    (mode.equals(Long.valueOf(91))) ||
                    (mode.equals(Long.valueOf(92))) ||
                    (mode.equals(Long.valueOf(93)))) return context.getString(R.string.bot);
            if ((mode.equals(Long.valueOf(9))) ||
                    (mode.equals(Long.valueOf(6))) ||
                    (mode.equals(Long.valueOf(41))) ||
                    (mode.equals(Long.valueOf(42)))) return context.getString(R.string.ranked);
            if (mode.equals(Long.valueOf(96))) return context.getString(R.string.ascension_game);
            if (mode.equals(Long.valueOf(98))) return context.getString(R.string.hexakill);
            if (mode.equals(Long.valueOf(300))) return context.getString(R.string.king_poro_game);
            if (mode.equals(Long.valueOf(310))) return context.getString(R.string.counter_pick);
            return context.getString(R.string.unknown);
        } catch (JSONException e) {
            return context.getString(R.string.unknown);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getLeagueTier(JSONObject leagueEntry, Context context) {
        String league = "";
        try {
            if (leagueEntry.getString("tier").equals("BRONZE"))
                league = context.getString(R.string.bronze);
            if (leagueEntry.getString("tier").equals("SILVER"))
                league = context.getString(R.string.silver);
            if (leagueEntry.getString("tier").equals("GOLD"))
                league = context.getString(R.string.Gold);
            if (leagueEntry.getString("tier").equals("PLATINUM"))
                league = context.getString(R.string.platinum);
            if (leagueEntry.getString("tier").equals("DIAMOND"))
                league = context.getString(R.string.diamond);
            if (leagueEntry.getString("tier").equals("MASTER"))
                league = context.getString(R.string.master);
            if (leagueEntry.getString("tier").equals("CHALLENGER"))
                league = context.getString(R.string.challenger);
        } catch (JSONException e) {
            Log.e("asdf", leagueEntry.toString());
            return context.getString(R.string.an_unknown_error_occured) + e.toString();
        }

        try {
            String division = leagueEntry.getJSONArray("entries").getJSONObject(0).getString("division");
            league = league + " " + division;
            return league;
        } catch (JSONException e) {
            return league;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getLeaguePointsOrMiniSeries(JSONObject leagueEntry, Context context) {
        Integer leaguePoints;
        String result;
        try {
            result = leagueEntry.getJSONArray("entries").getJSONObject(0).getJSONObject("miniSeries").getString("progress");
            return result.replace('N', '-').replace('L', 'x').replace('W', 'o');
        } catch (JSONException e) {
            // Do nothing (the next try-catch will be executed; this is, the man is not in his promotion series)
        }

        try {
            leaguePoints = leagueEntry.getJSONArray("entries").getJSONObject(0).getInt("leaguePoints");
            result = leaguePoints.toString() + " " + context.getString(R.string.lp);
        } catch (JSONException e) {
            return context.getString(R.string.wtf_this_should_never_happen);
        }
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getLeagueQueue(JSONObject leagueEntry, Context context) {
        String leagueInJSON = "", leagueQueue;
        try {
            leagueInJSON = leagueEntry.getString("queue");
        } catch (JSONException e) {
            leagueQueue = context.getString(R.string.unknown);
            return leagueQueue;
        }
        switch (leagueInJSON) {
            case "RANKED_TEAM_5x5":
                leagueQueue = context.getString(R.string.ranked_team_5x5_queuetype);
                break;
            case "RANKED_TEAM_3x3":
                leagueQueue = context.getString(R.string.ranked_team_3x3_queuetype);
                break;
            default:
                leagueQueue = leagueInJSON;
                break;
        }
        return leagueQueue;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static List<LoLParticipantData> getTeam100Participants(JSONObject game) {
        List<LoLParticipantData> summoners = new LinkedList<>();
        try {
            JSONArray participants = game.getJSONArray("participants");
            for (int i = 0; i < participants.length(); i++)
                // Parse the summoner if he/she belongs to team 100.
                if (participants.getJSONObject(i).getLong("teamId") == 100) { // WELL OK HEHE
                    // If it's a bot:
                    if (participants.getJSONObject(i).getBoolean("bot")) {
                        summoners.add(new LoLParticipantData(
                                true,
                                participants.getJSONObject(i).getLong("championId"),
                                null,
                                participants.getJSONObject(i).getString("summonerName")));
                    } else { // Human player.
                        summoners.add(new LoLParticipantData(
                                false,
                                participants.getJSONObject(i).getLong("championId"),
                                participants.getJSONObject(i).getLong("summonerId"),
                                participants.getJSONObject(i).getString("summonerName")));
                    }
                }
            return summoners;
        } catch (JSONException e) {
            Log.e("error YOLO", e.toString());
            return new LinkedList<>();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static List<LoLParticipantData> getTeam200Participants(JSONObject game) {
        List<LoLParticipantData> summoners = new LinkedList<>();
        try {
            JSONArray participants = game.getJSONArray("participants");
            for (int i = 0; i < participants.length(); i++)
                // Parse the summoner if he/she belongs to team 100.
                if (participants.getJSONObject(i).getLong("teamId") == 200) { // WELL OK HEHE
                    // If it's a bot:
                    if (participants.getJSONObject(i).getBoolean("bot")) {
                        summoners.add(new LoLParticipantData(
                                true,
                                participants.getJSONObject(i).getLong("championId"),
                                null,
                                participants.getJSONObject(i).getString("summonerName")));
                    } else { // Human player.
                        summoners.add(new LoLParticipantData(
                                false,
                                participants.getJSONObject(i).getLong("championId"),
                                participants.getJSONObject(i).getLong("summonerId"),
                                participants.getJSONObject(i).getString("summonerName")));
                    }
                }
            return summoners;
        } catch (JSONException e) {
            Log.e("petó", e.toString());
            return new LinkedList<>();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getMasteriesResume(JSONArray masteries) {
        Integer attack = 0, defense = 0, utility = 0;
        for (int i = 0; i < masteries.length(); i++) {
            try {
                int rank = masteries.getJSONObject(i).getInt("rank");
                Long masteryId = masteries.getJSONObject(i).getLong("masteryId");
                if ((masteryId >= 4100) && (masteryId <= 4199)) {
                    attack += rank; //rank is the number of masteries of that type
                }
                if ((masteryId >= 4200) && (masteryId <= 4299)) {
                    defense += rank; //rank is the number of masteries of that type
                }
                if ((masteryId >= 4300) && (masteryId <= 4399)) {
                    utility += rank; //rank is the number of masteries of that type
                }
            } catch (JSONException e) {
                Log.e("summonersApp", "Error: " + e.toString());
            }
        }

        return attack + "/" + defense + "/" + utility;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getStats(Context context, JSONArray playerStatsSummaries) {
        String stats = "";
        for (int i = 0; i < playerStatsSummaries.length(); i++) { // For each type.
            // First: get the name of the queue.
            String queueName;
            try {
                queueName = playerStatsSummaries.getJSONObject(i).getString("playerStatSummaryType");
                stats += "\n" + queueName + ":\n";
            } catch (JSONException e) {
                e.printStackTrace();
                return context.getString(R.string.wtf_this_should_never_happen);
            }

            // Second: get the parameters.
            String aggregatedStatsStr = "";
            try {
                JSONObject aggregatedStats = playerStatsSummaries.getJSONObject(i).getJSONObject("aggregatedStats");
                String wins = context.getString(R.string.wins),
                        neutralMinions = context.getString(R.string.neutr_min_kills),
                        minions = context.getString(R.string.minions_kills),
                        championKills = context.getString(R.string.champ_kills),
                        assists = context.getString(R.string.assists_done),
                        turrets = context.getString(R.string.turrets_kill);
                Integer winsInt = playerStatsSummaries.getJSONObject(i).getInt("wins");

                Integer championKillsInt = aggregatedStats.getInt("totalChampionKills");
                Integer assistsInt = aggregatedStats.getInt("totalAssists");
                Integer turretsInt = 0;
                if (!queueName.equals("OdinUnranked"))
                    turretsInt = aggregatedStats.getInt("totalTurretsKilled");
                Integer neutralMinionsInt = 0;
                if ((!queueName.equals("AramUnranked5x5")) && (!queueName.equals("OdinUnranked")))
                    neutralMinionsInt = aggregatedStats.getInt("totalNeutralMinionsKilled");
                Integer minionsInt = 0;
                if ((!queueName.equals("AramUnranked5x5")) && (!queueName.equals("OdinUnranked")))
                    minionsInt = aggregatedStats.getInt("totalMinionKills");

                // Add the things to the string.
                wins += " " + winsInt + "\n";
                neutralMinions += " " + neutralMinionsInt + "\n";
                minions += " " + minionsInt + "\n";
                championKills += " " + championKillsInt + "\n";
                assists += " " + assistsInt + "\n";
                turrets += " " + turretsInt + "\n";

                aggregatedStatsStr += wins + championKills + assists;
                if (!queueName.equals("OdinUnranked"))
                    aggregatedStatsStr += turrets;
                if ((!queueName.equals("AramUnranked5x5")) && (!queueName.equals("OdinUnranked")))
                    aggregatedStatsStr += minions + neutralMinions;

            } catch (JSONException e) {
                e.printStackTrace();
                aggregatedStatsStr = context.getString(R.string.unknown) + "\n";
            }
            stats += aggregatedStatsStr;

        }
        return stats
                .replace("CoopVsAI3x3", context.getString(R.string.stats_coop3x3))
                .replace("CoopVsAI", context.getString(R.string.stats_coopvsai))
                .replace("OdinUnranked", context.getString(R.string.stats_odin))
                .replace("RankedTeam3x3", context.getString(R.string.stats_rank5x5))
                .replace("RankedTeam5x5", context.getString(R.string.stats_rank3x3))
                .replace("AramUnranked5x5", context.getString(R.string.stats_aram))
                .replace("Unranked3x3", context.getString(R.string.stats_unranked3x3))
                .replace("Unranked", context.getString(R.string.stats_unranked))
                .replace("RankedSolo5x5", context.getString(R.string.stats_ranked))
                .replace("CounterPick", context.getString(R.string.stats_counterpick))
                .replace("CAP5x5", context.getString(R.string.stats_teambuilder));
    }
}
