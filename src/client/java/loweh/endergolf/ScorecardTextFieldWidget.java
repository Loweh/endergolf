package loweh.endergolf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;

/**
 * Custom TextFieldWidget that allows characters in the range of [1,9] only, while defaulting to being empty.
 * Has a max length of 1 character, and the value of the field will be changed on key pressed to a valid character.
 * Thus, the backspace key is not necessary to change the field.
 */
@Environment(EnvType.CLIENT)
public class ScorecardTextFieldWidget extends TextFieldWidget {
    private int lastInput = 0;
    private final ScorecardScreen scoreScreen;

    public ScorecardTextFieldWidget(TextRenderer txtRenderer, int x, int y, int width, int height, Text text, ScorecardScreen scoreScreen) {
        super(txtRenderer, x, y, width, height, text);
        this.scoreScreen = scoreScreen;
    }

    /**
     * Only allows charTyped to return true (thus allowing the input to register) if the character is in
     * the valid range.
     * @param modifiers
     * @return
     */
    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean result = false;

        if (Character.isDigit(chr) && chr != '0') {
            setText("" + chr);

            try {
                scoreScreen.updateScore(this);
            } catch (IllegalArgumentException iArgEx) {
                System.out.println(iArgEx.getMessage());
                result = false;
            }

            result = true;
        } else if (chr == '\t') {
            result = true;
        }

        return result;
    }

    /**
     * Allows tabs to register to allow for easy navigation between fields through tabbing.
     * @param keyCode
     * @param scanCode
     * @param modifiers
     * @return
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW_KEY_BACKSPACE) {
            return false;
        } else if (keyCode == GLFW_KEY_TAB) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        return true;
    }
}
