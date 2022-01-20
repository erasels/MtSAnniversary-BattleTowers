package BattleTowers.actions;

import BattleTowers.monsters.AspiringChampion;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.watcher.VigorPower;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BonkChampionAction extends AbstractGameAction {
    private AspiringChampion owner;
    
    public BonkChampionAction(AspiringChampion owner) {
        this.owner = owner;
        this.actionType = ActionType.DEBUFF;
    }
    
    @Override
    public void update() {
        this.owner.debuffAttack();
    }
}
