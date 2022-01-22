package BattleTowers;

import BattleTowers.RazIntent.CustomIntent;
import BattleTowers.cardmods.SlimyCardmod;
import BattleTowers.events.*;
import BattleTowers.monsters.*;
import BattleTowers.monsters.CardboardGolem.CardboardGolem;
import BattleTowers.monsters.chess.queen.Queen;
import BattleTowers.monsters.chess.queen.customintents.QueenDrainIntent;
import BattleTowers.monsters.executiveslime.ExecutiveSlime;
import BattleTowers.monsters.executiveslime.Slimeling;
import BattleTowers.subscribers.PetrifyingGazeApplyPowerSubscriber;
import BattleTowers.subscribers.TriggerSlimeFilledRoomPowerPostExhaustSubscriber;
import BattleTowers.util.KeywordWithProper;
import BattleTowers.util.TextureLoader;
import basemod.AutoAdd;
import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import basemod.abstracts.CustomRelic;
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
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.monsters.exordium.Cultist;
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
        EditKeywordsSubscriber,
        EditRelicsSubscriber,
        AddAudioSubscriber,
        EditCardsSubscriber
{
    public static final Logger logger = LogManager.getLogger(BattleTowers.class);
    private static SpireConfig modConfig = null;

    public static final String modid = "battleTowers"; //same as pom, for keywords

    public static final String WHARGH_KEY = makeID("WHARGH");
    private static final String WHARGH_OGG = "battleTowersResources/Audio/WHARGH.ogg";
    public static final String PEW_KEY = makeID("Pew");
    private static final String PEW_OGG = "battleTowersResources/Audio/Pew.ogg";

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
        BaseMod.addMonster(makeID("CardboardGolem"), () -> new CardboardGolem());
        BaseMod.addMonster(Encounters.CULTIST_ARMORER, () -> new MonsterGroup(
                new AbstractMonster[] {
                        new CultistArmorer(-250.0F, 0.0F),
                        new Cultist(100.0F, 0.0F)
                }));
        BaseMod.addMonster(tneisnarT.ID, (BaseMod.GetMonster) tneisnarT::new);
        BaseMod.addMonster(Encounters.NINJA_LOUSES, () -> new MonsterGroup(
                new AbstractMonster[] {
                        new NinjaLouse(-150.0F, 0.0F, false),
                        new NinjaLouse(100.0F, 0.0F, true),
                }));
        BaseMod.addMonster(Paladin.ID, (BaseMod.GetMonster) Paladin::new);
        BaseMod.addMonster(LouseHorde.ID, (BaseMod.GetMonster) LouseHorde::new);
        BaseMod.addMonster(Romeo.ID, (BaseMod.GetMonster) Romeo::new);
        BaseMod.addMonster(Encounters.RAINBOW_LOUSES, () -> new MonsterGroup(
                new AbstractMonster[] {
                        new RainbowLouse(-400.0F, 25.0F),
                        new RainbowLouse(-125.0F, 10.0F),
                        new RainbowLouse(120.0F, 30.0F)
                }));

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
        BaseMod.addMonster(ZastraszTheJusticar.ID, (BaseMod.GetMonster) ZastraszTheJusticar::new);
        BaseMod.addMonster(AspiringChampion.ID, (BaseMod.GetMonster) AspiringChampion::new);

        //Bosses
        BaseMod.addMonster(CardboardGolem.ID, (BaseMod.GetMonster) CardboardGolem::new);
        BaseMod.addMonster(Dijinn.ID, (BaseMod.GetMonster) Dijinn::new);
        BaseMod.addMonster(AlphabetBoss.ID, (BaseMod.GetMonster) AlphabetBoss::new);
        BaseMod.addMonster(ExecutiveSlime.ID, () -> new MonsterGroup(
                    new AbstractMonster[] {
                            new Slimeling(ExecutiveSlime.POS_X[0], ExecutiveSlime.POS_Y[0]).setMinionIndex(0),
                            new Slimeling(ExecutiveSlime.POS_X[1], ExecutiveSlime.POS_Y[1]).setMinionIndex(1),
                            new ExecutiveSlime()
                    }));
        BaseMod.addMonster(Queen.ID, (BaseMod.GetMonster) Queen::new);
        BaseMod.addMonster(GiantArm.ID, () -> new GiantArm(0.0F, 0.0F));
        BaseMod.addMonster(PrismGuardian.ID, () -> new PrismGuardian(0.0F, 0.0F));
        BaseMod.addMonster(Encounters.MAGUS_AND_ASSASSIN, () -> new MonsterGroup(
                new AbstractMonster[] {
                        new Assassin(-120.0F, 0.0F),
                        new Magus(120.0F, 0.0F),
                }));
        BaseMod.addMonster(Necrototem.ID, (BaseMod.GetMonster) Necrototem::new);
        BaseMod.addMonster(NatariTheTimewalker.ID, (BaseMod.GetMonster) NatariTheTimewalker::new);
    }

    private static void addEvents() {
        BaseMod.addEvent(CoolExampleEvent.ID, CoolExampleEvent.class, ""); //Only appears in dungeons with the ID "", which should be none.
        BaseMod.addEvent(EmeraldFlame.ID, EmeraldFlame.class, "");
        BaseMod.addEvent(OttoEvent.ID, OttoEvent.class, "");
        BaseMod.addEvent(ArmorerEvent.ID, ArmorerEvent.class, "");
        BaseMod.addEvent(BannerSageEvent.ID, BannerSageEvent.class, "");
        BaseMod.addEvent(GenieLampEvent.ID, GenieLampEvent.class, "");
        BaseMod.addEvent(VoidShrine.ID, VoidShrine.class, "");
        BaseMod.addEvent(PotOfGoldEvent.ID, PotOfGoldEvent.class, "");
        BaseMod.addEvent(RoarOfTheCrowd.ID, RoarOfTheCrowd.class, "");
        BaseMod.addEvent(GentlemanEvent.ID, GentlemanEvent.class, "");
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
    public static String makeMusicPath(String resourcePath) {
        return getModID() + "Resources/audio/music/" + resourcePath;
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
        new AutoAdd("BattleTowers")
                .packageFilter("BattleTowers.relics")
                .any(CustomRelic.class, (info, r) -> {
                    BaseMod.addRelic(r, RelicType.SHARED);
                });
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
        new AutoAdd("BattleTowers")
                .packageFilter("BattleTowers.cards")
                .setDefaultSeen(true)
                .cards();
    }

    @Override
    public void receiveAddAudio() {
        BaseMod.addAudio(WHARGH_KEY, WHARGH_OGG);
        BaseMod.addAudio(PEW_KEY, PEW_OGG);
    }
}
