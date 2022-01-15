package BattleTowers;

import BattleTowers.RazIntent.CustomIntent;
import BattleTowers.cardmod.SlimyCardmod;
import BattleTowers.cards.*;
import BattleTowers.events.*;
import BattleTowers.monsters.*;
import BattleTowers.monsters.CardboardGolem.CardboardGolem;
import BattleTowers.monsters.chess.queen.Queen;
import BattleTowers.monsters.chess.queen.customintents.QueenDrainIntent;
import BattleTowers.monsters.executiveslime.ExecutiveSlime;
import BattleTowers.relics.*;
import BattleTowers.subscribers.PetrifyingGazeApplyPowerSubscriber;
import BattleTowers.subscribers.TriggerSlimeFilledRoomPowerPostExhaustSubscriber;
import BattleTowers.util.KeywordWithProper;
import BattleTowers.util.TextureLoader;
import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import basemod.helpers.CardBorderGlowManager;
import basemod.helpers.CardModifierManager;
import basemod.helpers.RelicType;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.purple.Alpha;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.monsters.exordium.Cultist;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@SpireInitializer
public class BattleTowers implements
        PostInitializeSubscriber,
        EditStringsSubscriber,
        EditKeywordsSubscriber,
        EditRelicsSubscriber,
        EditCardsSubscriber
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

        CardBorderGlowManager.addGlowInfo(new CardBorderGlowManager.GlowInfo() {
            private final String ID = makeID("SlimyGlow");
            @Override
            public boolean test(AbstractCard card) {
                return CardModifierManager.hasModifier(card, SlimyCardmod.ID);
            }
            @Override
            public Color getColor(AbstractCard abstractCard) {
                return new Color(0.7F, 0.55F, 0.8F, 1.0F);
            }
            @Override
            public String glowID() {
                return ID;
            }
        });
    }

    private static void addMonsters() {
        CustomIntent.add(new QueenDrainIntent());

        //Normal Enemies
        BaseMod.addMonster(Encounters.METAL_LOUSES,  () -> new MonsterGroup(
                new AbstractMonster[] {
                        new SilverLouse(-400.0F, 0.0F),
                        new GoldenLouse(-150.0F, 0.0F),
                        new SilverLouse(100.0F, 0.0F),
                }));
        BaseMod.addMonster(Encounters.ICE_AND_FIRE_SLIME, () -> new MonsterGroup(
                new AbstractMonster[]{
                        new FireSlimeL(-385.0F, 20.0F),
                        new IceSlimeL(120.0F, -8.0F)
                }));
        BaseMod.addMonster(Trenchcoat.ID, (BaseMod.GetMonster) Trenchcoat::new);
        BaseMod.addMonster(DoomedSoul.ID, (BaseMod.GetMonster) DoomedSoul::new);
        BaseMod.addMonster(Encounters.MINOTAUR_GLADIATOR_AND_FRIEND, () -> new MonsterGroup(
                new AbstractMonster[] {
                        new BurningShambler(-350.0F, 0.0F),
                        new MinotaurGladiator(100.0F, 0.0F)
                }));
        BaseMod.addMonster(Encounters.DBZ_PUNS, () -> new MonsterGroup(
                new AbstractMonster[] {
                        new Cawcawrot(-250.0F, 0.0F),
                        new Veggieta(100.0F, 0.0F)
                }));
        BaseMod.addMonster(makeID("CardboardGolem"), new BaseMod.GetMonster() {
            @Override
            public AbstractMonster get() {
                return new CardboardGolem();
            }
        });
        BaseMod.addMonster(Encounters.CULTIST_ARMORER, () -> new MonsterGroup(
                new AbstractMonster[] {
                        new CultistArmorer(-250.0F, 0.0F),
                        new Cultist(100.0F, 0.0F)
                }));
        BaseMod.addMonster(tneisnarT.ID, (BaseMod.GetMonster) tneisnarT::new);

        //Elites
        BaseMod.addMonster(Encounters.ELEMENTAL_SENTRIES, () -> new MonsterGroup(
                new AbstractMonster[] {
                        new SentryRed(-500.0F, 25.0F),
                        new SentryGreen(-320.0F, 10.0F),
                        new SentryPurple(-140.0F, 30.0F),
                        new SentryHuge(140.0F, 0.0F)
                }));
        BaseMod.addMonster(VoodooDoll.ID, (BaseMod.GetMonster) VoodooDoll::new);
        BaseMod.addMonster(Gorgon.ID, (BaseMod.GetMonster) Gorgon::new);
        BaseMod.addMonster(GigaSlime.ID, (BaseMod.GetMonster) GigaSlime::new);
        BaseMod.addMonster(ItozusTheWindwalker.ID,(BaseMod.GetMonster) ItozusTheWindwalker::new);
        BaseMod.addMonster(ZastraszTheJusticar.ID,(BaseMod.GetMonster) ZastraszTheJusticar::new);
        //Bosses
        BaseMod.addMonster(CardboardGolem.ID, (BaseMod.GetMonster) CardboardGolem::new);
        BaseMod.addMonster(Dijinn.ID, (BaseMod.GetMonster) Dijinn::new);
        BaseMod.addMonster(AlphabetBoss.ID, (BaseMod.GetMonster) AlphabetBoss::new);
        BaseMod.addMonster(ExecutiveSlime.ID, (BaseMod.GetMonster) ExecutiveSlime::new);
        BaseMod.addMonster(Queen.ID, (BaseMod.GetMonster) Queen::new);

    }

    private static void addEvents() {
        BaseMod.addEvent(CoolExampleEvent.ID, CoolExampleEvent.class, ""); //Only appears in dungeons with the ID "", which should be none.
        BaseMod.addEvent(EmeraldFlame.ID, EmeraldFlame.class, "");
        BaseMod.addEvent(OttoEvent.ID, OttoEvent.class, ""); //Only appears in dungeons with the ID "", which should be none.
        BaseMod.addEvent(ArmorerEvent.ID, ArmorerEvent.class, ""); //Only appears in dungeons with the ID "", which should be none.
        BaseMod.addEvent(BannerSageEvent.ID, BannerSageEvent.class, ""); //Only appears in dungeons with the ID "", which should be none.
        BaseMod.addEvent(GenieLampEvent.ID, GenieLampEvent.class, "");
        BaseMod.addEvent(RoarOfTheCrowd.ID, RoarOfTheCrowd.class, "");
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
        BaseMod.loadCustomStringsFile(RelicStrings.class, makeLocalizationPath(lang + "/relics.json"));
        BaseMod.loadCustomStringsFile(OrbStrings.class, makeLocalizationPath(lang + "/orbs.json"));

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
            BaseMod.loadCustomStringsFile(RelicStrings.class, makeLocalizationPath(lang + "/relics.json"));
            BaseMod.loadCustomStringsFile(OrbStrings.class, makeLocalizationPath(lang + "/orbs.json"));
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

    public static String makeMonsterPath(String resourcePath) {
        return getModID() + "Resources/img/monsters/" + resourcePath;
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

    @Override
    public void receiveEditRelics() {
        BaseMod.addRelic(new CardboardHeart(), RelicType.SHARED);
        BaseMod.addRelic(new BucketOfSlime(), RelicType.SHARED);
        BaseMod.addRelic(new OttosDeck(), RelicType.SHARED);
        BaseMod.addRelic(new WarBannerSnecko(), RelicType.SHARED);
        BaseMod.addRelic(new WarBannerCultist(), RelicType.SHARED);
        BaseMod.addRelic(new WarBannerLouse(), RelicType.SHARED);
        BaseMod.addRelic(new WarBannerNob(), RelicType.SHARED);
        BaseMod.addRelic(new Torch(), RelicType.SHARED);
        UnlockTracker.markRelicAsSeen(Torch.ID);
        BaseMod.addRelic(new Lucky(), RelicType.SHARED);
        BaseMod.addRelic(new IronPotHelmet(), RelicType.SHARED);
        BaseMod.addRelic(new DijinnLamp(), RelicType.SHARED);
        BaseMod.addRelic(new CursedDoll(), RelicType.SHARED);
        BaseMod.addRelic(new PromiseOfGold(), RelicType.SHARED);
        BaseMod.addRelic(new ClericsBlessing(), RelicType.SHARED);
        BaseMod.addRelic(new ArmorersMask(), RelicType.SHARED);
        BaseMod.addRelic(new AlphabetSoup(), RelicType.SHARED);
        BaseMod.addRelic(new GorgonHead(), RelicType.SHARED);
        BaseMod.addRelic(new SlimeFilledFlask(), RelicType.SHARED);
    }
        
    public static String removeModId(String id) {
        if (id.startsWith(getModID() + ":")) {
            return id.substring(id.indexOf(':') + 1);
        } else {
            logger.warn("Missing mod id on: " + id);
            return id;
        }
    }

    @Override
    public void receiveEditCards() {
        BaseMod.addCard(new BishopsPrayer());
        BaseMod.addCard(new KingsCommand());
        BaseMod.addCard(new KnightsManeuver());
        BaseMod.addCard(new PawnsAdvance());
        BaseMod.addCard(new QueensGrace());
        BaseMod.addCard(new RooksCharge());
        BaseMod.addCard(new CursedTapestry());
        BaseMod.addCard(new Greedy());
        BaseMod.addCard(new DarkEnchantment());
        BaseMod.addCard(new Granted());
        BaseMod.addCard(new Knowledge());
        BaseMod.addCard(new AvertYourGaze());
        BaseMod.addCard(new SlimeElixir());
    }
}