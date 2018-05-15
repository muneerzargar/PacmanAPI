package org.fullstack5.pacmanapi;

import org.fullstack5.pacmanapi.models.Direction;
import org.fullstack5.pacmanapi.models.Maze;
import org.fullstack5.pacmanapi.models.Piece;
import org.fullstack5.pacmanapi.models.Position;
import org.fullstack5.pacmanapi.models.response.GameState;
import reactor.core.publisher.Flux;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * GUI for a pacman game.
 */
public final class PacmanGui {

    private static final int GRID_WIDTH = 40;
    private static final int MS_PER_TICK = 1000;
    private static final int FRAMES_PER_TICK = 10;
    private static final int MS_PER_FRAME = MS_PER_TICK / FRAMES_PER_TICK;

    private final Maze maze;

    private GameState state;

    private int renderProgress = 0;

    public PacmanGui(final Maze maze) {
        this.maze = maze;
    }

    public final void initialize(final Flux<GameState> flux) {
        flux.subscribe(state -> {
            this.state = state;
            renderProgress = 0;
            System.out.println("Updated state");
        });

        final JFrame frame = new JFrame();
        final JPanel panel = new MyPanel();
        panel.setFocusable(true);
        panel.requestFocusInWindow();
        frame.add(panel);
        frame.pack();
        frame.setSize(maze.getWidth() * GRID_WIDTH + 16, maze.getHeight() * GRID_WIDTH + 38);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Chapter Fullstack 5 Pacman Simulator");
        frame.setVisible(true);

        new Thread(new GuiRunner(frame)).start();
    }

    private class GuiRunner implements Runnable {

        private JFrame frame;

        private GuiRunner(JFrame frame) {
            this.frame = frame;
        }

        @Override
        public void run() {
            while (true) {
                renderProgress++;
                System.out.println("Repaint");
                frame.repaint();
                try {
                    Thread.sleep(MS_PER_FRAME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MyPanel extends JPanel {

        @Override
        protected void paintComponent(final Graphics g) {
            renderMaze(g);
            renderPacman(g);
            if (state != null) {
                renderGhost(g, state.getBlinky(), Color.RED);
                renderGhost(g, state.getPinky(), Color.PINK);
                renderGhost(g, state.getInky(), Color.CYAN);
                renderGhost(g, state.getClyde(), Color.ORANGE);
            }
        }

        private void renderPacman(final Graphics g) {
            if (state == null) {
                return;
            }
            final Piece pacman = state.getPacman();
            int animProgress = (renderProgress + 5) % FRAMES_PER_TICK;
            if (animProgress > 6) {
                animProgress = FRAMES_PER_TICK - animProgress;
            }
            g.setColor(Color.yellow);
            final int startAngle = pacman.getDirection().getAngle();
            g.fillArc(
                    calcDrawX(pacman, renderProgress),
                    calcDrawY(pacman, renderProgress),
                    GRID_WIDTH - 1, GRID_WIDTH - 1, startAngle + 45 - animProgress * 9, 270 + animProgress * 18);

//            g.setColor(Color.black);
//            g.drawString(String.format("X = %d; Y = %d; direction = %s; renderProgress = %d", pacman.getPosition().getX(), pacman.getPosition().getY(), pacman.getDirection().name(), renderProgress), 50, 250);
        }

        private void renderGhost(final Graphics g, final Piece ghost, final Color color) {
            g.setColor(color);
            final int drawX = calcDrawX(ghost, renderProgress);
            final int drawY = calcDrawY(ghost, renderProgress);
            g.fillArc(drawX, drawY, GRID_WIDTH - 1, (GRID_WIDTH ) - 1, 0, 180);
            final int[] x = new int[] {drawX, drawX, drawX + GRID_WIDTH / 4, drawX + GRID_WIDTH / 2, drawX + GRID_WIDTH * 3 / 4, drawX + GRID_WIDTH, drawX + GRID_WIDTH};
            final int legsTop = drawY + GRID_WIDTH * 3 / 4;
            final int legsBottom = drawY + GRID_WIDTH - 1;
            final int[] y = new int[] {drawY + GRID_WIDTH / 2, legsBottom, legsTop, legsBottom, legsTop, legsBottom, drawY + GRID_WIDTH / 2};
            g.fillPolygon(x, y, x.length);
            g.setColor(Color.WHITE);
            g.fillOval(drawX + GRID_WIDTH / 8, drawY + GRID_WIDTH / 8, GRID_WIDTH / 4, GRID_WIDTH / 4);
            g.fillOval(drawX + GRID_WIDTH * 5 / 8, drawY + GRID_WIDTH / 8, GRID_WIDTH / 4, GRID_WIDTH / 4);
            g.setColor(Color.BLACK);
            g.drawOval(drawX + GRID_WIDTH / 8, drawY + GRID_WIDTH / 8, GRID_WIDTH / 4, GRID_WIDTH / 4);
            g.fillOval(drawX + GRID_WIDTH / 8 + (ghost.getDirection().getDeltaX() + 1) * GRID_WIDTH / 16, drawY + GRID_WIDTH / 8 + (ghost.getDirection().getDeltaY() + 1) * GRID_WIDTH / 16, GRID_WIDTH / 8, GRID_WIDTH / 8);
            g.drawOval(drawX + GRID_WIDTH * 5 / 8, drawY + GRID_WIDTH / 8, GRID_WIDTH / 4, GRID_WIDTH / 4);
            g.fillOval(drawX + GRID_WIDTH * 5 / 8 + (ghost.getDirection().getDeltaX() + 1) * GRID_WIDTH / 16, drawY + GRID_WIDTH / 8 + (ghost.getDirection().getDeltaY() + 1) * GRID_WIDTH / 16, GRID_WIDTH / 8, GRID_WIDTH / 8);
        }

        private void renderMaze(final Graphics g) {
            final int width = maze.getWidth();
            final int height = maze.getHeight();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (maze.isWall(x, y)) {
                        g.setColor(Color.blue);
                    } else {
                        g.setColor(Color.black);
                    }
                    g.fillRect(x * GRID_WIDTH, y * GRID_WIDTH, GRID_WIDTH - 1, GRID_WIDTH - 1);
                }
            }
        }
    }

    private static int calcDrawX(final Piece piece, final int renderProgress) {
        return GRID_WIDTH * piece.getPosition().getX() + GRID_WIDTH * renderProgress * piece.getDirection().getDeltaX() / FRAMES_PER_TICK;
    }

    private static int calcDrawY(final Piece piece, final int renderProgress) {
        return GRID_WIDTH * piece.getPosition().getY() + GRID_WIDTH * renderProgress * piece.getDirection().getDeltaY() / FRAMES_PER_TICK;
    }
}
