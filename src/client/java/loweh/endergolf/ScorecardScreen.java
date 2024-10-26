package loweh.endergolf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import oshi.util.tuples.Pair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The screen opened upon using the Scorecard item. Needs significant improvements with regards to how page information
 * is stored. It should be centralized in a class stored in the client ModInitializer to save score info between opening
 * and closing the Scorecard item. Proper commenting will follow once the code has been refactored to reflect these changes.
 * Field and method names should also be audited.
 */
@Environment(EnvType.CLIENT)
public class ScorecardScreen extends Screen {
    public final Screen prevScr;
    public final int buttonWidth = 10;
    public final int buttonHeight = buttonWidth;
    public final int textWidth = 200;
    public final int textHeight = buttonHeight;
    private final int bottomButtonW = (buttonWidth * 2 + textWidth) / 3;
    private final int bottomButtonH = (int) (buttonHeight * 1.5f);
    private final int buttonRightPadding = 10;
    private final int bottomPadding = 5;
    private final int gridPadding = 10;
    private final int gridWidth = buttonWidth * 2 + textWidth;
    private final int gridHeight = height - buttonHeight * 2 - gridPadding * 2;
    private final int scoreFieldWidth = buttonWidth + 5;
    private final int scoreFieldHeight = scoreFieldWidth;
    private int totalScoreYPos = 0;

    public ButtonWidget goLeft;
    public ButtonWidget goRight;
    public ButtonWidget submit;
    public ButtonWidget clearScore;
    public ButtonWidget exit;
    public TextWidget title;
    public TextWidget totalScore;
    public ArrayList<Pair<TextWidget, ScorecardTextFieldWidget>> holeScores = new ArrayList<>();

    private final String[] titles = {"Front 9", "Back 9", "18 Hole"};
    private int curPagePos;
    private final ArrayList<int[]> scores = new ArrayList<>();

    protected ScorecardScreen() {
        super(Text.literal("Ender Golf Scorecard"));
        scores.add(new int[9]);
        scores.add(new int[9]);
        scores.add(new int[18]);
        prevScr = MinecraftClient.getInstance().currentScreen;
        curPagePos = 0;
    }

    public void updateScore(ScorecardTextFieldWidget scoreWidget) throws IllegalArgumentException {
        int hole = -1;
        int score = -1;

        for (int i = 0; i < holeScores.size(); i++) {
            ScorecardTextFieldWidget curScoreField = holeScores.get(i).getB();
            if (curScoreField.equals(scoreWidget)) {
                hole = i;
                score = Integer.parseInt(curScoreField.getText());

                for (int j = 0; j < scores.size(); j++) {
                    if (titles[j].compareTo(title.getMessage().getString()) == 0) {
                        int[] scoreArray = scores.get(j);
                        scoreArray[hole] = score;
                        updateTotal(j, false);
                    }
                }

                checkSubmit();

                break;
            }
        }

        if (hole == -1|| score == -1) {
            throw new IllegalArgumentException("Could not find the scoreWidget in the ScorecardScreen's holeScores list.");
        }
    }

    @Override
    protected void init() {
        buildHoleScores();
        buildWidgets();

        for (Field field : this.getClass().getFields()) {
            if (Widget.class.isAssignableFrom(field.getType())) {
                try {
                    ButtonWidget bWidget = null;
                    TextWidget tWidget = null;
                    ScorecardTextFieldWidget tfWidget = null;

                    if (ButtonWidget.class.isAssignableFrom(field.getType())) {
                        bWidget = (ButtonWidget) field.get(this);
                    } else if (TextWidget.class.isAssignableFrom(field.getType())) {
                        tWidget = (TextWidget) field.get(this);
                    } else if (TextFieldWidget.class.isAssignableFrom(field.getType())) {
                        tfWidget = (ScorecardTextFieldWidget) field.get(this);
                    } else {
                        System.out.println("Unknown class name from widget field: " + field.getType().getName());
                        break;
                    }

                    addDrawableChild(bWidget != null ? bWidget : (tWidget != null ? tWidget : tfWidget));
                } catch (IllegalAccessException accEx) {
                    System.out.println("An illegal access exception occurred in ScorecardScreen init.");
                }
            }
        }

        updatePageInformation();
    }

