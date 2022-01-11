package BattleTowers;

import BattleTowers.events.CoolExampleEvent;
import BattleTowers.monsters.*;
import BattleTowers.subscribers.PetrifyingGazeApplyPowerSubscriber;
import BattleTowers.subscribers.TriggerSlimeFilledRoomPowerPostExhaustSubscriber;
import BattleTowers.util.KeywordWithProper;
import BattleTowers.util.TextureLoader;
import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import basemod.interfaces.EditKeywordsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.powers.AbstractPower;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@SpireInitializer
public class BattleTowers implements
        PostInitializeSubscriber,
        EditStringsSubscriber,
        EditKeywordsSubscriber
{
    public static final Logger logger = LogManager.getLogger(BattleTowers.class);
    private static SpireConfig modConfig = null;

    public static final String modid = "battleTowers"; //same as pom, for keywords

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

        BaseMod.subscribe(new PetrifyingGazeApplyPowerSubscriber());
        BaseMod.subscribe(new TriggerSlimeFilledRoomPowerPostExhaustSubscriber());

        addMonsters();
        addEvents();
    }

    private static void addMonsters() {
        BaseMod.addMonster(VoodooDoll.ID, (BaseMod.GetMonster) VoodooDoll::new);
        BaseMod.addMonster(Gorgon.ID, (BaseMod.GetMonster) Gorgon::new);
        BaseMod.addMonster(DoomedSoul.ID, (BaseMod.GetMonster) DoomedSoul::new);
        BaseMod.addMonster(GigaSlime.ID, (BaseMod.GetMonster) GigaSlime::new);
        BaseMod.addMonster(Encounters.MINOTAUR_GLADIATOR_AND_FRIEND, () -> new MonsterGroup(
                new AbstractMonster[] {
                        new BurningShambler(-350.0F, 0.0F),
                        new MinotaurGladiator(100.0F, 0.0F)
                }));
    }

    private static void addEvents() {
        BaseMod.addEvent(CoolExampleEvent.ID, CoolExampleEvent.class, ""); //Only appears in dungeons with the ID "", which should be none.
    }

    @Override
    public void receiveEditStrings() {
        String lang = defaultLoc();

        BaseMod.loadCustomStringsFile(CardStrings.class, makeLocalizationPath(lang + "/cards.json"));
        BaseMod.loadCustomStringsFile(EventStrings.class, makeLocalizationPath(lang + "/events.json"));
        BaseMod.loadCustomStringsFile(MonsterStrings.class, makeLocalizationPath(lang + "/monsters.json"));
        BaseMod.loadCustomStringsFile(PotionStrings.class, makeLocalizationPath(lang + "/potions.json"));
        BaseMod.loadCustomStringsFile(PowerStrings.class, makeLocalizationPath(lang + "/powers.json"));
        BaseMod.loadCustomStringsFile(UIStrings.class, makeLocalizationPath(lang + "/ui.json"));

        lang = getLangString();
        if (lang.equals(defaultLoc())) return;

        try
        {
            BaseMod.loadCustomStringsFile(CardStrings.class, makeLocalizationPath(lang + "/cards.json"));
            BaseMod.loadCustomStringsFile(EventStrings.class, makeLocalizationPath(lang + "/events.json"));
            BaseMod.loadCustomStringsFile(MonsterStrings.class, makeLocalizationPath(lang + "/monsters.json"));
            BaseMod.loadCustomStringsFile(PotionStrings.class, makeLocalizationPath(lang + "/potions.json"));
            BaseMod.loadCustomStringsFile(PowerStrings.class, makeLocalizationPath(lang + "/powers.json"));
            BaseMod.loadCustomStringsFile(UIStrings.class, makeLocalizationPath(lang + "/ui.json"));
        }
        catch (Exception e)
        {
            logger.error("Failed to load other language strings. ");
            logger.error(e.getMessage());
        }
    }

    @Override
    public void receiveEditKeywords() {
        String lang = defaultLoc();

        Gson gson = new Gson();
        String json = Gdx.files.internal(makeLocalizationPath(lang + "/keywords.json")).readString(String.valueOf(StandardCharsets.UTF_8));
        KeywordWithProper[] keywords = gson.fromJson(json, KeywordWithProper[].class);

        for (KeywordWithProper keyword : keywords) {
            BaseMod.addKeyword(modid, keyword.PROPER_NAME, keyword.NAMES, keyword.DESCRIPTION);
        }

        try {
            lang = getLangString();

            FileHandle localized = Gdx.files.internal(makeLocalizationPath(lang + "/Keywords.json"));
            if (localized.exists()) {
                keywords = gson.fromJson(localized.readString(String.valueOf(StandardCharsets.UTF_8)), KeywordWithProper[].class);
                for (KeywordWithProper keyword : keywords) {
                    BaseMod.addKeyword(modid, keyword.PROPER_NAME, keyword.NAMES, keyword.DESCRIPTION);
                }
            }
        }
        catch (Exception e) {
            logger.info("Failed to load localized keywords.");
            e.printStackTrace();
        }
    }

    private String defaultLoc() {
        return "eng";
    }
    private String getLangString()
    {
        return Settings.language.name().toLowerCase();
    }

    public static String makePath(String resourcePath) {
        return getModID() + "Resources/" + resourcePath;
    }

    public static String makeImagePath(String resourcePath) {
        return getModID() + "Resources/img/" + resourcePath;
    }

    public static String makeCardPath(String resourcePath) {
        return getModID() + "Resources/img/cards/" + resourcePath;
    }
    public static String makeUIPath(String resourcePath) {
        return getModID() + "Resources/img/ui/" + resourcePath;
    }

    public static String makePowerPath(String resourcePath) {
        return getModID() + "Resources/img/power/" + resourcePath;
    }

    public static String makeRelicPath(String resourcePath) {
        return getModID() + "Resources/img/relics/" + resourcePath;
    }

    public static String makeLocalizationPath(String resourcePath) {
        return getModID() + "Resources/loc/" + resourcePath;
    }

    public static void LoadPowerImage(AbstractPower power) {
        Texture tex84 = TextureLoader.getTexture(makePowerPath(removeModId(power.ID) + "84.png"));
        Texture tex32 = TextureLoader.getTexture(makePowerPath(removeModId(power.ID) + "32.png"));
        power.region128 = new TextureAtlas.AtlasRegion(tex84, 0, 0, 84, 84);
        power.region48 = new TextureAtlas.AtlasRegion(tex32, 0, 0, 32, 32);
    }

    public static String getModID() {
        return "battleTowers";
    }

    public static String makeID(String input) {
        return getModID() + ":" + input;
    }

    public static String removeModId(String id) {
        if (id.startsWith(getModID() + ":")) {
            return id.substring(id.indexOf(':') + 1);
        } else {
            logger.warn("Missing mod id on: " + id);
            return id;
        }
    }
}