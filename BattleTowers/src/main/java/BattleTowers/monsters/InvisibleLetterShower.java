package BattleTowers.monsters;

import BattleTowers.util.TextureLoader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class InvisibleLetterShower extends AbstractBTMonster {
    public static final String ID = makeID(ExampleMonster.class.getSimpleName());

    //name of the monster's moves

    public String lastKnownLetter;
    private float rotation0 = 0;
    private float rotation1 = 0.5F;
    private float rotation2 = 0.2F;
    private static Texture background = TextureLoader.getTexture("battleTowersResources/img/vfx/letters/Background.png");
    private static ArrayList<Texture> letter_textures = new ArrayList<>();

    private static String getCharForNumber(int i) {
        if (i > 0 && i < 27) {
            String blah = String.valueOf((char) (i + 64));
            //System.out.println(blah);
            return blah;
        }
        return null;
    }

    private static int getNumberForChar(String s) {
        char[] ch = s.toCharArray();
        for (char c : ch) {
            int temp = (int) c;
            int temp_integer = 96; //for lower case
            if (temp <= 122 & temp >= 97)
                return temp - temp_integer - 1;
        }
        return 0;
    }

    static {
        for (int i = 1; i < 27; i++) {
            letter_textures.add(TextureLoader.getTexture("battleTowersResources/img/vfx/letters/" + getCharForNumber(i) + ".png"));
        }
    }

    public InvisibleLetterShower(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super("", ID, 140, 0.0F, 0.0f, 200.0f, 220.0f, null, x, y);
        // HANDLE YOUR ANIMATION STUFF HERE
        // this.animation = Whatever your animation is

        // calcAscensionTankiness automatically scales HP based on ascension and enemy type
        // passing 2 values makes the game randomly select a value in between the ranges for the HP
        // if you pass only 1 value to set HP it will use that as the HP value
        setHp(calcAscensionTankiness(36), calcAscensionTankiness(42));


    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.NORMAL;
    }

    @Override
    public void takeTurn() {
    }

    @Override
    protected void getMove(final int num) {
    }

    public boolean shouldRenderIntent = false;

    @Override
    public void render(SpriteBatch sb) {
        if (shouldRenderIntent) {
            this.renderIntentVfxBehind(sb);
            this.renderIntent(sb);
        }
    }

    @Override
    protected void renderIntentVfxBehind(SpriteBatch sb) {
        float width_halved = background.getWidth() / 2F;
        float width = background.getWidth();
        float height_halved = background.getHeight() / 2F;
        float height = background.getHeight();
        sb.setColor(Color.WHITE.cpy());
        sb.draw(background, this.intentHb.cX - width_halved, this.intentHb.cY - height_halved, width_halved, height_halved, width, height, Settings.scale * 0.5F, Settings.scale * 0.5F, rotation0, 0, 0, (int)width, (int)height, false, false);
        sb.setColor(new Color(220F,220F,220F,1F));
        sb.draw(background, this.intentHb.cX - width_halved, this.intentHb.cY - height_halved, width_halved, height_halved, width, height, Settings.scale * 0.55F, Settings.scale * 0.55F, rotation1, 0, 0, (int)width, (int)height, false, false);
        sb.setColor(new Color(190F,190F,190F,1F));
        sb.draw(background, this.intentHb.cX - width_halved, this.intentHb.cY - height_halved, width_halved, height_halved, width, height, Settings.scale * 0.6F, Settings.scale * 0.6F, rotation2, 0, 0, (int)width, (int)height, false, false);
        sb.setColor(Color.WHITE.cpy());
    }

    @Override
    protected void renderIntent(SpriteBatch sb) {
        sb.setColor(Color.WHITE.cpy());
        Texture to_grab = letter_textures.get(getNumberForChar(lastKnownLetter));
        float width_halved = to_grab.getWidth() / 2F;
        float width = to_grab.getWidth();
        float height_halved = to_grab.getHeight() / 2F;
        float height = to_grab.getHeight();
        sb.draw(to_grab, this.intentHb.cX - width_halved, this.intentHb.cY - height_halved, width_halved, height_halved, width, height, Settings.scale * 0.7F, Settings.scale * 0.7F, 0, 0, 0, (int)width, (int)height, false, false);
    }

    @Override
    public void update() {
        super.update();
        rotation0 += Gdx.graphics.getDeltaTime() * (AbstractDungeon.cardRandomRng.random(2, 6F));
        rotation1 += Gdx.graphics.getDeltaTime() * (AbstractDungeon.cardRandomRng.random(1F,5F));
        rotation2 -= Gdx.graphics.getDeltaTime() * (AbstractDungeon.cardRandomRng.random(7F, 10F));
    }
}