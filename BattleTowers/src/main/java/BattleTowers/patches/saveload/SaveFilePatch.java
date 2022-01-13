package BattleTowers.patches.saveload;

import BattleTowers.events.TowerEvent;
import BattleTowers.room.BattleTowerRoom;
import basemod.Pair;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static BattleTowers.BattleTowers.logger;

public class SaveFilePatch {
    private static final String CHOSEN_TOWER_KEY = "BATTLE_TOWER_CHOSEN_TOWER";
    private static final String TOWER_PATH_KEY = "BATTLE_TOWER_CHOSEN_PATH";

    /*--------------------- Data ---------------------*/
    @SpirePatch(
            clz = SaveFile.class,
            method = SpirePatch.CLASS
    )
    public static class Data {
        public static SpireField<Integer> chosenTower = new SpireField<>(()->null);
        public static SpireField<List<Pair<Integer, Integer>>> pathTaken = new SpireField<>(Collections::emptyList);
    }

    /*--------------------- Saving ---------------------*/
    @SpirePatch(
            clz = SaveFile.class,
            method = SpirePatch.CONSTRUCTOR,
            paramtypez = { SaveFile.SaveType.class }
    )
    public static class SaveToSaveInfo {
        @SpireInsertPatch(
                locator = Locator.class
        )
        public static void saveTowerData(SaveFile __instance) {
            if (AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
                if (AbstractDungeon.getCurrRoom().event instanceof TowerEvent) {
                    TowerEvent event = (TowerEvent) AbstractDungeon.getCurrRoom().event;

                    logger.info("Saving Battle Tower data.");
                    //Tower generation info doesn't have to be saved; it utilizes map rng so it will be the same every time.
                    if (event.chosenTower != -1) {
                        logger.info("Chosen tower: " + event.chosenTower);
                        Data.chosenTower.set(__instance, event.chosenTower);
                        logger.info("Path taken: " + event.pathTakenString());
                        Data.pathTaken.set(__instance, new ArrayList<>(event.pathTaken));
                    }
                    else {
                        logger.info("No tower chosen yet.");
                    }
                }
            }
        }

        private static class Locator extends SpireInsertLocator
        {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
            {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Class.class, "getName");
                return LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch(
            clz = SaveAndContinue.class,
            method = "save",
            paramtypez = { SaveFile.class }
    )
    public static class SaveInfoToFile
    {
        @SpireInsertPatch(
                locator = Locator.class,
                localvars = { "params" }
        )
        public static void addCustomSaveData(SaveFile save, HashMap<Object, Object> params)
        {
            Integer chosenTower = Data.chosenTower.get(save);
            if (chosenTower != null) {
                params.put(CHOSEN_TOWER_KEY, chosenTower);
                params.put(TOWER_PATH_KEY, Data.pathTaken.get(save));
            }
        }

        private static class Locator extends SpireInsertLocator
        {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
            {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(GsonBuilder.class, "create");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }


    /*--------------------- Loading ---------------------*/
    @SpirePatch(
            clz = SaveAndContinue.class,
            method = "loadSaveFile",
            paramtypez = { String.class }
    )
    public static class LoadDataFromFile
    {
        @SpireInstrumentPatch
        public static ExprEditor adjustLoad() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("fromJson") && m.getClassName().equals(Gson.class.getName())) {
                        m.replace("{" +
                                "$_ = $proceed($$);" +
                                LoadDataFromFile.class.getName() + ".loadCustomSaveData((" + SaveFile.class.getName() + ")$_, $0, $1);" +
                                "}");
                    }
                }
            };
        }
        public static void loadCustomSaveData(SaveFile saveFile, Gson gson, String savestr)
        {
            try
            {
                TowerData data = gson.fromJson(savestr, TowerData.class);
                Data.chosenTower.set(saveFile, data.BATTLE_TOWER_CHOSEN_TOWER);
                Data.pathTaken.set(saveFile, data.BATTLE_TOWER_CHOSEN_PATH);

            }
            catch (Exception e)
            {
                logger.error("Failed to load BattleTower savedata.");
                e.printStackTrace();
            }
        }

        private static class TowerData {
            public Integer BATTLE_TOWER_CHOSEN_TOWER = null;
            public List<Pair<Integer, Integer>> BATTLE_TOWER_CHOSEN_PATH = Collections.emptyList();
        }
    }
}
