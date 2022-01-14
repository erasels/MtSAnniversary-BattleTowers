package BattleTowers.blights;

import BattleTowers.BattleTowers;
import BattleTowers.util.TextureLoader;
import com.megacrit.cardcrawl.blights.AbstractBlight;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;

public class AbstractBTBlight extends AbstractBlight {

    public static RelicStrings STRINGS;
    public boolean usedUp;
    private String imgUrl;

    public AbstractBTBlight(String id, String textureString) {
        super(id, "", "", "", true);

        STRINGS = CardCrawlGame.languagePack.getRelicStrings(id);
        description = getDescription();
        name = STRINGS.NAME;

        imgUrl = textureString + ".png";
        img = TextureLoader.getTexture(BattleTowers.makeRelicPath(imgUrl));
        outlineImg = TextureLoader.getTexture(BattleTowers.makeRelicPath("outline/" + imgUrl));

        this.tips.clear();
        this.tips.add(new PowerTip(name, description));
    }

    public void usedUp() {
        usedUp = true;
        description = getUsedUpMsg();
        this.tips.clear();
        this.tips.add(new PowerTip(this.name, this.description));
        initializeTips();
        img = TextureLoader.getTexture(BattleTowers.makeRelicPath("used/" + imgUrl));
    }

    private static String getDescription() {
        return STRINGS.DESCRIPTIONS[0];
    }
    private static String getUsedUpMsg() {
        return STRINGS.DESCRIPTIONS[1];
    }
}