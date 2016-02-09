package info.summoners.app.rest.lol.util;

// LoLDataParser.java

// A class that gathers the info of an in-game participant.

public class LoLParticipantData {
    private boolean bot;
    private Long champId;
    private Long summonerId;
    private String summonerName;
    private String champKey;

    public LoLParticipantData(boolean bot, Long champId, Long summonerId, String summonerName) {
        this.bot = bot;
        this.champId = champId;
        this.summonerId = summonerId;
        this.summonerName = summonerName;
        this.champKey = "";
    }

    public boolean getBot() {
        return this.bot;
    }

    public Long getChampId() {
        return this.champId;
    }

    public Long getSummonerId() {
        return this.summonerId;
    }

    public String getSummonerName() {
        return this.summonerName;
    }

    public String getChampKey() {
        return this.champKey;
    }

    public void setChampKey(String champKey) {
        this.champKey = champKey;
    }
}
