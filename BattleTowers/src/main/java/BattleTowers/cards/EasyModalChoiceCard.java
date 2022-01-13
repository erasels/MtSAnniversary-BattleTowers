package BattleTowers.cards;

import basemod.AutoAdd;
import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static BattleTowers.BattleTowers.makeID;

@AutoAdd.Ignore
public class EasyModalChoiceCard extends CustomCard {

    private Runnable onUseOrChosen;
    private String passedName;
    private String passedDesc;
    private String passedImg;

    public EasyModalChoiceCard(int magicnum, String name, String description, String img, Runnable onUseOrChosen) {
        this(name,description,img,onUseOrChosen);
        baseMagicNumber = magicNumber = magicnum;
    }

    public EasyModalChoiceCard(String name, String description, String img, Runnable onUseOrChosen) {
        super(makeID(name), "", img,
                -2, "", CardType.POWER, CardColor.COLORLESS, CardRarity.SPECIAL, CardTarget.NONE);
        this.name = this.originalName = passedName = name;
        this.rawDescription = passedDesc = description;
        this.onUseOrChosen = onUseOrChosen;
        this.passedImg = img;
        initializeTitle();
        initializeDescription();
    }

    @Override
    public void onChoseThisOption() {
        onUseOrChosen.run();
    }

    @Override
    public void use(AbstractPlayer abstractPlayer, AbstractMonster abstractMonster) {
        onUseOrChosen.run();
    }

    @Override
    public boolean canUpgrade() {
        return false;
    }

    @Override
    public void upgrade() {
    }

    @Override
    public AbstractCard makeCopy() {
        return new EasyModalChoiceCard(passedName, passedDesc, passedImg, onUseOrChosen);
    }
}