    private void buildWidgets() {
        goLeft = ButtonWidget.builder(Text.literal("<"), button -> { pressLeft(); }).dimensions(
                getButtonOffset(true),
                buttonHeight / 2,
                buttonWidth,
                buttonHeight
        ).build();
        goRight = ButtonWidget.builder(Text.literal(">"), button -> { pressRight(); }).dimensions(
                getButtonOffset(false),
                buttonHeight / 2,
                buttonWidth,
                buttonHeight
        ).build();
        submit = ButtonWidget.builder(Text.literal("Submit"), button -> { submit(); }).dimensions(
                width / 2 - (int)(bottomButtonW * 1.5) - buttonRightPadding,
                height - bottomButtonH * 2 + bottomPadding,
                bottomButtonW,
                bottomButtonH
        ).build();
        submit.active = false;
        clearScore = ButtonWidget.builder(Text.literal("Clear"), button -> { clearScore(); }).dimensions(
                width / 2 - bottomButtonW / 2,
                height - bottomButtonH * 2 + bottomPadding,
                bottomButtonW,
                bottomButtonH
        ).build();
        exit = ButtonWidget.builder(Text.literal("Exit"), button -> { close(); }).dimensions(
                width / 2 + bottomButtonW / 2 + buttonRightPadding,
                height - bottomButtonH * 2 + bottomPadding,
                bottomButtonW,
                bottomButtonH
        ).build();

        title = new TextWidget(Text.literal("Front 9"), MinecraftClient.getInstance().textRenderer);
        title.alignCenter();
        title.setDimensionsAndPosition(textWidth, textHeight, width / 2 - (textWidth / 2), textHeight / 2);
        totalScore = new TextWidget(Text.literal("Total: 0"), MinecraftClient.getInstance().textRenderer);
        totalScore.alignCenter();
        totalScore.setDimensionsAndPosition(gridWidth / 4, gridHeight / 9, width / 2 + gridWidth / 3, scoreFieldHeight + textHeight + gridPadding);
    }

    private void buildHoleScores() {
        for (int i = 0; i < 18; i++) {
            int yPos = scoreFieldHeight * (i < 9 ? i + 1 : i - 9 + 1) + textHeight + gridPadding * (i < 9 ? i + 1 : i - 9 + 1);
            holeScores.add(new Pair<TextWidget, ScorecardTextFieldWidget>(
                    new TextWidget(
                            i < 9 ? width / 4 - gridWidth / 4 : width / 2 - gridWidth / 4,
                            yPos,
                            gridWidth / 4,
                            gridHeight / 9,
                            Text.literal("Hole " + (i + 1)),
                            MinecraftClient.getInstance().textRenderer
                    ),
                    new ScorecardTextFieldWidget(
                            MinecraftClient.getInstance().textRenderer,
                            i < 9 ? width / 4 + scoreFieldWidth : width / 2 + scoreFieldWidth,
                            yPos - 9,
                            scoreFieldWidth,
                            scoreFieldHeight,
                            Text.literal("0"),
                            this
                    )
            ));

            addDrawableChild(holeScores.get(i).getA());
            addDrawableChild(holeScores.get(i).getB());

            ScorecardTextFieldWidget scWidget = holeScores.get(i).getB();
            scWidget.setMaxLength(1);

            TextWidget tWidget = holeScores.get(i).getA();
            tWidget.alignCenter();
        }
    }

    private void updatePageInformation() {
        int length = scores.get(curPagePos).length;

        for (Pair<TextWidget, ScorecardTextFieldWidget> pair : holeScores) {
            int index = holeScores.indexOf(pair);

            if (length != 18) {
                if (index > 9 - 1) {
                    pair.getA().visible = false;
                    pair.getB().visible = false;
                } else {
                    int score = scores.get(curPagePos)[index];
                    pair.getB().setText("" + (score != 0 ? score : ""));
                }
            } else {
                pair.getA().visible = true;
                pair.getB().visible = true;
                int score = scores.get(curPagePos)[index];
                pair.getB().setText("" + (score != 0 ? score : ""));
            }
        }

        updateTotal(curPagePos, false);
        checkSubmit();
    }

    @Override
    public void close() {
        try {
            client.setScreen(prevScr);
        } catch (NullPointerException ptrEx) {
            System.out.println("Null Pointer Exception in ScorecardScreen close.");
        }
    }

    private int getButtonOffset(boolean left) {
        int ans = 0;

        if (left) {
            ans = width / 2 - (textWidth / 2);
        } else {
            ans = width / 2 + (textWidth / 2);
        }

        return ans;
    }

    private void pressLeft() {
        if (curPagePos > 0) {
            curPagePos--;
        } else {
            curPagePos = titles.length - 1;
        }

        title.setMessage(Text.of(titles[curPagePos]));
        updatePageInformation();

    }

    private void pressRight() {
        if (curPagePos < titles.length - 1) {
            curPagePos++;
        } else {
            curPagePos = 0;
        }

        title.setMessage(Text.of(titles[curPagePos]));
        updatePageInformation();
    }

    private void submit() {

    }

    private void clearScore() {
        Arrays.fill(scores.get(curPagePos), 0);

        updateTotal(0, true);

        for (Pair<TextWidget, ScorecardTextFieldWidget> pair : holeScores) {
            ScorecardTextFieldWidget field = pair.getB();
            field.setText("");
        }
    }


    private void updateTotal(int index, boolean clear) {
        int sum = 0;

        if (!clear) {
            int[] pageScore = scores.get(index);
            sum = Arrays.stream(pageScore).sum();
        }

        totalScore.setMessage(Text.literal("Total: " + sum));
    }

    private boolean checkSubmit() {
        boolean canSubmit = true;

        for (Pair<TextWidget, ScorecardTextFieldWidget> holeScore : holeScores) {
            if (holeScore.getB().getText().isEmpty()) {
                System.out.println("Empty field at " + holeScore.getA().getMessage().getString());
                canSubmit = false;
                break;
            }
        }

        if (canSubmit) {
            submit.active = true;
        } else {
            submit.active = false;
        }

        return canSubmit;
    }
}
