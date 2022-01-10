package BattleTowers;

import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

@SpireInitializer
public class BattleTowers implements
        PostInitializeSubscriber,
        EditStringsSubscriber
{
    public static final Logger logger = LogManager.getLogger(BattleTowers.class);
    private static SpireConfig modConfig = null;

    public static void initialize() {
        BaseMod.subscribe(new BattleTowers());

        try {
            Properties defaults = new Properties();
            defaults.put("Test", Boolean.toString(true));
            modConfig = new SpireConfig("BattleTowers", "Config", defaults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean shouldT() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("Test");
    }

    @Override
    public void receivePostInitialize() {
        ModPanel settingsPanel = new ModPanel();

        UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("Config"));

        ModLabeledToggleButton TBtn = new ModLabeledToggleButton(uiStrings.TEXT_DICT.get("Test"), 350, 700, Settings.CREAM_COLOR, FontHelper.charDescFont, shouldT(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("Test", button.enabled);
                        try {
                            modConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        settingsPanel.addUIElement(TBtn);

        BaseMod.registerModBadge(ImageMaster.loadImage("battleTowersResources/img/modBadge.png"), "Battle Towers", "erasels", "TODO", settingsPanel);


    }

    @Override
    public void receiveEditStrings() {
        BaseMod.loadCustomStringsFile(CardStrings.class, getModID() + "Resources/loc/" + locPath() + "/cards.json");
        BaseMod.loadCustomStringsFile(UIStrings.class, getModID() + "Resources/loc/" + locPath() + "/ui.json");
    }

    private String locPath() {
        return "eng";
    }

    public static String makePath(String resourcePath) {
        return getModID() + "Resources/" + resourcePath;
    }

    public static String makeImagePath(String resourcePath) {
        return getModID() + "Resources/images/" + resourcePath;
    }

    public static String makeCardPath(String resourcePath) {
        return getModID() + "Resources/images/cards/" + resourcePath;
    }
    public static String makeUIPath(String resourcePath) {
        return getModID() + "Resources/images/ui/" + resourcePath;
    }

    public static String makePowerPath(String resourcePath) {
        return getModID() + "Resources/images/power/" + resourcePath;
    }

    public static String makeRelicPath(String resourcePath) {
        return getModID() + "Resources/images/relics/" + resourcePath;
    }

    public static String getModID() {
        return "battleTowers";
    }

    public static String makeID(String input) {
        return getModID() + ":" + input;
    }
}