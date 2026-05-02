package BlackJack;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class BlackjackPanel extends JPanel implements ActionListener {
    private static final int PANEL_WIDTH = 1000;
    private static final int PANEL_HEIGHT = 760;
    private static final int CARD_WIDTH = 90;
    private static final int CARD_HEIGHT = 130;
    private static final int CARD_GAP = 34;
    private static final int PLAYER_Y = 470;
    private static final int DEALER_Y = 150;
    private static final int DECK_X = 450;
    private static final int DECK_Y = 300;
    private static final int TIMER_DELAY = 16;
    private static final int ANIMATION_STEPS = 24;

    private final BlackjackGame game = new BlackjackGame();
    private final Timer timer = new Timer(TIMER_DELAY, this);
    private final List<AnimatedCard> playerCards = new ArrayList<>();
    private final List<AnimatedCard> dealerCards = new ArrayList<>();
    private final Deque<DealRequest> dealQueue = new ArrayDeque<>();

    private final JButton hitButton = new JButton("Hit");
    private final JButton standButton = new JButton("Stand");
    private final JButton newRoundButton = new JButton("New Round");

    private AnimatedCard movingCard;
    private boolean movingToDealer;
    private int animationFrame;

    public BlackjackPanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(10, 85, 50));
        setFocusable(true);
        setLayout(null);

        configureButton(hitButton, 280, 690);
        configureButton(standButton, 430, 690);
        configureButton(newRoundButton, 580, 690);

        hitButton.addActionListener(event -> onHit());
        standButton.addActionListener(event -> onStand());
        newRoundButton.addActionListener(event -> startRound());

        add(hitButton);
        add(standButton);
        add(newRoundButton);

        updateButtons();
    }

    private void configureButton(JButton button, int x, int y) {
        button.setBounds(x, y, 140, 42);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setBackground(new Color(248, 204, 74));
        button.setForeground(new Color(38, 32, 24));
    }

    private void startRound() {
        playerCards.clear();
        dealerCards.clear();
        dealQueue.clear();
        movingCard = null;
        timer.stop();

        game.startNewRound();

        enqueueDeal(game.getPlayer().getHand().getCards().get(0), false, true);
        enqueueDeal(game.getDealer().getHand().getCards().get(0), true, true);
        enqueueDeal(game.getPlayer().getHand().getCards().get(1), false, true);
        enqueueDeal(game.getDealer().getHand().getCards().get(1), true, false);

        beginNextAnimation();
        updateButtons();
        repaint();
    }

    private void onHit() {
        Card card = game.hitPlayer();
        if (card == null) {
            return;
        }

        enqueueDeal(card, false, true);
        beginNextAnimation();
        updateButtons();
    }

    private void onStand() {
        game.stand();
        revealDealerCard();
        queueDealerTurn();
        updateButtons();
        repaint();
    }

    private void queueDealerTurn() {
        while (game.getDealer().shouldHit()) {
            Card card = game.drawDealerCard();
            if (card == null) {
                break;
            }
            enqueueDeal(card, true, true);
        }

        if (movingCard == null) {
            completeDealerTurnIfNeeded();
        } else if (!timer.isRunning()) {
            beginNextAnimation();
        }
    }

    private void enqueueDeal(Card card, boolean toDealer, boolean faceUp) {
        int index = toDealer ? dealerCards.size() + countQueued(true) : playerCards.size() + countQueued(false);
        double targetX = getHandStartX(index);
        double targetY = toDealer ? DEALER_Y : PLAYER_Y;
        dealQueue.addLast(new DealRequest(card, toDealer, faceUp, targetX, targetY));
    }

    private int countQueued(boolean dealerSide) {
        int count = 0;
        for (DealRequest request : dealQueue) {
            if (request.toDealer == dealerSide) {
                count++;
            }
        }
        return count;
    }

    private double getHandStartX(int index) {
        return 220 + (double) index * CARD_GAP;
    }

    private void revealDealerCard() {
        if (dealerCards.size() > 1) {
            dealerCards.get(1).setFaceUp(true);
        }
    }

    private void beginNextAnimation() {
        if (movingCard != null || dealQueue.isEmpty()) {
            if (movingCard == null) {
                handleAnimationQueueFinished();
            }
            return;
        }

        DealRequest request = dealQueue.removeFirst();
        movingCard = new AnimatedCard(
                request.card,
                DECK_X,
                DECK_Y,
                request.targetX,
                request.targetY,
                request.faceUp
        );
        movingToDealer = request.toDealer;
        animationFrame = 0;
        timer.start();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (movingCard == null) {
            timer.stop();
            return;
        }

        animationFrame++;
        double progress = Math.min(1.0, animationFrame / (double) ANIMATION_STEPS);
        double eased = 1 - Math.pow(1 - progress, 3);

        double nextX = DECK_X + (movingCard.getTargetX() - DECK_X) * eased;
        double nextY = DECK_Y + (movingCard.getTargetY() - DECK_Y) * eased;
        movingCard.setPosition(nextX, nextY);

        if (progress >= 1.0) {
            movingCard.setPosition(movingCard.getTargetX(), movingCard.getTargetY());
            if (movingToDealer) {
                dealerCards.add(movingCard);
            } else {
                playerCards.add(movingCard);
            }
            movingCard = null;

            if (dealQueue.isEmpty()) {
                timer.stop();
                handleAnimationQueueFinished();
            } else {
                beginNextAnimation();
            }
        }

        repaint();
    }

    private void handleAnimationQueueFinished() {
        if (game.getState() == GameState.DEALING) {
            game.finishInitialDeal();
            if (game.getState() == GameState.ROUND_OVER) {
                revealDealerCard();
            }
        } else if (game.getState() == GameState.DEALER_TURN) {
            completeDealerTurnIfNeeded();
        }

        updateButtons();
        repaint();
    }

    private void completeDealerTurnIfNeeded() {
        if (game.getState() != GameState.DEALER_TURN) {
            return;
        }

        if (game.getDealer().shouldHit()) {
            queueDealerTurn();
            return;
        }

        game.finishDealerTurn();
        updateButtons();
        repaint();
    }

    private void updateButtons() {
        boolean playerTurn = game.getState() == GameState.PLAYER_TURN && movingCard == null && dealQueue.isEmpty();
        hitButton.setEnabled(playerTurn);
        standButton.setEnabled(playerTurn);
        newRoundButton.setEnabled(movingCard == null);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawTable(graphics2D);
        drawDeck(graphics2D);
        drawHands(graphics2D);
        drawStatus(graphics2D);

        if (movingCard != null) {
            drawCard(graphics2D, movingCard);
        }

        graphics2D.dispose();
    }

    private void drawTable(Graphics2D graphics) {
        graphics.setColor(new Color(12, 112, 67));
        graphics.fillRoundRect(30, 30, PANEL_WIDTH - 60, PANEL_HEIGHT - 150, 40, 40);

        graphics.setColor(new Color(215, 179, 64));
        graphics.setStroke(new BasicStroke(4f));
        graphics.drawRoundRect(30, 30, PANEL_WIDTH - 60, PANEL_HEIGHT - 150, 40, 40);

        graphics.setColor(new Color(230, 241, 225, 70));
        graphics.drawArc(220, 365, 560, 180, 200, 140);
    }

    private void drawDeck(Graphics2D graphics) {
        graphics.setColor(new Color(31, 45, 88));
        graphics.fillRoundRect(DECK_X, DECK_Y, CARD_WIDTH, CARD_HEIGHT, 18, 18);
        graphics.setColor(new Color(249, 222, 90));
        graphics.drawRoundRect(DECK_X, DECK_Y, CARD_WIDTH, CARD_HEIGHT, 18, 18);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 20));
        drawCenteredText(graphics, "DECK", DECK_X, DECK_Y + 75, CARD_WIDTH);
    }

    private void drawHands(Graphics2D graphics) {
        for (AnimatedCard card : dealerCards) {
            drawCard(graphics, card);
        }

        for (AnimatedCard card : playerCards) {
            drawCard(graphics, card);
        }
    }

    private void drawCard(Graphics2D graphics, AnimatedCard animatedCard) {
        int x = (int) Math.round(animatedCard.getX());
        int y = (int) Math.round(animatedCard.getY());

        if (!animatedCard.isFaceUp()) {
            graphics.setColor(new Color(34, 52, 122));
            graphics.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 18, 18);
            graphics.setColor(new Color(241, 214, 99));
            graphics.drawRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 18, 18);
            graphics.drawRoundRect(x + 10, y + 10, CARD_WIDTH - 20, CARD_HEIGHT - 20, 14, 14);
            drawCenteredText(graphics, "BJ", x, y + 74, CARD_WIDTH);
            return;
        }

        Card card = animatedCard.getCard();
        graphics.setColor(new Color(252, 249, 243));
        graphics.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 18, 18);
        graphics.setColor(new Color(35, 35, 35));
        graphics.drawRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 18, 18);

        Color suitColor = card.getSuit().isRed() ? new Color(196, 45, 63) : new Color(20, 28, 38);
        graphics.setColor(suitColor);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 24));
        graphics.drawString(card.getRank().getSymbol(), x + 10, y + 28);
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 22));
        graphics.drawString(card.getSuit().getSymbol(), x + 12, y + 52);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 34));
        drawCenteredText(graphics, card.getSuit().getSymbol(), x, y + 78, CARD_WIDTH);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 24));
        graphics.drawString(card.getRank().getSymbol(), x + CARD_WIDTH - 26, y + CARD_HEIGHT - 12);
    }

    private void drawStatus(Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 28));
        graphics.drawString("Dealer", 80, 105);
        graphics.drawString("Player", 80, 425);

        graphics.setFont(new Font("SansSerif", Font.BOLD, 22));
        graphics.setColor(new Color(249, 222, 90));
        graphics.drawString("Wins: " + game.getWins(), 760, 110);
        graphics.drawString("Losses: " + game.getLosses(), 760, 145);
        graphics.drawString("Pushes: " + game.getPushes(), 760, 180);

        graphics.setColor(new Color(237, 242, 247));
        graphics.setFont(new Font("SansSerif", Font.BOLD, 20));
        graphics.drawString(getDealerScoreText(), 80, 135);
        graphics.drawString("Score: " + game.getPlayer().getHand().getBestValue(), 80, 455);

        graphics.setColor(new Color(255, 248, 220));
        graphics.setFont(new Font("SansSerif", Font.BOLD, 22));
        drawRightAlignedText(graphics, game.getMessage(), 930, 465);
    }

    private String getDealerScoreText() {
        if (dealerCards.size() < 2) {
            return "Score: ?";
        }

        if (game.getState() == GameState.PLAYER_TURN || game.getState() == GameState.DEALING) {
            Card visibleCard = game.getDealer().getHand().getCards().get(0);
            return "Score: " + visibleCard.getValue() + " + ?";
        }

        return "Score: " + game.getDealer().getHand().getBestValue();
    }

    private void drawCenteredText(Graphics2D graphics, String text, int x, int baselineY, int width) {
        FontMetrics metrics = graphics.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(text)) / 2;
        graphics.drawString(text, textX, baselineY);
    }

    private void drawRightAlignedText(Graphics2D graphics, String text, int rightX, int baselineY) {
        FontMetrics metrics = graphics.getFontMetrics();
        int textX = rightX - metrics.stringWidth(text);
        graphics.drawString(text, textX, baselineY);
    }

    private static class DealRequest {
        private final Card card;
        private final boolean toDealer;
        private final boolean faceUp;
        private final double targetX;
        private final double targetY;

        private DealRequest(Card card, boolean toDealer, boolean faceUp, double targetX, double targetY) {
            this.card = card;
            this.toDealer = toDealer;
            this.faceUp = faceUp;
            this.targetX = targetX;
            this.targetY = targetY;
        }
    }
}
